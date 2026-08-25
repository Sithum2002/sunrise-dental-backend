package com.sunrise.dental.repository;

import com.sunrise.dental.entity.Notification;
import com.sunrise.dental.enums.NotificationChannel;
import com.sunrise.dental.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientOrderBySentAtDesc(String recipient, Pageable pageable);

    @Query("select n from Notification n order by n.sentAt desc nulls last")
    Page<Notification> findAllOrderBySentAtDesc(Pageable pageable);

    @Modifying
    @Query("update Notification n set n.status = :status, n.sentAt = :sentAt where n.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") NotificationStatus status,
                      @Param("sentAt") java.time.LocalDateTime sentAt);
}
