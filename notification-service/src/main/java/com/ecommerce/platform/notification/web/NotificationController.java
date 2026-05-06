package com.ecommerce.platform.notification.web;

import com.ecommerce.platform.notification.domain.NotificationLog;
import com.ecommerce.platform.notification.repository.NotificationLogRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationLogRepository notificationLogRepository;

    public NotificationController(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @GetMapping
    public List<NotificationLog> findAll() {
        return notificationLogRepository.findAll();
    }
}
