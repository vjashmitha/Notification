package channel;

import org.Notification.channel.SmsChannel;
import org.Notification.model.Notification;
import org.Notification.model.enums.ChannelType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmsChannelTest {

    private final SmsChannel smsChannel = new SmsChannel();

    @Test
    void getChannelName_shouldReturnSMS() {
        assertEquals("SMS", smsChannel.getChannelName());
    }

    @Test
    void send_shouldThrowWhenPhoneNumberIsNull() {
        Notification n = new Notification();
        n.setUserId("user1");
        n.setMessage("SMS message");
        n.setPhoneNumber(null);
        n.setChannel(ChannelType.SMS);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> smsChannel.send(n));
        assertEquals("Phone number is required for SMS channel", ex.getMessage());
    }

    @Test
    void send_shouldThrowWhenPhoneNumberIsEmpty() {
        Notification n = new Notification();
        n.setPhoneNumber("");
        n.setMessage("SMS message");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> smsChannel.send(n));
        assertEquals("Phone number is required for SMS channel", ex.getMessage());
    }
}


