package com.sparta.myblog.controller;

import com.sparta.myblog.dto.ChatRoomResponseDto;
import com.sparta.myblog.security.UserDetailsImpl;
import com.sparta.myblog.service.ChatRoomService;
import com.sparta.myblog.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/view")
@RequiredArgsConstructor
@Tag(name = "채팅방 관련 API", description = "채팅방 관련 API 입니다.")
public class ChatRoomViewController {

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;

    // 채팅방 목록
    @GetMapping("/openChat")
    @Operation(summary = "오픈채팅방 목록 조회")
    public String getOpenChatRooms(Model model) {
        model.addAttribute("chatRoomList", chatRoomService.getOpenChatRooms());
        return "openChatList";
    }

    // 채팅방 조회
    @GetMapping("/openChat/{chatRoomId}")
    @Operation(summary = "오픈채팅방 조회")
    public String getOpenChatRoom(@PathVariable String chatRoomId, Model model) {
        ChatRoomResponseDto chatRoomResponseDto = chatRoomService.getOpenChatRoom(chatRoomId);
        model.addAttribute("chatRoom", chatRoomResponseDto);
        return "openChat";
    }

    @GetMapping("/openChat/create")
    @Operation(summary = "오픈채팅방 생성 페이지")
    public String createOpenChatRoom(@RequestParam(required = false) String chatRoomId,
        Model model, @AuthenticationPrincipal
    UserDetailsImpl userDetails, RedirectAttributes rttr) {
        if (userDetails == null) {
            rttr.addFlashAttribute("result", "로그인이 필요합니다.");
            return "redirect:/openChat";
        } else {
            if (chatRoomId == null) {
                model.addAttribute("chatRoom", new ChatRoomResponseDto());
            } else {
                ChatRoomResponseDto chatRoomResponseDto = chatRoomService.getOpenChatRoom(
                    chatRoomId);
                model.addAttribute("chatRoom", chatRoomResponseDto);
            }
            return "createOpenChatRoom";
        }
    }

    @GetMapping("/openChat/room")
    @Operation(summary = "오픈채팅방 내 채팅 목록 조회", description = "@PathVariable 을 통해 채팅방 id를 받아와, 해당 오픈채팅방에 존재하는 채팅 목록을 조회합니다.")
    public String getAllChatByRoomId(@RequestParam(required = false) String chatRoomId,
        Model model) {
        model.addAttribute("chatList", chatService.getAllChatByRoomId(chatRoomId));
        model.addAttribute("chatRoom", chatRoomService.getOpenChatRoom(chatRoomId));
        return "openChatRoom";
    }

    // 나의 채팅방 목록
    @GetMapping("/myOpenChat")
    public String getMyOpenChatRooms(Model model,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        model.addAttribute("chatRoomList", chatRoomService.getMyOpenChatRooms(userDetails));
        return "myOpenChatList";
    }

    @GetMapping("/myOpenChat/{chatRoomId}")
    public String getMyOpenChatRoom(@PathVariable String chatRoomId, Model model) {
        ChatRoomResponseDto chatRoomResponseDto = chatRoomService.getOpenChatRoom(chatRoomId);
        model.addAttribute("chat", chatRoomResponseDto);
        return "myOpenChat";
    }
}
