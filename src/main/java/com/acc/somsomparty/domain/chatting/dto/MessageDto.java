package com.acc.somsomparty.domain.chatting.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record MessageDto(
        @NotNull
        Long chatRoomId,
        Long senderId,
        String senderName,
        @NotNull
        LocalDateTime createdAt,
        String content
) {
}
