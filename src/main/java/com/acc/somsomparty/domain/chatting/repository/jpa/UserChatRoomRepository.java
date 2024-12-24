package com.acc.somsomparty.domain.chatting.repository.jpa;

import com.acc.somsomparty.domain.User.entity.User;
import com.acc.somsomparty.domain.chatting.entity.ChatRoom;
import com.acc.somsomparty.domain.chatting.entity.UserChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom,Long> {
    boolean existsByUserAndChatRoom(User user, ChatRoom chatRoom);

    List<UserChatRoom> findByUserId(Long userId);

    Optional<UserChatRoom> findByUserAndChatRoom(User user, ChatRoom chatRoom);
}
