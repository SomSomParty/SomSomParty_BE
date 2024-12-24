package com.acc.somsomparty.domain.chatting.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.acc.somsomparty.domain.chatting.dto.MessageDto;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class KafkaConsumerServiceTest {

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private KafkaConsumerService kafkaConsumerService;

    @Test
    public void testConsume() {
        String key = "chatRoomId1";
        String message = "{\"chatRoomId\":1,\"content\":\"Hello!\"}";

        ConsumerRecord<String, String> record = new ConsumerRecord<>("chat-topic", 0, 0L, key, message);

        // when
        kafkaConsumerService.consume(record);

        // then
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/chat/" + key), any(MessageDto.class)); // 매처를 일관되게 사용
    }
}