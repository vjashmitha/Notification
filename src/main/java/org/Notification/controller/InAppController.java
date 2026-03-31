package org.Notification.controller;

import lombok.extern.slf4j.Slf4j;
import org.Notification.model.InAppNotification;
import org.Notification.repository.InAppRepository;
import org.Notification.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/inapps")
public class InAppController {

    @Autowired
    private InAppRepository repo;

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ Get all notifications
    @GetMapping
    public List<InAppNotification> getNotifications(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String userId = jwtUtil.getUserId(token);

        List<InAppNotification> list = new ArrayList<>(repo.findByUserId(userId));

        // sort latest first
        list.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

        return list;
    }

    // ✅ Mark as read
    @PutMapping("/read/{id}")
    public String markAsRead(@PathVariable String id) {

        return repo.findById(id).map(notif -> {
            notif.notify();
            repo.save((InAppNotification) notif);
            return "Marked as read";
        }).orElse("Notification not found");
    }

    // ✅ Get unread
    @GetMapping("/unread")
    public Collection<Object> getUnread(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String userId = jwtUtil.getUserId(token);

        return repo.findByUserIdAndIsRead(userId, false);
    }

    // ✅ Get unread count
    @GetMapping("/unread/count")
    public int getUnreadCount(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String userId = jwtUtil.getUserId(token);

        return repo.findByUserIdAndIsRead(userId, false).size();
    }

    // ✅ Delete
    @DeleteMapping("/{id}")
    public String deleteNotification(@PathVariable String id) {
        repo.deleteById(id);
        return "Notification deleted";
    }

    // ✅ Get one
    @GetMapping("/one/{id}")
    public InAppNotification getOne(@PathVariable String id) {
        return (InAppNotification) repo.findById(id).orElse(null);
    }
}