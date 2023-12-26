package com.sparta.myblog.service;

import com.sparta.myblog.dto.ChatRoomListResponseDto;
import com.sparta.myblog.dto.ChatRoomRequestDto;
import com.sparta.myblog.dto.ChatRoomResponseDto;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.security.UserDetailsImpl;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ChatRoomService {

    /**
     * 오픈채팅방 목록 조회
     *
     * @return 조회된 오픈채팅방 목록
     */
    ChatRoomListResponseDto getOpenChatRooms();

    /**
     * 나의 오픈채팅방 목록 조회
     *
     * @return 조회된 나의 오픈채팅방 목록
     */
    ChatRoomListResponseDto getMyOpenChatRooms(UserDetailsImpl userDetails);

    /**
     * 오픈채팅방 조회
     *
     * @param id        조회할 오픈채팅방 id
     * @return          조회된 오픈채팅방
     */
    ChatRoomResponseDto getOpenChatRoom(String id);

    /**
     * 오픈채팅방 생성
     *
     * @param requestDto 오픈채팅방 저장 요청정보
     * @param user       오픈채팅방 생성 요청자
     * @param files       오픈채팅방 생성 첨부 파일
     */
    void createOpenChatRoom(ChatRoomRequestDto requestDto, User user, List<MultipartFile> files) throws IOException;

    /**
     * 오픈채팅방 제목 수정
     *
     * @param id         수정할 오픈채팅방 id
     * @param requestDto 오픈채팅방 수정 요청정보
     * @param user       오픈채팅방 수정 요청자
     */
    void updateOpenChatRoom(String id, ChatRoomRequestDto requestDto, User user, List<MultipartFile> files) throws IOException;


    /**
     * 오픈채팅방 삭제
     *
     * @param id   삭제할 채팅방 id
     * @param user 오픈채팅방 삭제 요청자
     */
    // 채팅방 삭제
    void deleteChatRoom(String id, User user);

}