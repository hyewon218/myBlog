package com.sparta.myblog.controller;

import com.sparta.myblog.dto.ApiResponseDto;
import com.sparta.myblog.dto.CommentRequestDto;
import com.sparta.myblog.dto.CommentResponseDto;
import com.sparta.myblog.security.UserDetailsImpl;
import com.sparta.myblog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/comment")
    public CommentResponseDto createComment(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody CommentRequestDto commentRequestDto) {
        return commentService.createComment(commentRequestDto, userDetails.getUser());
    }

    // 댓글 수정
    @PutMapping("/comment/{id}")
    public CommentResponseDto updateComment(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long id, @RequestBody CommentRequestDto commentRequestDto) {
        return commentService.updateComment(id, commentRequestDto, userDetails.getUser());
    }

    // 댓글 삭제
    @DeleteMapping("/comment/{id}")
    public ApiResponseDto deleteComment(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long id) {
        return commentService.deleteComment(id, userDetails.getUser());
    }
}
