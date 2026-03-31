package controller;

import org.Notification.controller.NotificationController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.Notification.model.Notification;
import org.Notification.model.enums.ChannelType;
import org.Notification.model.enums.RoleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.Notification.security.JwtUtil;
import org.Notification.service.NotificationService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean NotificationService service;
    @MockBean JwtUtil jwtUtil;

    private static final String AUTH_HEADER = "Bearer test-token";

    @Test
    void POST_shouldCreateNotificationFromToken() throws Exception {
        when(jwtUtil.getUserId("test-token")).thenReturn("user1");

        Notification n = buildNotification("user1");
        n.setNotificationId("id-1");
        when(service.create(any())).thenReturn(n);

        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(n)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notificationId").value("id-1"))
                .andExpect(jsonPath("$.userId").value("user1"));

        verify(jwtUtil, times(1)).getUserId("test-token");
        verify(service, times(1)).create(any());
    }

    @Test
    void GET_shouldReturnNotificationsFromToken() throws Exception {
        when(jwtUtil.getUserId("test-token")).thenReturn("user1");

        Notification n = buildNotification("user1");
        n.setNotificationId("id-1");
        when(service.getByUser("user1")).thenReturn(List.of(n));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationId").value("id-1"))
                .andExpect(jsonPath("$[0].userId").value("user1"));

        verify(jwtUtil, times(1)).getUserId("test-token");
    }

    @Test
    void GET_shouldReturnEmptyListWhenNoNotifications() throws Exception {
        when(jwtUtil.getUserId("test-token")).thenReturn("user1");
        when(service.getByUser("user1")).thenReturn(List.of());

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void PUT_shouldUpdateNotification() throws Exception {
        Notification updated = buildNotification("user1");
        updated.setNotificationId("id-1");
        updated.setMessage("Updated message");

        when(service.update(eq("id-1"), any())).thenReturn(updated);

        mockMvc.perform(put("/api/notifications/id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value("id-1"))
                .andExpect(jsonPath("$.message").value("Updated message"));

        verify(service, times(1)).update(eq("id-1"), any());
    }

    @Test
    void DELETE_shouldDeleteNotificationAndReturn200() throws Exception {
        doNothing().when(service).delete("id-1");

        mockMvc.perform(delete("/api/notifications/id-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification deleted successfully"));

        verify(service, times(1)).delete("id-1");
    }

    private Notification buildNotification(String userId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setMessage("Test message");
        n.setChannel(ChannelType.EMAIL);
        n.setRole(RoleType.LEARNER);
        n.setStatus("PENDING");
        return n;
    }
}


