package com.sparta.myblog.controller;

import com.sparta.myblog.dto.ApiResult;
import com.sparta.myblog.dto.CommentRequestDto;
import com.sparta.myblog.dto.CommentResponseDto;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/comment")
    public CommentResponseDto createComment(@RequestBody CommentRequestDto commentRequestDto, User user) {
        return commentService.createComment(commentRequestDto, user);
    }

    // 댓글 수정
    @PutMapping("/comment/{id}")
    public CommentResponseDto updateComment(@PathVariable Long id, @RequestBody CommentRequestDto commentRequestDto, User user) {
        return commentService.updateComment(id, commentRequestDto, user);

    }

    // 댓글 삭제
    @DeleteMapping("/comment/{id}")
    public ApiResult deleteComment(@PathVariable Long id, User user) {
        return commentService.deleteComment(id, user);
    }
}
