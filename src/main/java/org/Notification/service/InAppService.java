package org.Notification.service;

import org.Notification.model.InAppNotification;
import org.Notification.model.enums.NotificationType;
import org.Notification.repository.InAppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InAppService {

    @Autowired
    private InAppRepository inAppRepo;

    // ✅ CLEAN + CORRECT METHOD
    public void createInApp(String userId,
                            String title,
                            String desc,
                            NotificationType type,
                            String redirectUrl) {

        InAppNotification inApp = new InAppNotification();

        inApp.setId(UUID.randomUUID().toString());
        inApp.setUserId(userId);
        inApp.setTitle(title);
        inApp.setDescription(desc);
        inApp.setType(type); // ✅ no casting needed
        inApp.setCreatedAt(System.currentTimeMillis());
        inApp.setIsRead(false);
        inApp.setRedirectUrl(redirectUrl); // ✅ IMPORTANT (UI navigation)

        inAppRepo.save(inApp);
    }
}