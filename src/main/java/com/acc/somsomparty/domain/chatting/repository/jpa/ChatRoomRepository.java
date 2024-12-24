package com.acc.somsomparty.domain.chatting.repository.jpa;

import com.acc.somsomparty.domain.chatting.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
