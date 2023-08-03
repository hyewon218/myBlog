package com.sparta.myblog.service;

import com.sparta.myblog.dto.*;
import com.sparta.myblog.entity.*;

import com.sparta.myblog.repository.PostSearchCond;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PostService {

  /**
   * 전체 게시글 목록 조회
   *
   * @return 전체 게시글 목록
   */
  List<PostResponseDto> getPosts2();

  /**
   * 키워드 검색 게시글 목록 조회
   *
   * @param cond 조건
   * @return 검색한 키워드가 있는 게시글 목록 조회
   */
  List<PostResponseDto> searchPost(PostSearchCond cond, Pageable pageable);

  /**
   * 게시글 생성
   *
   * @param requestDto 게시글 생성 요청정보
   * @param user       게시글 생성 요청자
   * @return 게시글 생성 결과
   */
  PostResponseDto createPost(PostRequestDto requestDto, User user, List<String> files);

  /**
   * 게시글 단건 조회
   *
   * @param id 조회할 게시글 ID
   * @return 조회된 게시글 정보
   */
  PostResponseDto getPost(Long id);

  /**
   * 게시글 수정
   *
   * @param id         수정할 게시글 ID
   * @param requestDto 수정할 게시글 정보
   * @param user       게시글 수정 요청자
   * @return 수정된 게시글 정보
   */
  PostResponseDto updatePost(Long id, PostRequestDto requestDto, User user);

  /**
   * 게시글 삭제
   *
   * @param id   삭제 요청 게시글 ID
   * @param user 게시글 삭제 요청자
   */
  void deletePost(Long id, User user);

  /**
   * 게시글 좋아요
   *
   * @param id   좋아요 요청 게시글 ID
   * @param user 게시글 좋아요 요청자
   */
  void likePost(Long id, User user);

  /**
   * 게시글 좋아요 취소
   *
   * @param id   좋아요 취소 게시글 ID
   * @param user 게시글 좋아요 취소 요청자
   */
  void dislikePost(Long id, User user);
}