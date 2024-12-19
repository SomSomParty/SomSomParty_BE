package com.acc.somsomparty.domain.Notification.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreatePlatformEndpointRequest;
import software.amazon.awssdk.services.sns.model.CreatePlatformEndpointResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Service
@RequiredArgsConstructor
public class SNSServiceImpl implements SNSService {
    private final CredentialService credentialService;

    private Logger logger = LoggerFactory.getLogger(SNSServiceImpl.class);

    // SNS에 디바이스 토큰 등록
    private String registerDeviceToSNS(String platformApplicationArn, String fcmToken) {
        SnsClient snsClient = credentialService.getSnsClient();

        CreatePlatformEndpointRequest req = CreatePlatformEndpointRequest.builder()
                .platformApplicationArn(platformApplicationArn)
                .token(fcmToken)
                .build();

        CreatePlatformEndpointResponse response = snsClient.createPlatformEndpoint(req);

        String endpointArn = response.endpointArn();
        logger.info("Endpoint ARN created: " + endpointArn);
        return endpointArn;
    }

    // 엔드포인트에 메시지 전송
    private void sendMessageToEndpoint(String endpointArn, String title, String messageBody) {
        String message = "{"
                + "\"GCM\": \"{"
                + "\\\"notification\\\": {"
                + "\\\"title\\\": \\\"" + title + "\\\","
                + "\\\"body\\\": \\\"" + messageBody + "\\\""
                + "}}\""
                + "}";

        PublishRequest publishRequest = PublishRequest.builder()
                .targetArn(endpointArn)
                .message(message)
                .messageStructure("json")
                .build();

        SnsClient snsClient = credentialService.getSnsClient();
        PublishResponse publishResponse = snsClient.publish(publishRequest);

        System.out.println("Message sent to endpoint: " + endpointArn);
        System.out.println("Message ID: " + publishResponse.messageId());
    }

    // SNS 워크 플로우
    public void snsFCMWorkFlow(String platformApplicationArn, String token, String title, String msg) {
        String endpointArn = registerDeviceToSNS(platformApplicationArn, token);
        sendMessageToEndpoint(endpointArn, title, msg);
    }
}
