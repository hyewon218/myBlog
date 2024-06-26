package com.sparta.myblog.dto;

import com.sparta.myblog.entity.Chat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(description = "오픈채팅방 내 채팅 목록 조회 응답 DTO")
public class ChatListResponseDto {

    private List<ChatResponseDto> chatList;

    public static ChatListResponseDto of(List<Chat> chats) {
        List<ChatResponseDto> chatResponseDtoList = chats.stream().map(ChatResponseDto::of)
            .toList();
        return ChatListResponseDto.builder()
            .chatList(chatResponseDtoList)
            .build();
    }
}