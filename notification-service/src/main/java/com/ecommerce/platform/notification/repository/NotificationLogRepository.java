package com.ecommerce.platform.notification.repository;

import com.ecommerce.platform.notification.domain.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
}
