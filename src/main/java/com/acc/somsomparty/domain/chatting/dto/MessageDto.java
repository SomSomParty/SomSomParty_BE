package com.acc.somsomparty.domain.chatting.dto;


import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record MessageDto(
        Long chatRoomId,
        Long senderId,
        String senderName,
        LocalDateTime createdAt,
        String content
) {
}
