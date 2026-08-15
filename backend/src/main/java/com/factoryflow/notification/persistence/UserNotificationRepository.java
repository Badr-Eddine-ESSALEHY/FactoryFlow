package com.factoryflow.notification.persistence;

import com.factoryflow.notification.domain.UserNotification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<UserNotification> findByIdAndUserId(Long id, Long userId);
}
