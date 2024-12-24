package com.acc.somsomparty.domain.chatting.dto;

import lombok.Builder;

@Builder
public record UserChatRoomListDto(
        String title,
        Long userCount
) {
}
