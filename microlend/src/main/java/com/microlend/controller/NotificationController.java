package com.microlend.controller;

import com.microlend.dto.response.ApiResponse;
import com.microlend.entity.Notification;
import com.microlend.enums.NotificationCategory;
import com.microlend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** POST /api/notifications/send - Admin or system sends a notification */
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Notification>> send(
            @RequestParam Long userID,
            @RequestParam String message,
            @RequestParam NotificationCategory category) {
        return ResponseEntity.ok(ApiResponse.success("Notification sent",
                notificationService.send(userID, message, category)));
    }

    /** GET /api/notifications/user/{userID} */
    @GetMapping("/user/{userID}")
    public ResponseEntity<ApiResponse<List<Notification>>> getByUser(@PathVariable Long userID) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getByUser(userID)));
    }

    /** GET /api/notifications/user/{userID}/unread */
    @GetMapping("/user/{userID}/unread")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnread(@PathVariable Long userID) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadByUser(userID)));
    }

    /** PATCH /api/notifications/{id}/read */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Notification>> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Marked as read", notificationService.markRead(id)));
    }

    /** PATCH /api/notifications/{id}/dismiss */
    @PatchMapping("/{id}/dismiss")
    public ResponseEntity<ApiResponse<Notification>> dismiss(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Notification dismissed", notificationService.dismiss(id)));
    }

    /** GET /api/notifications */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Notification>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getAll()));
    }
}
