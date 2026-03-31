package repository;

import org.Notification.repository.InAppRepository;
import org.Notification.model.InAppNotification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InAppRepositoryTest {

    @Mock InAppRepository repo;

    @Test
    void save_shouldPersistInAppNotification() {
        InAppNotification n = buildNotif("user1", false);
        when(repo.save(n)).thenReturn(n);

        InAppNotification saved = repo.save(n);

        assertNotNull(saved);
        assertEquals("user1", saved.getUserId());
        verify(repo, times(1)).save(n);
    }

    @Test
    void findByUserId_shouldReturnList() {
        InAppNotification n = buildNotif("user1", false);
        when(repo.findByUserId("user1")).thenReturn(List.of(n));

        List<InAppNotification> result = repo.findByUserId("user1");

        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getUserId());
    }

    @Test
    void findByUserId_shouldReturnEmptyForUnknown() {
        when(repo.findByUserId("unknown")).thenReturn(List.of());
        assertTrue(repo.findByUserId("unknown").isEmpty());
    }

    @Test
    void findByUserIdAndIsRead_shouldReturnUnreadNotifications() {
        InAppNotification n = buildNotif("user1", false);
        when(repo.findByUserIdAndIsRead("user1", false)).thenReturn(List.of(n));

        Collection<Object> result = repo.findByUserIdAndIsRead("user1", false);

        assertEquals(1, result.size());
    }

    @Test
    void findByUserIdAndIsRead_shouldReturnEmptyWhenAllRead() {
        when(repo.findByUserIdAndIsRead("user1", false)).thenReturn(List.of());

        Collection<Object> result = repo.findByUserIdAndIsRead("user1", false);

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldReturnNotificationWhenExists() {
        String id = UUID.randomUUID().toString();
        InAppNotification n = buildNotif("user1", false);
        n.setId(id);
        when(repo.findById(id)).thenReturn(Optional.of(n));

        Optional<InAppNotification> result = repo.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        when(repo.findById("none")).thenReturn(Optional.empty());
        assertFalse(repo.findById("none").isPresent());
    }

    @Test
    void delete_shouldRemoveNotification() {
        InAppNotification n = buildNotif("user1", false);
        doNothing().when(repo).delete(n);
        repo.delete(n);
        verify(repo, times(1)).delete(n);
    }

    private InAppNotification buildNotif(String userId, boolean isRead) {
        InAppNotification n = new InAppNotification();
        n.setId(UUID.randomUUID().toString());
        n.setUserId(userId);
        n.setMessage("Test");
        n.setStatus("SENT");
        n.setCreatedAt(System.currentTimeMillis());
        n.setIsRead(isRead);
        return n;
    }
}


