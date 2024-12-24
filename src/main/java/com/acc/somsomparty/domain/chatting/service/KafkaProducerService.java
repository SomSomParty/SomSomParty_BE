package com.acc.somsomparty.domain.chatting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendMessage(String topic, String key, Object message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message); // JSON 직렬화
            kafkaTemplate.send(topic, key, jsonMessage);
            log.info("토픽으로 메세지를 전송함. 토픽: {}, 키: {}, 메세지: {}", topic, key, jsonMessage);
        } catch (Exception e) {
            log.error("메시지 직렬화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("메시지 직렬화 실패", e);
        }
    }
}