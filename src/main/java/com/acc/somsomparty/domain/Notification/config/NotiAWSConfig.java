package com.acc.somsomparty.domain.Notification.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class NotiAWSConfig {
    @Value("${aws.noti.accessKey}")
    private String notiAccessKey;

    @Value("${aws.noti.secretKey}")
    private String notiSecretKey;

    @Value("${aws.noti.region}")
    private String notiRegion;
}
