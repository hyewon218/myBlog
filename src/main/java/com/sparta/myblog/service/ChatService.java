package com.sparta.myblog.service;


import com.sparta.myblog.dto.ChatListResponseDto;
import com.sparta.myblog.dto.ChatMessageDto;

public interface ChatService {

    /**
     * 채팅방에 메시지 발송
     *
     * @param roomId 발송할 오픈채팅 방 ID
     */
    void sendChatMessage(Long roomId, ChatMessageDto requestDto);

    /**
     * 오픈채팅방 내 채팅 목록 조회
     *
     * @param id 조회할 오픈채팅 방 ID
     * @return 조회된 메세지 목록
     */
    ChatListResponseDto getAllChatByRoomId(Long id);

    /**
     * 오픈채팅 메세지 저장
     *
     * @param roomId     저장할 오픈채팅 방 ID
     * @param requestDto 메세지 저장 요청정보
     */
    void saveMessage(Long roomId, ChatMessageDto requestDto);
}