package com.sparta.myblog.dto;

import com.sparta.myblog.entity.Chat;
import com.sparta.myblog.entity.ChatRoom;
import com.sparta.myblog.entity.ChatType;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto implements Serializable {

    private ChatType type; // 메시지 타입
    private Long roomId;
    private String sender; // 메시지 보낸사람
    private String message;

    public Chat toEntity(ChatRoom chatRoom) { // 오픈채팅
        return Chat.builder()
            .chatRoom(chatRoom)
            .sender(this.sender)
            .message(this.message)
            .type(this.type)
            .build();
    }
}