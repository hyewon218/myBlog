package com.sparta.myblog.dto;

import com.sparta.myblog.entity.ChatRoom;
import com.sparta.myblog.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "오픈채팅방 생성 및 수정 요청 DTO")
public class ChatRoomRequestDto {

    @Schema(description = "오픈채팅방 id", example = "36b8f84d-df4e-4d49-b662-bcde71a8764f")
    private String id;
    @Schema(description = "오픈채팅방 제목", example = "강아지 병원 정보 공유합니다.")
    private String title;
    @Schema(description = "오픈채팅방 설명", example = "서울 인천 경기 지역 분들을 위한 방입니다.")
    private String content;

    public ChatRoom toEntity(User user) {
        return ChatRoom.builder()
            .id(UUID.randomUUID().toString())
            .title(this.title)
            .content(this.content)
            .user(user)
            .build();
    }
}