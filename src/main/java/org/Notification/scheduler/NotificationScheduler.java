package org.Notification.scheduler;

import org.Notification.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.Notification.repository.NotificationRepository;
import org.Notification.service.ChannelDispatcherService;
import org.springframework.beans.factory.annotation.Value;

@Component
public class NotificationScheduler {

    @Autowired
    private NotificationRepository repo;

    @Autowired
    private ChannelDispatcherService dispatcher;

    @Value("${notification.scheduler.max-retry}")
    private int maxRetry;

    @Scheduled(fixedDelayString = "${notification.scheduler.interval}")
    public void run() {

        System.out.println("Scheduler running...");

        Iterable<Notification> list = repo.findAll();

        if (list == null) {
            System.out.println("No data from DB");
            return;
        }

        for (Notification n : list) {
            if (n == null) continue;

            if ("PENDING".equalsIgnoreCase(n.getStatus())) {
                // your logic
            }
        }
    }
}