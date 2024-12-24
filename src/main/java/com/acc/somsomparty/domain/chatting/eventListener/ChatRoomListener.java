package com.acc.somsomparty.domain.chatting.eventListener;

import com.acc.somsomparty.domain.Festival.entity.Festival;
import com.acc.somsomparty.domain.chatting.entity.ChatRoom;
import com.acc.somsomparty.domain.chatting.repository.jpa.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatRoomListener {
    private final ChatRoomRepository chatRoomRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createChatRoom(Festival festival) {
        ChatRoom chatRoom = new ChatRoom(festival.getName(),festival.getId());
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        festival.addChatRoom(savedChatRoom);
        log.info("채팅방 생성, 채팅방 아이디: {}", savedChatRoom.getId());
    }

}
