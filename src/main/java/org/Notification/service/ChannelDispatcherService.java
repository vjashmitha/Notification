package org.Notification.service;

import org.Notification.channel.NotificationChannel;
import org.Notification.model.Notification;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChannelDispatcherService {

    // Map to store channel name → channel object
    private final Map<String, NotificationChannel> channelMap = new HashMap<>();

    private final int maxRetry = 3;

    //  Constructor → ONLY mapping (no calling)
    public ChannelDispatcherService(List<NotificationChannel> channels) {
        for (NotificationChannel c : channels) {
            channelMap.put(c.getChannelName(), c);
        }
    }

    // Main method → decides & calls correct channel
    public void dispatch(Notification n) throws InterruptedException {

        if (n == null || n.getChannel() == null) {
            System.out.println("Invalid notification");
            return;
        }

        String channelName = n.getChannel(); // EMAIL, SMS, PUSH, IN_APP

        NotificationChannel channel = channelMap.get(channelName);

        if (channel == null) {
            System.out.println("No channel found for " + channelName);
            return;
        }

        try {
            //  Only ONE channel will be called
            channel.send(n);

            System.out.println("Notification sent via " + channelName);

        } catch (Exception e) {

            System.out.println("Error sending notification: " + e.getMessage());

            //  Retry logic
            if (n.getRetryCount() >= maxRetry) {
                System.out.println("Max retries reached for notification " + n.getNotificationId());
            } else {
                n.setRetryCount(n.getRetryCount() + 1);
                dispatch(n); // retry again
            }
        }
    }
}
