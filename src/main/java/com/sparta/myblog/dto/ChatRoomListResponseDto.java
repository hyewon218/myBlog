package com.sparta.myblog.dto;

import com.sparta.myblog.entity.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(description = "오픈채팅방 목록 조회 응답 DTO")
public class ChatRoomListResponseDto {

    List<ChatRoomResponseDto> chatRoomList;

    public static ChatRoomListResponseDto of(List<ChatRoom> chats) {
        List<ChatRoomResponseDto> chatRoomResponseDtoList = chats.stream()
            .map(ChatRoomResponseDto::of)
            .toList();
        return ChatRoomListResponseDto.builder()
            .chatRoomList(chatRoomResponseDtoList)
            .build();
    }
}