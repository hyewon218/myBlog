package com.sparta.myblog.controller;

import com.sparta.myblog.dto.ChatRoomResponseDto;
import com.sparta.myblog.security.UserDetailsImpl;
import com.sparta.myblog.service.ChatRoomService;
import com.sparta.myblog.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ChatRoomViewController {

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;

    // 채팅방 목록
    @GetMapping("/openchat")
    public String openChatList(Model model) {
        model.addAttribute("chatRoomList", chatRoomService.getOpenChatRooms());
        return "openChat";
    }

    @GetMapping("/openchat/{chatId}")
    public String getChat(@PathVariable Long chatId, Model model) {
        ChatRoomResponseDto chatRoomResponseDto = chatRoomService.getOpenChatRoom(chatId);
        model.addAttribute("chat", chatRoomResponseDto);
        return "chat";
    }

    @GetMapping("/openchat/create")
    public String createPost(@RequestParam(required = false) Long chatId, Model model, @AuthenticationPrincipal
    UserDetailsImpl userDetails, RedirectAttributes rttr) {
        if(userDetails == null){
            rttr.addFlashAttribute("result", "로그인이 필요합니다.");
            return "redirect:/openchat";
        }else {
            if(chatId==null){
                model.addAttribute("chat",new ChatRoomResponseDto());
            } else {
                ChatRoomResponseDto chatRoomResponseDto = chatRoomService.getOpenChatRoom(chatId);
                model.addAttribute("chat", chatRoomResponseDto);
            }
            return "createChat";
        }
    }

    // 채팅방
    @GetMapping("/openchat/room")
    public String openChatList(@RequestParam(required=false) Long chatId, Model model) {
        model.addAttribute("chatList", chatService.getAllChatByRoomId(chatId));
        model.addAttribute("chatRoom", chatRoomService.getOpenChatRoom(chatId));
        return "openChatRoom";
    }

    @GetMapping("/myopenchat")
    public String myopenChatList(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        model.addAttribute("chatRoomList", chatRoomService.getmyOpenChatRooms(userDetails));
        return "myopenChat";
    }

    @GetMapping("/myopenchat/{chatId}")
    public String getmyChat(@PathVariable Long chatId, Model model) {
        ChatRoomResponseDto chatRoomResponseDto = chatRoomService.getOpenChatRoom(chatId);
        model.addAttribute("chat", chatRoomResponseDto);
        return "mychat";
    }
}
