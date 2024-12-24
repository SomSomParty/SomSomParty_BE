package com.acc.somsomparty.domain.chatting.dto;

import com.acc.somsomparty.domain.chatting.entity.Message;
import java.util.List;
import lombok.Builder;

@Builder
public record MessageListResponse(
        List<Message> messages,
        Long lastEvaluatedSendTime) {
}
