package com.acc.somsomparty.domain.chatting.service;

import com.acc.somsomparty.domain.chatting.dto.ChatRoomCreateEvent;
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

    public List<Message> getMessages(Long chatRoomId, Long lastEvaluatedSendTime, int limit) {
        try {
            QueryEnhancedRequest.Builder queryBuilder = QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional.keyEqualTo(Key.builder()
                            .partitionValue(chatRoomId)
                            .build()))
                    .limit(limit) // 한 번에 조회할 메시지 개수
                    .scanIndexForward(false); // 최신 메시지부터 조회

            // 커서가 존재하면 exclusiveStartKey 설정
            if (lastEvaluatedSendTime != null) {
                queryBuilder.exclusiveStartKey(Map.of(
                        "chatRoomId", AttributeValue.builder().n(chatRoomId.toString()).build(),
                        "sendTime", AttributeValue.builder().n(lastEvaluatedSendTime.toString()).build()
                ));
            }

            SdkIterable<Page<Message>> pages = messageRepository.query(queryBuilder.build());
            log.info("메세지 조회 완료: {}", pages);
            return pages.stream()
                    .flatMap(page -> page.items().stream()) // 각 페이지에서 메시지를 가져옴
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("메세지 조회 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.FAILED_MESSAGE_GET);
        }
    }

    /**
     * TODO: 웹소켓 기반의 실시간 삭제
     */


}