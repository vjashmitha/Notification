package scheduler;

import org.Notification.scheduler.NotificationScheduler;
import org.Notification.repository.NotificationRepository;
import org.Notification.service.ChannelDispatcherService;
import org.Notification.model.Notification;
import org.Notification.model.enums.ChannelType;
import org.Notification.model.enums.RoleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.Notification.repository.NotificationRepository;
import org.Notification.service.ChannelDispatcherService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock NotificationRepository repo;
    @Mock ChannelDispatcherService dispatcher;
    @InjectMocks NotificationScheduler scheduler;

    @Test
    void run_shouldDispatchAndMarkSentForPendingNotification() {
        setMaxRetry(3);
        Notification n = pendingNotification(false, null);
        when(repo.findAll()).thenReturn(List.of(n));

        scheduler.run();

        verify(dispatcher, times(1)).dispatch(n);
        assertEquals("SENT", n.getStatus());
        verify(repo, times(1)).save(n);
    }

    @Test
    void run_shouldDispatchScheduledNotificationWhenTimeHasPassed() {
        setMaxRetry(3);
        long pastTime = System.currentTimeMillis() - 10000;
        Notification n = pendingNotification(true, pastTime);
        when(repo.findAll()).thenReturn(List.of(n));

        scheduler.run();

        verify(dispatcher, times(1)).dispatch(n);
        assertEquals("SENT", n.getStatus());
    }

    @Test
    void run_shouldSkipScheduledNotificationWhenTimeNotYet() {
        setMaxRetry(3);
        long futureTime = System.currentTimeMillis() + 99999999L;
        Notification n = pendingNotification(true, futureTime);
        when(repo.findAll()).thenReturn(List.of(n));

        scheduler.run();

        verify(dispatcher, never()).dispatch(any());
        verify(repo, never()).save(any());
    }

    @Test
    void run_shouldMarkFailedWhenMaxRetriesReached() {
        setMaxRetry(3);
        Notification n = pendingNotification(false, null);
        n.setRetryCount(3);
        when(repo.findAll()).thenReturn(List.of(n));
        doThrow(new RuntimeException("fail")).when(dispatcher).dispatch(n);

        scheduler.run();

        assertEquals("FAILED", n.getStatus());
        verify(repo, times(1)).save(n);
    }

    @Test
    void run_shouldIncrementRetryCountOnFailure() {
        setMaxRetry(3);
        Notification n = pendingNotification(false, null);
        n.setRetryCount(1);
        when(repo.findAll()).thenReturn(List.of(n));
        doThrow(new RuntimeException("fail")).when(dispatcher).dispatch(n);

        scheduler.run();

        assertEquals(2, n.getRetryCount());
    }

    @Test
    void run_shouldSkipNonPendingNotifications() {
        setMaxRetry(3);
        Notification n = pendingNotification(false, null);
        n.setStatus("SENT");
        when(repo.findAll()).thenReturn(List.of(n));

        scheduler.run();

        verify(dispatcher, never()).dispatch(any());
    }

    @Test
    void run_shouldDoNothingWhenNoNotifications() {
        setMaxRetry(3);
        when(repo.findAll()).thenReturn(List.of());

        scheduler.run();

        verify(dispatcher, never()).dispatch(any());
        verify(repo, never()).save(any());
    }

    private void setMaxRetry(int value) {
        ReflectionTestUtils.setField(scheduler, "maxRetry", value);
    }

    private Notification pendingNotification(boolean isScheduled, Long scheduledTime) {
        Notification n = new Notification();
        n.setNotificationId("id-1");
        n.setUserId("user1");
        n.setMessage("test");
        n.setChannel(ChannelType.EMAIL);
        n.setRole(RoleType.LEARNER);
        n.setStatus("PENDING");
        n.setIsScheduled(isScheduled);
        n.setScheduledTime(scheduledTime);
        n.setRetryCount(0);
        return n;
    }
}


