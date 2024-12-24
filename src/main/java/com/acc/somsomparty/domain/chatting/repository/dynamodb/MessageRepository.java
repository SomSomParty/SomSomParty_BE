package com.acc.somsomparty.domain.chatting.repository.dynamodb;

import com.acc.somsomparty.domain.chatting.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

@Repository
@RequiredArgsConstructor
public class MessageRepository {

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private static final String TABLE_NAME = "Message";

    /**
     * DynamoDbTable 인스턴스를 반환
     */
    private DynamoDbTable<Message> getTable() {
        return dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(Message.class));
    }

    /**
     * 메시지 저장
     */
    public void save(Message message) {
        getTable().putItem(message);
    }

    /**
     * 메시지 조회 (페이징 포함)
     */
    public SdkIterable<Page<Message>> query(QueryEnhancedRequest request) {
        return getTable().query(request);
    }

    /**
     * 메시지 단일 조회
     */
    public Message getItem(Long chatRoomId, Long sendTime) {
        Key key = Key.builder()
                .partitionValue(chatRoomId)
                .sortValue(sendTime)
                .build();
        return getTable().getItem(r -> r.key(key));
    }

    /**
     * 메시지 삭제
     */
    public void delete(Message message) {
        getTable().deleteItem(message);
    }
}