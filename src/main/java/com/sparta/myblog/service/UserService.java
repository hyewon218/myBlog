package com.sparta.myblog.service;

import com.sparta.myblog.dto.SignupRequestDto;
import com.sparta.myblog.dto.UserProfileRequestDto;
import com.sparta.myblog.dto.UserProfileResponseDto;
import java.io.IOException;

public interface UserService {

  /**
   * 회원가입
   *
   * @param requestDto 회원가입 요청정보
   */
  void signup(SignupRequestDto requestDto) throws IOException;

  /**
   * 프로필 조회
   *
   * @param userId 조회할 게시글 ID
   * @return 조회된 프로필 정보
   */
  UserProfileResponseDto getUserProfile(Long userId);

  /**
   * 프로필 수정
   *
   * @param userId     수정할 사용자 ID
   * @param requestDto 수정할 사용자 정보
   * @return 수정된 프로필 정보
   */
  UserProfileResponseDto updateUserProfile(Long userId, UserProfileRequestDto requestDto)
      throws IOException;
}