package com.acc.somsomparty.domain.chatting.service;

import com.acc.somsomparty.domain.chatting.dto.ChatRoomCreateEvent;
import com.acc.somsomparty.domain.chatting.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChattingService {

    private final ChatRoomRepository chatRoomRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishCreateChatRoom(Long festivalId, String festivalName){
        ChatRoomCreateEvent event = ChatRoomCreateEvent.builder()
                .festivalId(festivalId)
                .festivalName(festivalName)
                .build();
        applicationEventPublisher.publishEvent(event);
    }
}
