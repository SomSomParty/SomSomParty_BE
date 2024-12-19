package com.acc.somsomparty.domain.Notification.controller;

import com.acc.somsomparty.domain.Notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    // aws sns로 축제 알림 전송
    @PostMapping("/festivals/notification")
    public ResponseEntity<Object> sendNotification() {  // AWS SNS를 통해 알림 전송
        notificationService.setAWSSNSForFestival();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
