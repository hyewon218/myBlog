package com.sparta.myblog.controller;

import com.sparta.myblog.exception.ApiResponseDto;
import com.sparta.myblog.dto.CommentRequestDto;
import com.sparta.myblog.dto.CommentResponseDto;
import com.sparta.myblog.security.UserDetailsImpl;
import com.sparta.myblog.service.CommentServiceImpl;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/view")
@RequiredArgsConstructor
public class CommentController {

  private final CommentServiceImpl commentService;

  // 댓글 작성
  @PostMapping("/comments")
  public String createComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestBody CommentRequestDto requestDto) {
    commentService.createComment(requestDto, userDetails.getUser());
    return "redirect:/view/post/{id}";
  }

  // 댓글 수정
  @PutMapping("/comments/{id}")
  @ResponseBody
  public String updateComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
      @PathVariable Long id, @RequestBody CommentRequestDto requestDto, Model model) {
    CommentResponseDto dto = commentService.updateComment(id, requestDto, userDetails.getUser());
    model.addAttribute("comment", dto);
    return "redirect:/detail_page";
  }

  // 댓글 삭제
  @DeleteMapping("/comments/{id}")
  public Long deleteComment(@PathVariable Long id,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    commentService.deleteComment(id, userDetails.getUser());

    return id;
  }

  // 댓글 좋아요
  @PostMapping("/comments/{id}/like")
  public ResponseEntity<ApiResponseDto> likeComment(
      @AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long id) {
    try {
      commentService.likeComment(id, userDetails.getUser());
    } catch (DuplicateRequestException e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponseDto(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(new ApiResponseDto("댓글 좋아요 성공", HttpStatus.ACCEPTED.value()));
  }

  // 댓글 좋아요 취소
  @DeleteMapping("/comments/{id}/like")
  public ResponseEntity<ApiResponseDto> dislikeComment(
      @AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long id) {
    try {
      commentService.dislikeComment(id, userDetails.getUser());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponseDto(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(new ApiResponseDto("댓글 좋아요 취소 성공", HttpStatus.ACCEPTED.value()));
  }
}



