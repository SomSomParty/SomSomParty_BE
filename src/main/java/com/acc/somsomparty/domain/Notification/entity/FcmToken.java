package com.acc.somsomparty.domain.Notification.entity;

import com.acc.somsomparty.domain.Notification.enums.DeviceType;
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

    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;  // 디바이스 유형

    @CreationTimestamp
    private LocalDateTime createdDate;  // fcm 토큰 생성 날짜

    @UpdateTimestamp
    private LocalDateTime updatedDate;   // fcm 토큰 갱신 날짜

    private Long userId;  // user 정보
}
