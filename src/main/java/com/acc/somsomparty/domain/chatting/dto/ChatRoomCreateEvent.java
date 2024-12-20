package com.acc.somsomparty.domain.chatting.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
public record ChatRoomCreateEvent(
        Long festivalId,
        String festivalName
) {
}
