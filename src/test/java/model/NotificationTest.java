package model;

import org.Notification.model.Notification;
import org.Notification.model.enums.ChannelType;
import org.Notification.model.enums.NotificationType;
import org.Notification.model.enums.RoleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void builder_shouldSetAllFields() {
        Notification n = Notification.builder()
                .notificationId("id-1")
                .userId("user1")
                .message("Hello")
                .channel(ChannelType.EMAIL)
                .role(RoleType.LEARNER)
                .type(NotificationType.COURSE_ALERT)
                .status("PENDING")
                .email("user@example.com")
                .phoneNumber("1234567890")
                .deviceToken("token-abc")
                .isScheduled(false)
                .retryCount(0)
                .createdAt(System.currentTimeMillis())
                .build();

        assertEquals("id-1", n.getNotificationId());
        assertEquals("user1", n.getUserId());
        assertEquals("Hello", n.getMessage());
        assertEquals(ChannelType.EMAIL, n.getChannel());
        assertEquals(RoleType.LEARNER, n.getRole());
        assertEquals("PENDING", n.getStatus());
        assertEquals("user@example.com", n.getEmail());
        assertEquals("1234567890", n.getPhoneNumber());
        assertEquals("token-abc", n.getDeviceToken());
    }

    @Test
    void setters_shouldUpdateFields() {
        Notification n = new Notification();
        n.setStatus("SENT");
        n.setRetryCount(2);
        n.setEmail("test@test.com");
        n.setPhoneNumber("9999999999");
        n.setDeviceToken("device-xyz");

        assertEquals("SENT", n.getStatus());
        assertEquals(2, n.getRetryCount());
        assertEquals("test@test.com", n.getEmail());
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyObject() {
        Notification n = new Notification();
        assertNull(n.getNotificationId());
        assertNull(n.getUserId());
        assertEquals(0, n.getRetryCount());
    }

    @Test
    void equals_shouldBeTrueForSameFields() {
        Notification n1 = Notification.builder().notificationId("id-1").userId("u1").build();
        Notification n2 = Notification.builder().notificationId("id-1").userId("u1").build();
        assertEquals(n1, n2);
    }
}


