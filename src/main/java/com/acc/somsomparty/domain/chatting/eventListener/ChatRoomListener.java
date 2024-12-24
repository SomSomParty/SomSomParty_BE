package com.acc.somsomparty.domain.chatting.eventListener;

import com.acc.somsomparty.domain.chatting.dto.ChatRoomCreateEvent;
import com.acc.somsomparty.domain.chatting.entity.ChatRoom;
import com.acc.somsomparty.domain.chatting.repository.jpa.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatRoomListener {
    private final ChatRoomRepository chatRoomRepository;

    /**
     * @param event : 축제 생성 dto
     * 축제 생성과 채팅방 생성을 한 트랜잭션으로 유지해 일관성을 보장한다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createChatRoom(ChatRoomCreateEvent event) {
        ChatRoom chatRoom = new ChatRoom(event.festivalId(),event.festivalName());
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        log.info("채팅방 생성, 채팅방 아이디: {}", savedChatRoom.getId());
    }

}
