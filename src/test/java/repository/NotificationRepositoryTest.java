package repository;

import org.Notification.repository.NotificationRepository;
import org.Notification.model.Notification;
import org.Notification.model.enums.ChannelType;
import org.Notification.model.enums.RoleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRepositoryTest {

    @Mock NotificationRepository repo;

    @Test
    void save_shouldPersistNotification() {
        Notification n = buildNotification("user1");
        when(repo.save(n)).thenReturn(n);

        Notification saved = repo.save(n);

        assertNotNull(saved);
        assertEquals("user1", saved.getUserId());
        verify(repo, times(1)).save(n);
    }

    @Test
    void findByUserId_shouldReturnList() {
        Notification n = buildNotification("user1");
        when(repo.findByUserId("user1")).thenReturn(List.of(n));

        List<Notification> result = repo.findByUserId("user1");

        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getUserId());
    }

    @Test
    void findByUserId_shouldReturnEmptyForUnknown() {
        when(repo.findByUserId("unknown")).thenReturn(List.of());
        assertTrue(repo.findByUserId("unknown").isEmpty());
    }

    @Test
    void findById_shouldReturnNotificationWhenExists() {
        String id = UUID.randomUUID().toString();
        Notification n = buildNotification("user1");
        n.setNotificationId(id);
        when(repo.findById(id)).thenReturn(Optional.of(n));

        Optional<Notification> result = repo.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getNotificationId());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        when(repo.findById("none")).thenReturn(Optional.empty());
        assertFalse(repo.findById("none").isPresent());
    }

    @Test
    void delete_shouldRemoveNotification() {
        Notification n = buildNotification("user1");
        doNothing().when(repo).delete(n);
        repo.delete(n);
        verify(repo, times(1)).delete(n);
    }

    private Notification buildNotification(String userId) {
        Notification n = new Notification();
        n.setNotificationId(UUID.randomUUID().toString());
        n.setUserId(userId);
        n.setChannel(ChannelType.EMAIL);
        n.setRole(RoleType.LEARNER);
        n.setMessage("Test");
        n.setStatus("PENDING");
        return n;
    }
}


