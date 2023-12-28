package com.sparta.myblog.controller;

import com.sparta.myblog.dto.ChatMessageDto;
import com.sparta.myblog.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    // stompConfig 에서 설정한 applicationDestinationPrefixes 와 @MessageMapping 경로가 병합됨 (/pub + ...)
    // /pub/chat/message 에 메세지가 오면 동작
    // 채팅방에 발행된 메시지는 서로 다른 서버에 공유하기 위해 redis 의 Topic 으로 발행
    @MessageMapping("chat/message/{roomId}") // 오픈채팅
    @SendTo("/sub/chat/{roomId}")
    // 메세징 요청을 보낼 때에는 @MessageMapping 어노테이션을 사용
    // HTTP Method 매핑의 경우 Parameter 값을 받아오기 위해서 @PathVariable을 사용하였지만, 메세징의 경우 @DestinationVariable을 사용
    public ChatMessageDto message(@DestinationVariable String roomId, ChatMessageDto messageDto) {

        chatService.sendChatMessage(roomId, messageDto);

        return ChatMessageDto.builder()
            .roomId(roomId)
            .sender(messageDto.getSender())
            .message(messageDto.getMessage())
            .build();
    }
}