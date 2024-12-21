package com.acc.somsomparty.domain.Notification.service;

import com.acc.somsomparty.domain.Notification.config.NotiAWSConfig;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Service
@AllArgsConstructor
public class CredentialServiceImpl implements CredentialService {
    private NotiAWSConfig notiAwsConfig;

    public AwsCredentialsProvider getAwsCredentials(String accessKeyID, String secretAccessKey) {
        AwsBasicCredentials awsBasicCredentials =
                AwsBasicCredentials.create(accessKeyID, secretAccessKey);
        return () -> awsBasicCredentials;
    }

    public SnsClient getSnsClient() {
        return SnsClient.builder()
                .credentialsProvider(getAwsCredentials(notiAwsConfig.getNotiAccessKey(),
                        notiAwsConfig.getNotiSecretKey())
                ).region(Region.of(notiAwsConfig.getNotiRegion()))
                .build();
    }
}
