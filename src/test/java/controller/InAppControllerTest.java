package controller;

import org.Notification.model.InAppNotification;
import org.Notification.controller.InAppController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.Notification.repository.InAppRepository;
import org.Notification.security.JwtUtil;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InAppController.class)
class InAppControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean InAppRepository repo;
    @MockBean JwtUtil jwtUtil;

    private static final String AUTH_HEADER = "Bearer test-token";

    @Test
    void GET_shouldReturnSortedNotificationsFromToken() throws Exception {
        when(jwtUtil.getUserId("test-token")).thenReturn("user1");

        InAppNotification n1 = buildNotif("id-1", "user1", 1000L);
        InAppNotification n2 = buildNotif("id-2", "user1", 2000L);
        when(repo.findByUserId("user1")).thenReturn(List.of(n1, n2));

        mockMvc.perform(get("/api/inapps")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("id-2")) // latest first
                .andExpect(jsonPath("$[1].id").value("id-1"));

        verify(jwtUtil, times(1)).getUserId("test-token");
    }

    @Test
    void GET_shouldReturnEmptyListWhenNoNotifications() throws Exception {
        when(jwtUtil.getUserId("test-token")).thenReturn("user1");
        when(repo.findByUserId("user1")).thenReturn(List.of());

        mockMvc.perform(get("/api/inapps")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void PUT_markAsRead_shouldReturnMarkedAsRead() throws Exception {
        InAppNotification n = buildNotif("id-1", "user1", 1000L);
        when(repo.findById("id-1")).thenReturn(Optional.of(n));
        when(repo.save(any())).thenReturn(n);

        mockMvc.perform(put("/api/inapps/read/id-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Marked as read"));

        verify(repo, times(1)).save(any());
    }

    @Test
    void PUT_markAsRead_shouldReturnNotFoundWhenMissing() throws Exception {
        when(repo.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/inapps/read/missing"))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification not found"));
    }

    @Test
    void GET_unread_shouldReturnUnreadNotificationsFromToken() throws Exception {
        when(jwtUtil.getUserId("test-token")).thenReturn("user1");
        InAppNotification n = buildNotif("id-1", "user1", 1000L);
        when(repo.findByUserIdAndIsRead("user1", false)).thenReturn(List.of(n));

        mockMvc.perform(get("/api/inapps/unread")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("id-1"));
    }

    @Test
    void GET_unreadCount_shouldReturnCountFromToken() throws Exception {
        when(jwtUtil.getUserId("test-token")).thenReturn("user1");
        InAppNotification n = buildNotif("id-1", "user1", 1000L);
        when(repo.findByUserIdAndIsRead("user1", false)).thenReturn(List.of(n));

        mockMvc.perform(get("/api/inapps/unread/count")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void DELETE_shouldDeleteAndReturnMessage() throws Exception {
        doNothing().when(repo).deleteById("id-1");

        mockMvc.perform(delete("/api/inapps/id-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification deleted"));

        verify(repo, times(1)).deleteById("id-1");
    }

    @Test
    void GET_one_shouldReturnSingleNotification() throws Exception {
        InAppNotification n = buildNotif("id-1", "user1", 1000L);
        when(repo.findById("id-1")).thenReturn(Optional.of(n));

        mockMvc.perform(get("/api/inapps/one/id-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id-1"))
                .andExpect(jsonPath("$.userId").value("user1"));
    }

    @Test
    void GET_one_shouldReturnNullWhenNotFound() throws Exception {
        when(repo.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inapps/one/missing"))
                .andExpect(status().isOk());
    }

    private InAppNotification buildNotif(String id, String userId, long createdAt) {
        InAppNotification n = new InAppNotification();
        n.setId(id);
        n.setUserId(userId);
        n.setMessage("Hello");
        n.setStatus("SENT");
        n.setCreatedAt(createdAt);
        n.setIsRead(false);
        return n;
    }
}


