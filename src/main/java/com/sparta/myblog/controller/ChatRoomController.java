package com.sparta.myblog.controller;

import com.sparta.myblog.dto.ChatListResponseDto;
import com.sparta.myblog.dto.ChatRoomListResponseDto;
import com.sparta.myblog.dto.ChatRoomRequestDto;
import com.sparta.myblog.dto.ChatRoomResponseDto;
import com.sparta.myblog.exception.ApiResponseDto;
import com.sparta.myblog.security.UserDetailsImpl;
import com.sparta.myblog.service.ChatRoomService;
import com.sparta.myblog.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "채팅방 관련 API", description = "채팅방 관련 API 입니다.")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;

    @GetMapping("/openchat")
    @Operation(summary = "오픈채팅방 목록 조회")
    public ResponseEntity<ChatRoomListResponseDto> getOpenChatRooms() {
        return ResponseEntity.ok(chatRoomService.getOpenChatRooms());
    }
    @GetMapping("/openchat/{id}")
    @Operation(summary = "오픈채팅방 목록 조회")
    public ResponseEntity<ChatRoomResponseDto> getOpenChatRoom(@PathVariable Long id) {
        return ResponseEntity.ok(chatRoomService.getOpenChatRoom(id));
    }

    @PostMapping("/openchat")
    @Operation(summary = "오픈채팅방 생성")
    public ResponseEntity<ApiResponseDto> createOpenChatRoom(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @ModelAttribute(value = "requestDto") ChatRoomRequestDto requestDto,
        @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files) throws IOException {
        chatRoomService.createOpenChatRoom(requestDto,
            userDetails.getUser(),files);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponseDto("오픈채팅방 생성 성공", HttpStatus.CREATED.value()));
    }

    @PutMapping("/openchat/{id}")
    @Operation(summary = "오픈채팅방 수정", description = "@PathVariable 을 통해 오픈채팅방 id를 받아와, 해당 오픈채팅방의 제목 및 설명을 수정합니다.")
    public ResponseEntity<ApiResponseDto> updateContent(
        @Parameter(name = "roomId", description = "특정 채팅방 id", in = ParameterIn.PATH) @PathVariable Long id,
        @ModelAttribute(value = "requestDto") ChatRoomRequestDto requestDto,
        @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files,
        @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        chatRoomService.updateOpenChatRoom(id, requestDto,
            userDetails.getUser(), files);
        return ResponseEntity.ok().body(new ApiResponseDto("오픈채팅방 수정 성공", HttpStatus.OK.value()));
    }

    @DeleteMapping("/openchat/{id}")
    @Operation(summary = "오픈채팅방 삭제", description = "@PathVariable 을 통해 오픈채팅방 Id를 받아와, 해당 오픈채팅방을 삭제합니다.")
    public ResponseEntity<ApiResponseDto> deleteChatRoom(
        @Parameter(name = "id", description = "특정 채팅방 id", in = ParameterIn.PATH) @PathVariable Long id,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        chatRoomService.deleteChatRoom(id, userDetails.getUser());
        return ResponseEntity.ok()
            .body(new ApiResponseDto("채팅방 삭제 성공", HttpStatus.OK.value()));

    }

    @GetMapping("/openchat/room/{id}")
    @Operation(summary = "오픈채팅방 내 채팅 목록 조회", description = "@PathVariable 을 통해 채팅방 id를 받아와, 해당 오픈채팅방에 존재하는 채팅 목록을 조회합니다.")
    public ResponseEntity<ChatListResponseDto> getAllChatByRoomId(
        @Parameter(name = "roomId", description = "특정 채팅방 id", in = ParameterIn.PATH) @PathVariable Long id) {
        return ResponseEntity.ok(chatService.getAllChatByRoomId(id));
    }
}