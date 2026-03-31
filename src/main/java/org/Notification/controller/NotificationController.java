package org.Notification.controller;

import org.Notification.model.Notification;
import org.Notification.service.NotificationService;
import org.Notification.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService service;

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ CREATE (UPDATED)
    @PostMapping
    public ResponseEntity<Notification> create(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody Notification n) {

        String token = authHeader.substring(7);

        String userId = jwtUtil.getUserId(token);
        String email = jwtUtil.getEmail(token);



        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(n, userId, email));
    }

    // ✅ GET BY USER (from token)
    @GetMapping
    public ResponseEntity<List<Notification>> getByUser(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String userId = jwtUtil.getUserId(token);

        return ResponseEntity.ok(service.getByUser(userId));
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Notification> updateNotification(
            @PathVariable String id,
            @RequestBody Notification updatedNotification) {

        Notification updated = service.update(id, updatedNotification);
        return ResponseEntity.ok(updated);
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable String id) {

        service.delete(id);
        return ResponseEntity.ok("Notification deleted successfully");
    }
}