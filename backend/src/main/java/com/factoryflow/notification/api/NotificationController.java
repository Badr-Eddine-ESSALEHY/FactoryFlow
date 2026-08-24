package com.factoryflow.notification.api;

import com.factoryflow.notification.application.NotificationService;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service) { this.service = service; }
    @GetMapping public List<NotificationResponse> list(Principal principal) { return service.list(principal.getName()); }
    @PatchMapping("/{id}/read") public NotificationResponse read(Principal principal, @PathVariable Long id) {
        return service.markRead(principal.getName(), id);
    }
}
