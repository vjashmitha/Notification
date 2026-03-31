package model;

import org.Notification.model.InAppNotification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InAppNotificationTest {

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        InAppNotification n = new InAppNotification();
        n.setId("id-1");
        n.setUserId("user1");
        n.setMessage("Hello");
        n.setStatus("SENT");
        n.setCreatedAt(1000L);
        n.setIsRead(false);

        assertEquals("id-1", n.getId());
        assertEquals("user1", n.getUserId());
        assertEquals("Hello", n.getMessage());
        assertEquals("SENT", n.getStatus());
        assertEquals(1000L, n.getCreatedAt());
        assertFalse(n.getIsRead());
    }

    @Test
    void isRead_shouldDefaultToFalse() {
        InAppNotification n = new InAppNotification();
        assertFalse(n.getIsRead());
    }

    @Test
    void setIsRead_shouldUpdateToTrue() {
        InAppNotification n = new InAppNotification();
        n.setIsRead(true);
        assertTrue(n.getIsRead());
    }

    @Test
    void defaultValues_shouldBeNullOrZero() {
        InAppNotification n = new InAppNotification();
        assertNull(n.getId());
        assertNull(n.getUserId());
        assertNull(n.getMessage());
        assertNull(n.getStatus());
        assertEquals(0L, n.getCreatedAt());
    }
}


