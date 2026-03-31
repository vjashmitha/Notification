package channel;

import org.Notification.channel.InAppChannel;
import org.Notification.model.InAppNotification;
import org.Notification.model.Notification;
import org.Notification.model.enums.ChannelType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.Notification.repository.InAppRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InAppChannelTest {

    @Mock InAppRepository repo;
    @InjectMocks InAppChannel inAppChannel;

    @Test
    void getChannelName_shouldReturnIN_APP() {
        assertEquals("IN_APP", inAppChannel.getChannelName());
    }

    @Test
    void send_shouldSaveNotificationWithCorrectFields() {
        Notification n = buildNotification("user1", "Hello");

        inAppChannel.send(n);

        ArgumentCaptor<InAppNotification> captor = ArgumentCaptor.forClass(InAppNotification.class);
        verify(repo, times(1)).save(captor.capture());

        InAppNotification saved = captor.getValue();
        assertEquals("user1", saved.getUserId());
        assertEquals("Hello", saved.getMessage());
        assertEquals("SENT", saved.getStatus());
        assertFalse(saved.getIsRead());
        assertNotNull(saved.getId());
        assertTrue(saved.getCreatedAt() > 0);
    }

    @Test
    void send_shouldThrowWhenUserIdIsNull() {
        Notification n = new Notification();
        n.setUserId(null);
        n.setMessage("Test");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> inAppChannel.send(n));
        assertEquals("UserId is required", ex.getMessage());
        verify(repo, never()).save(any());
    }

    @Test
    void send_shouldThrowWhenUserIdIsEmpty() {
        Notification n = new Notification();
        n.setUserId("");
        n.setMessage("Test");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> inAppChannel.send(n));
        assertEquals("UserId is required", ex.getMessage());
        verify(repo, never()).save(any());
    }

    @Test
    void send_shouldGenerateUniqueIds() {
        inAppChannel.send(buildNotification("user1", "msg1"));
        inAppChannel.send(buildNotification("user2", "msg2"));

        ArgumentCaptor<InAppNotification> captor = ArgumentCaptor.forClass(InAppNotification.class);
        verify(repo, times(2)).save(captor.capture());

        assertNotEquals(captor.getAllValues().get(0).getId(),
                        captor.getAllValues().get(1).getId());
    }

    private Notification buildNotification(String userId, String message) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setMessage(message);
        n.setChannel(ChannelType.IN_APP);
        return n;
    }
}


