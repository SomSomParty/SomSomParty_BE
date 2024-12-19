package com.acc.somsomparty.domain.Queue.config;

import io.awspring.cloud.sqs.config.SqsBootstrapConfiguration;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.acknowledgement.AcknowledgementOrdering;
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Import(SqsBootstrapConfiguration.class)
@Slf4j
@Configuration
public class AwsSQSConfig {
    @Value("${spring.cloud.aws.access-key}")
    private String AWS_ACCESS_KEY;
    @Value("${spring.cloud.aws.secret-key}")
    private String AWS_SECRET_KEY;
    @Value("${spring.cloud.aws.region}")
    private String AWS_REGION;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .credentialsProvider(() -> new AwsCredentials() {
                    @Override
                    public String accessKeyId() {
                        return AWS_ACCESS_KEY;
                    }

                    @Override
                    public String secretAccessKey() {
                        return AWS_SECRET_KEY;
                    }
                })
                .region(Region.of(AWS_REGION))
                .build();
    }

    @Bean
    SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(SqsAsyncClient sqsAsyncClient) {
        return SqsMessageListenerContainerFactory
                .builder()
                .configure(options -> options
                        .acknowledgementMode(AcknowledgementMode.MANUAL) // 명시적으로 acknowledgement를 해야 메시지가 삭제
                        .acknowledgementOrdering(AcknowledgementOrdering.ORDERED) // 순서대로 처리
                )
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }
}