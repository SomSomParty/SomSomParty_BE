package com.acc.somsomparty.domain.chatting.service;

import com.acc.somsomparty.domain.Festival.entity.Festival;
import com.acc.somsomparty.domain.User.entity.User;
import com.acc.somsomparty.domain.User.repository.UserRepository;
import com.acc.somsomparty.domain.chatting.dto.UserChatRoomListDto;
import com.acc.somsomparty.domain.chatting.dto.MessageListResponse;
import com.acc.somsomparty.domain.chatting.entity.ChatRoom;
import com.acc.somsomparty.domain.chatting.entity.Message;
import com.acc.somsomparty.domain.chatting.entity.UserChatRoom;
import com.acc.somsomparty.domain.chatting.repository.dynamodb.MessageRepository;
import com.acc.somsomparty.domain.chatting.repository.jpa.ChatRoomRepository;
import com.acc.somsomparty.domain.chatting.repository.jpa.UserChatRoomRepository;
import com.acc.somsomparty.global.exception.CustomException;
import com.acc.somsomparty.global.exception.error.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChattingService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishCreateChatRoom(Festival festival) {
        applicationEventPublisher.publishEvent(festival);
    }

    public void save(Message message) {
        try {
            messageRepository.save(message);
            log.info("메세지 저장: {}", message);
        } catch (Exception e) {
            log.error("메세지 저장 실패: {}, 예외: {}", message, e.getMessage(), e);
            throw new CustomException(ErrorCode.FAILED_MESSAGE_SAVE);
        }
    }

    public MessageListResponse getMessages(Long chatRoomId, Long lastEvaluatedSendTime, int limit) {
        try {
            QueryEnhancedRequest.Builder queryBuilder = QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional.keyEqualTo(Key.builder()
                            .partitionValue(chatRoomId)
                            .build()))
                    .limit(limit)
                    .scanIndexForward(false); // 최신 메시지부터 조회

            if (lastEvaluatedSendTime != null) {
                queryBuilder.exclusiveStartKey(Map.of(
                        "chatRoomId", AttributeValue.builder().n(chatRoomId.toString()).build(),
                        "sendTime", AttributeValue.builder().n(lastEvaluatedSendTime.toString()).build()
                ));
            }

            Page<Message> firstPage = messageRepository.query(queryBuilder.build()).iterator().next();

            List<Message> messages = firstPage.items();
            Long newLastEvaluatedSendTime = null;

            Map<String, AttributeValue> lastKey = firstPage.lastEvaluatedKey();
            if (lastKey != null && lastKey.containsKey("sendTime")) {
                newLastEvaluatedSendTime = Long.valueOf(lastKey.get("sendTime").n());
            }

            return new MessageListResponse(messages, newLastEvaluatedSendTime);

        }
        catch(Exception e){
                log.error("메세지 조회 실패: {}", e.getMessage(), e);
                throw new CustomException(ErrorCode.FAILED_MESSAGE_GET);
        }
    }

    @Transactional
    public void joinChatRoom(Long userId, Long chatRoomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

        if (userChatRoomRepository.existsByUserAndChatRoom(user, chatRoom)) {
            return;
        }

        UserChatRoom userChatRoom = UserChatRoom.builder()
                .user(user)
                .chatRoom(chatRoom)
                .build();

        userChatRoomRepository.save(userChatRoom);
    }

    public List<UserChatRoomListDto> getUserChatRoomList(Long userId) {
        List<UserChatRoom> userChatRooms = userChatRoomRepository.findByUserId(userId);
        return userChatRooms.stream()
                .map(userChatRoom -> new UserChatRoomListDto(
                        userChatRoom.getChatRoom().getName(),
                        (long) userChatRoom.getChatRoom().getUserChatRooms().size()
                ))
                .collect(Collectors.toList());
    }
    
    public void deleteUserChatRoom(Long userId, Long chatRoomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

        UserChatRoom userChatRoom = userChatRoomRepository.findByUserAndChatRoom(user, chatRoom)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_CHATROOM_NOT_FOUND));
        userChatRoomRepository.delete(userChatRoom);
    }

    /**
     * TODO: 웹소켓 기반의 실시간 삭제
     */
}
