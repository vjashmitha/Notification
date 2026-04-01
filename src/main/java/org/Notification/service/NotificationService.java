package org.Notification.service;

import org.Notification.model.Notification;
import org.Notification.model.UserPreference;
import org.Notification.repository.NotificationRepository;
import org.Notification.repository.UserPreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository repo;

    @Autowired
    private UserPreferenceRepository prefRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private InAppService inAppService;

    // ✅ CREATE
    public Notification create(Notification n, String userId, String email) {

        // 🔐 FROM JWT
        n.setUserId(userId);
        n.setEmail(email);

        // ⚠️ REQUIRED DEFAULTS (VERY IMPORTANT)
        n.setNotificationId(UUID.randomUUID().toString());
        n.setCreatedAt(System.currentTimeMillis());
        n.setStatus("PENDING");
        n.setRetryCount(0);
        if (n.getNotificationId() == null) {
            n.setNotificationId(UUID.randomUUID().toString());
        }

        if (n.getUserId() == null) {
            throw new RuntimeException("UserId missing from JWT");
        }

        if (n.getType() == null) {
            throw new RuntimeException("Type is required");
        }

        if (n.getIsRead() == null) n.setIsRead(false);
        if (n.getIsScheduled() == null) n.setIsScheduled(false);

        // 🚨 VALIDATION (THIS WAS MISSING)
        if (n.getType() == null) {
            throw new RuntimeException("Notification type is required");
        }
        if (n.getChannel() == null) {
            throw new RuntimeException("Channel is required");
        }

        // ✅ USER PREFERENCES
        UserPreference pref = prefRepo.findByUserId(userId);
        if (pref != null) {
            switch (n.getType()) {
                case FEEDBACK_ALERT:
                    if (Boolean.FALSE.equals(pref.getStudentFeedback())) return null;
                    break;
                case SESSION_REMINDER:
                    if (Boolean.FALSE.equals(pref.getLiveClassReminder())) return null;
                    break;
                case PAYOUT_UPDATE:
                    if (Boolean.FALSE.equals(pref.getPayoutUpdate())) return null;
                    break;
                case STREAK_ALERT:
                    if (Boolean.FALSE.equals(pref.getStreakUpdate())) return null;
                    break;
                case NEW_ENROLLMENT:
                    if (Boolean.FALSE.equals(pref.getNewEnrollment())) return null;
                    break;
                default:
                    break;
            }
        }

        // ✅ SAVE FIRST
        Notification saved = repo.save(n);

        // ✅ CHANNEL LOGIC (IMPORTANT FIXES)
        try {
            if ("IN_APP".equalsIgnoreCase(n.getChannel())) {

                inAppService.createInApp(
                        userId,
                        n.getTitle(),
                        n.getDescription(),
                        n.getType(),
                        n.getRedirectUrl()
                );

            } else if ("EMAIL".equalsIgnoreCase(n.getChannel())) {

                if (n.getEmail() == null) {
                    throw new RuntimeException("Email required for EMAIL channel");
                }

                emailService.sendEmail(
                        n.getEmail(),
                        n.getTitle(),
                        n.getDescription()
                );

            } else if ("SMS".equalsIgnoreCase(n.getChannel())) {

                if (n.getPhoneNumber() == null) {
                    throw new RuntimeException("Phone number required for SMS");
                }

                smsService.sendSms(
                        n.getPhoneNumber(),
                        n.getDescription()
                );
            }

            // ✅ SUCCESS
            saved.setStatus("SENT");

        } catch (Exception e) {
            // ❌ FAILURE HANDLING (VERY IMPORTANT)
            saved.setStatus("FAILED");
        }

        // ✅ UPDATE FINAL STATUS
        return repo.save(saved);
    }

    // ✅ GET
    public List<Notification> getByUser(String userId) {
        return repo.findByUserId(userId);
    }

    // ✅ UPDATE
    public Notification update(String id, Notification updated) {

        Notification existing = (Notification) repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (updated.getTitle() != null)
            existing.setTitle(updated.getTitle());

        if (updated.getDescription() != null)
            existing.setDescription(updated.getDescription());

        if (updated.getStatus() != null)
            existing.setStatus(updated.getStatus());

        if (updated.getChannel() != null)
            existing.setChannel(updated.getChannel());

        if (updated.getPhoneNumber() != null)
            existing.setPhoneNumber(updated.getPhoneNumber());

        if (updated.getDeviceToken() != null)
            existing.setDeviceToken(updated.getDeviceToken());

        return repo.save(existing);
    }

    // ✅ DELETE
    public void delete(String id) {
        Notification existing = (Notification) repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        repo.delete(existing);
    }
}