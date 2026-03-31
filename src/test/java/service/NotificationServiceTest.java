package service;

import org.Notification.service.NotificationService;
import org.Notification.repository.NotificationRepository;
import org.Notification.model.Notification;
import org.Notification.model.enums.ChannelType;
import org.Notification.model.enums.RoleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.Notification.repository.NotificationRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repo;

    @InjectMocks
    private NotificationService service;

    @Test
    void create_shouldSetNotificationIdStatusAndTimestamp() {
        Notification n = buildNotification("user1");
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Notification result = service.create(n);

        assertNotNull(result.getNotificationId());
        assertEquals("PENDING", result.getStatus());
        assertEquals(0, result.getRetryCount());
        assertNotNull(result.getCreatedAt());
        verify(repo, times(1)).save(n);
    }

    @Test
    void create_shouldGenerateUniqueIds() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Notification n1 = buildNotification("user1");
        Notification n2 = buildNotification("user2");
        service.create(n1);
        service.create(n2);

        assertNotEquals(n1.getNotificationId(), n2.getNotificationId());
    }

    @Test
    void getByUser_shouldReturnNotificationsForUser() {
        when(repo.findByUserId("user1")).thenReturn(List.of(buildNotification("user1")));

        List<Notification> result = service.getByUser("user1");

        assertEquals(1, result.size());
        verify(repo, times(1)).findByUserId("user1");
    }

    @Test
    void getByUser_shouldReturnEmptyListWhenNone() {
        when(repo.findByUserId("unknown")).thenReturn(List.of());

        assertTrue(service.getByUser("unknown").isEmpty());
    }

    private Notification buildNotification(String userId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setMessage("Test");
        n.setChannel(ChannelType.EMAIL);
        n.setRole(RoleType.LEARNER);
        return n;
    }
}


