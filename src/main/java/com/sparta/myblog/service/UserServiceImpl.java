package com.sparta.myblog.service;

import com.sparta.myblog.dto.SignupRequestDto;
import com.sparta.myblog.dto.UserProfileRequestDto;
import com.sparta.myblog.dto.UserProfileResponseDto;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.entity.UserRoleEnum;
import com.sparta.myblog.exception.NotFoundException;
import com.sparta.myblog.file.S3Uploader;
import com.sparta.myblog.repository.UserRepository;
import com.sun.jdi.request.DuplicateRequestException;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final MessageSource messageSource;
  private final S3Uploader s3Uploader;

  private final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

  // 회원가입을 위해 요청받은 requestBody 내 정보를 이용하여 계정 생성
  @Transactional
  public void signup(SignupRequestDto requestDto) throws IOException {

    String username = requestDto.getUsername();
    String password = passwordEncoder.encode(requestDto.getPassword());

    // id 중복 확인
    if (userRepository.findByUsername(username).isPresent()) {
      throw new DuplicateRequestException(
          messageSource.getMessage(
              "username.already.exist",
              null,
              "Username already exist",
              Locale.getDefault()
          )
      );
    }

    // email 중복 확인
    String email = requestDto.getEmail();
    Optional<User> checkEmail = userRepository.findByEmail(email);
    if (checkEmail.isPresent()) {
      throw new DuplicateRequestException(
          messageSource.getMessage(
              "email.already.exist",
              null,
              "Email already exist",
              Locale.getDefault()
          )
      );
    }

    // 사용자 ROLE 확인
    UserRoleEnum role = UserRoleEnum.USER;
    if (requestDto.isAdmin()) { // 관리자일 때
      if (!ADMIN_TOKEN.equals(requestDto.getAdminToken())) {
        throw new IllegalArgumentException(
            messageSource.getMessage(
                "admin.password.different",
                null,
                "Administrator password is different",
                Locale.getDefault()
            ));
      }
      role = UserRoleEnum.ADMIN;
    }

    // 프로필 사진 S3 업로드
    String imageUrl = s3Uploader.upload(requestDto.getProfileImage(), "image");

    // 자기소개
    String selfText = requestDto.getSelfText();

    // 사용자 등록
    User user = new User(username, password, email, role, selfText, imageUrl);
    userRepository.save(user);
  }

  // 프로필 조회
  public UserProfileResponseDto getUserProfile(Long userId) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException(
            messageSource.getMessage(
                "user.not.found",
                null,
                "User not found",
                Locale.getDefault()
            )));

    return new UserProfileResponseDto(user);
  }

  // 프로필 수정
  @Transactional
  public UserProfileResponseDto updateUserProfile(Long userId, UserProfileRequestDto requestDto)
      throws IOException {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException(
            messageSource.getMessage(
                "user.not.found",
                null,
                "User not found",
                Locale.getDefault()
            )));

    String imageUrl = s3Uploader.upload(requestDto.getProfileImage(), "image");
      // 사용자 정보 업데이트
      user.setUsername(requestDto.getUsername());
      user.setSelfText(requestDto.getSelfText());
      user.setImageUrl(imageUrl);

      User updatedUser = userRepository.save(user);

      return new UserProfileResponseDto(updatedUser);
    }
}
