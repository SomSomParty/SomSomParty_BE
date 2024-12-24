package com.acc.somsomparty.domain.chatting.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.acc.somsomparty.domain.chatting.dto.MessageDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class KafkaProducerServiceTest {

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Test
    public void testSendMessage() {
        String topic = "chat-topic";
        String key = "chatRoomId1";
        MessageDto message = MessageDto.builder().chatRoomId(1L).content("Hello").userName("user").build();

        // when
        kafkaProducerService.sendMessage(topic, key, message);

        // then
        verify(kafkaTemplate, times(1)).send(eq(topic), eq(key), any(String.class));
    }
}