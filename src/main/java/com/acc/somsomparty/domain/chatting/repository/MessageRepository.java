package com.acc.somsomparty.domain.chatting.repository;

import com.acc.somsomparty.domain.chatting.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
