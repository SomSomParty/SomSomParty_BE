package com.acc.somsomparty.domain.chatting.controller;

import com.acc.somsomparty.domain.chatting.dto.MessageDto;
import com.acc.somsomparty.domain.chatting.dto.MessageListResponse;
import com.acc.somsomparty.domain.chatting.dto.UserChatRoomListDto;
import com.acc.somsomparty.domain.chatting.entity.Message;
import com.acc.somsomparty.domain.chatting.entity.UserChatRoom;
import com.acc.somsomparty.domain.chatting.service.ChattingService;
import com.acc.somsomparty.domain.chatting.service.KafkaProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "chatting", description = "채팅 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/festivals/chatting")
public class ChattingController {
    private final ChattingService chattingService;

    @Operation(
            summary = "채팅방 진입 API",
            description = "채팅방에 진입하면 해당 채팅방의 메세지 내역을 페이징해 조회한다."
    )
    @Parameters({
            @Parameter(name = "chatRoomId", description = "채팅방 Id"),
            @Parameter(name = "lastEvaluatedSendTime", description = "ddb의 마지막 조회 키(sendTime)"),
            @Parameter(name = "limit", description = "페이지당 컨텐츠 개수"),
            @Parameter(name = "userId", description = "입장하는 회원 Id")
    })
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<MessageListResponse> getMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(required = false) Long lastEvaluatedSendTime,
            @RequestParam(defaultValue = "10") int limit) {
            MessageListResponse messages = chattingService.getMessages(chatRoomId, lastEvaluatedSendTime, limit);
            return ResponseEntity.ok(messages);
    }

    @Operation(
            summary = "채팅방 참여 API",
            description = "회원이 채팅방에 처음 참여하는 경우 참여 채팅방 목록에 추가한다."
    )
    @PostMapping("/{chatRoomId}/join")
    public ResponseEntity<Void> joinChatRoom(
            @PathVariable Long chatRoomId,
            @RequestParam Long userId) {
        chattingService.joinChatRoom(userId, chatRoomId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "참여중인 채팅방 목록 API",
            description = "회원이 참여 중인 채팅방 목록을 보내준다."
    )
    @GetMapping("/list/{userId}")
    public ResponseEntity<List<UserChatRoomListDto>> getChatRoomList(@PathVariable Long userId){
        return ResponseEntity.ok(chattingService.getUserChatRoomList(userId));
    }

    @Operation(
            summary = "채팅방 나가기 API",
            description = "회원이 참여중인 채팅방을 나가기"
    )
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> leaveChatRoom(@PathVariable Long userId, @RequestParam Long chatRoomId){
        chattingService.deleteUserChatRoom(userId, chatRoomId);
        return ResponseEntity.ok().build();
    }
}
