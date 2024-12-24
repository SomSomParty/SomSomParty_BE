package com.acc.somsomparty.domain.chatting.service;

import com.acc.somsomparty.domain.chatting.dto.ChatRoomCreateEvent;
import com.acc.somsomparty.domain.chatting.dto.MessageListResponse;
import com.acc.somsomparty.domain.chatting.entity.Message;
import com.acc.somsomparty.domain.chatting.repository.dynamodb.MessageRepository;
import com.acc.somsomparty.global.exception.CustomException;
import com.acc.somsomparty.global.exception.error.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
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
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishCreateChatRoom(Long festivalId, String festivalName){
        ChatRoomCreateEvent event = ChatRoomCreateEvent.builder()
                .festivalId(festivalId)
                .festivalName(festivalName)
                .build();
        applicationEventPublisher.publishEvent(event);
    }

    public void saveMessage(Message message) {
        try {
            messageRepository.save(message);
            log.info("메세지 저장: {}", message);
        } catch (Exception e) {
            log.error("메세지 저장 실패: {}", message);
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

            SdkIterable<Page<Message>> pages = messageRepository.query(queryBuilder.build());

            List<Message> messages = new ArrayList<>();
            Long newLastEvaluatedSendTime = null;

            for (Page<Message> page : pages) {
                messages.addAll(page.items());
                // 마지막 키를 페이지의 마지막으로 갱신
                Map<String, AttributeValue> lastKey = page.lastEvaluatedKey();
                if (lastKey != null && lastKey.containsKey("sendTime")) {
                    newLastEvaluatedSendTime = Long.valueOf(lastKey.get("sendTime").n());
                }
            }

            return new MessageListResponse(messages, newLastEvaluatedSendTime);

        } catch (Exception e) {
            log.error("메세지 조회 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.FAILED_MESSAGE_GET);
        }
    }

    /**
     * TODO: 웹소켓 기반의 실시간 삭제
     */
}
