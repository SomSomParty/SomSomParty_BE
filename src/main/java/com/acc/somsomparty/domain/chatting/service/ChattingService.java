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
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
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
@Transactional
public class ChattingService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;

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

            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

            return new MessageListResponse(chatRoom.getName(),messages, newLastEvaluatedSendTime);

        }
        catch(Exception e){
                log.error("메세지 조회 실패: {}", e.getMessage(), e);
                throw new CustomException(ErrorCode.FAILED_MESSAGE_GET);
        }
    }

    @Transactional
    public Long joinChatRoom(Long userId, Long chatRoomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));


        if (userChatRoomRepository.existsByUserAndChatRoom(user, chatRoom)) {
            return userChatRoomRepository.findByUserAndChatRoom(user,chatRoom).get().getChatRoom().getId();
        }

        UserChatRoom userChatRoom = UserChatRoom.builder()
                .user(user)
                .chatRoom(chatRoom)
                .build();

        UserChatRoom savedUserChatRoom = userChatRoomRepository.save(userChatRoom);
        // Redis에서 채팅방 참여자 목록 관리
        redisTemplate.opsForSet().add("chatRoom:participants:" + chatRoomId,userId.toString());
        return savedUserChatRoom.getChatRoom().getId();
    }

    public List<UserChatRoomListDto> getUserChatRoomList(Long userId) {
        List<UserChatRoom> userChatRooms = userChatRoomRepository.findByUserId(userId);
        return userChatRooms.stream()
                .map(userChatRoom -> new UserChatRoomListDto(
                        userChatRoom.getChatRoom().getId(),
                        userChatRoom.getChatRoom().getName(),
                        (long) userChatRoom.getChatRoom().getUserChatRooms().size(),
                        (Integer) redisTemplate.opsForHash().get("chatRoom:unreadCount",userChatRoom.getChatRoom().getId()+":"+userId)
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
        redisTemplate.opsForSet().remove("chatRoom:participants:" + chatRoomId,userId.toString());
    }

    public void incrementUnreadCount(String key) {
        String chatRoomId = key.replaceAll("\\D+", "");
        String participantsKey = "chatRoom:participants:" + chatRoomId;
        String activeUsersKey = "chatRoom:activeUsers:" + chatRoomId;
        String unreadCountKey = "chatRoom:unreadCount";

        // 모든 참여 사용자 가져오기
        Set<Object> allUsers = redisTemplate.opsForSet().members(participantsKey);
        if (allUsers == null || allUsers.isEmpty()) {
            return; // 참여자가 없는 경우 종료
        }

        // 현재 활성 사용자 가져오기
        Set<Object> activeUsers = redisTemplate.opsForSet().members(activeUsersKey);

        // 비활성 사용자 추출
        allUsers.forEach(userId -> {
            if (activeUsers == null || !activeUsers.contains(userId)) {
                // 비활성 사용자에 대해 읽지 않은 메시지 개수 증가
                String redisField = chatRoomId + ":" + userId;
                redisTemplate.opsForHash().increment(unreadCountKey, redisField, 1);
                log.info("ChatRoom {} 의 안 읽은 메세지 개수 갱신", chatRoomId);
            }
        });
    }

    /**
     * TODO: 웹소켓 기반의 실시간 삭제
     */
}
