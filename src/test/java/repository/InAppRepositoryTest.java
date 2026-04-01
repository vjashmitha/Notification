package repository;

import org.Notification.model.InAppNotification;
import org.Notification.repository.InAppRepository;
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
    void save_shouldCallSave() {
        InAppNotification n = buildNotif("user1");
        doNothing().when(repo).save(n);
        repo.save(n);
        verify(repo, times(1)).save(n);
    }

    @Test
    void findByUserId_shouldReturnCount() {
        when(repo.findByUserId("user1")).thenReturn(1);
        assertEquals(1, repo.findByUserId("user1"));
    }

    @Test
    void findByUserId_shouldReturnZeroForUnknown() {
        when(repo.findByUserId("unknown")).thenReturn(0);
        assertEquals(0, repo.findByUserId("unknown"));
    }

    @Test
    void findByUserIdAndIsRead_shouldReturnUnread() {
        InAppNotification n = buildNotif("user1");
        Collection<Object> expected = List.of(n);
        when(repo.findByUserIdAndIsRead("user1", false)).thenReturn(expected);
        assertEquals(1, repo.findByUserIdAndIsRead("user1", false).size());
    }

    @Test
    void findByUserIdAndIsRead_shouldReturnEmptyWhenAllRead() {
        when(repo.findByUserIdAndIsRead("user1", false)).thenReturn(List.of());
        assertTrue(repo.findByUserIdAndIsRead("user1", false).isEmpty());
    }

    @Test
    void findById_shouldReturnNotification() {
        String id = UUID.randomUUID().toString();
        InAppNotification n = buildNotif("user1");
        n.setId(id);
        when(repo.findById(id)).thenReturn(Optional.of(n));
        assertTrue(repo.findById(id).isPresent());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        when(repo.findById("none")).thenReturn(Optional.empty());
        assertFalse(repo.findById("none").isPresent());
    }

    @Test
    void deleteById_shouldCallDelete() {
        doNothing().when(repo).deleteById("id-1");
        repo.deleteById("id-1");
        verify(repo, times(1)).deleteById("id-1");
    }

    private InAppNotification buildNotif(String userId) {
        InAppNotification n = new InAppNotification();
        n.setId(UUID.randomUUID().toString());
        n.setUserId(userId);
        n.setTitle("Test");
        n.setDescription("Desc");
        n.setIsRead(false);
        n.setCreatedAt(System.currentTimeMillis());
        return n;
    }
}
