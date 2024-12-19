package com.acc.somsomparty.domain.Queue.config;

import io.awspring.cloud.sqs.operations.SendResult;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsSender {
    @Value("${spring.cloud.aws.sqs.queue-name}")
    private String queueName;
    private final SqsTemplate sqsTemplate;

    // 메세지 발행
    public SendResult<String> send(String payload) {
        try {
            log.info("sqs에 대기열 입장 메세지 보냈음.");
            return sqsTemplate.send(queueName, MessageBuilder.withPayload(payload).build());
        } catch (Exception e) {
            log.error("Failed to send message to SQS: {}", e.getMessage());
            throw e; // 예외를 다시 던져서 처리할 수 있도록 함
        }
    }
}