package com.acc.somsomparty.domain.chatting.service;

import com.acc.somsomparty.domain.chatting.dto.MessageDto;
import com.acc.somsomparty.domain.chatting.entity.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {
    private final SimpMessagingTemplate messagingTemplate; // STOMP 메시지 전송 객체
    private final ChattingService chattingService;
    private final ObjectMapper objectMapper;

    /**
     * @param record: @KafkaListener 의 기본 동작 방식에서는 메서드 파라미터로 메시지와 Key 를 직접 분리해서 읽을 수 없다.
     *                대신, Kafka 의 ConsumerRecord 를 활용하면 메시지와 Key 를 동시에 처리할 수 있다.
     */
    @KafkaListener(topics = "chat-topic", groupId = "consumer-group-websocket")
    public void websocketConsumer(ConsumerRecord<String, String> record) {
        String key = record.key();
        String message = record.value();

        MessageDto chatMessage = parseMessage(message);

        if (chatMessage == null) {
            log.warn("잘못된 메시지를 무시합니다.");
            return;
        }

        // WebSocket 구독자들에게 메시지 전달
        messagingTemplate.convertAndSend("/topic/chat/" + key, chatMessage);
        log.info("WebSocket 구독자들에게 메세지 전달 완료");
    }

    /**
     * DB 저장용 Consumer Group 을 추가해 웹소켓 실시간 통신과 분리해 처리한다.
     * 실시간 통신이 DB 작업을 기다려선 안되기 때문이다.
     */
    @KafkaListener(topics = "chat-topic", groupId = "consumer-group-db")
    public void dbConsumer(String message) {

        MessageDto chatMessage = parseMessage(message);

        if (chatMessage == null) {
            log.warn("잘못된 메시지를 무시합니다.");
            return;
        }

        Message messageEntity = Message.builder()
                .senderId(chatMessage.senderId())
                .senderName(chatMessage.senderName())
                .chatRoomId(chatMessage.chatRoomId())
                .content(chatMessage.content())
                .sendTime(chatMessage.createdAt().toEpochSecond(ZoneOffset.UTC))
                .build();

        //DynamoDB 저장
        chattingService.save(messageEntity);
        log.info("메세지 DynamoDB 저장 완료: {}",messageEntity);
    }

    private MessageDto parseMessage(String message) {
        try {
            return objectMapper.readValue(message, MessageDto.class);
        } catch (Exception e) {
            log.error("Kafka 메시지 역직렬화 실패 - 메시지: {}", message, e);
            return null;
        }
    }
}
