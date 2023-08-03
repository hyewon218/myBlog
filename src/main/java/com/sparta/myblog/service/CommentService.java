package com.sparta.myblog.service;

import com.sparta.myblog.dto.CommentRequestDto;
import com.sparta.myblog.dto.CommentResponseDto;
import com.sparta.myblog.entity.*;

public interface CommentService {

  /**
   * 댓글 생성
   *
   * @param commentRequestDto 댓글 생성 요청정보
   * @param user              댓글 생성 요청자
   * @return 댓글 생성 결과
   */
  CommentResponseDto createComment(CommentRequestDto commentRequestDto, User user);

  /**
   * 댓글 수정
   *
   * @param commentId         수정할 댓글 ID
   * @param commentRequestDto 수정할 댓글 정보
   * @param user              댓글 수정 요청자
   * @return 수정된 댓글 정보
   */
  CommentResponseDto updateComment(Long commentId, CommentRequestDto commentRequestDto, User user);

  /**
   * 댓글 삭제
   *
   * @param commentId 삭제 요청 댓글 ID
   * @param user      댓글 삭제 요청자
   */
  void deleteComment(Long commentId, User user);

  /**
   * 댓글 좋아요
   *
   * @param id   좋아요 요청 댓글 ID
   * @param user 댓글 좋아요 요청자
   */
  void likeComment(Long id, User user);

  /**
   * 댓글 좋아요 취소
   *
   * @param id   좋아요 취소 댓글 ID
   * @param user 댓글 좋아요 취소 요청자
   */
  void dislikeComment(Long id, User user);
}
