package com.acc.somsomparty.domain.Notification.repository;

import com.acc.somsomparty.domain.Notification.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    // userId를 통해 토큰 리스트 반환
    @Query("SELECT f.fcmToken FROM FcmToken f WHERE f.userId = :userId")
    List<String> findTokensByUserId(@Param("userId") Long userId);
}
