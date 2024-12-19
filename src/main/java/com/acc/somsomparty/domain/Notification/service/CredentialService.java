package com.acc.somsomparty.domain.Notification.service;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.sns.SnsClient;

public interface CredentialService {
    AwsCredentialsProvider getAwsCredentials(String accessKeyID, String secretAccessKey);
    SnsClient getSnsClient();
}
