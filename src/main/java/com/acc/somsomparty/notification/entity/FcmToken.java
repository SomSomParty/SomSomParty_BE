package com.acc.somsomparty.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FcmToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fcmTokenId; // id

    private String fcmToken;    // fcm 토큰

    private String deviceType;  // 디바이스 유형

    @CreationTimestamp
    private LocalDateTime createdDate;  // fcm 토큰 생성 날짜

    @UpdateTimestamp
    private LocalDateTime updatedDate;   // fcm 토큰 갱신 날짜

    private String userId;  // user 정보
}
