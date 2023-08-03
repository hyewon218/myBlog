package com.sparta.myblog.service;

import com.sparta.myblog.dto.SignupRequestDto;
import com.sparta.myblog.dto.UserProfileRequestDto;
import com.sparta.myblog.dto.UserProfileResponseDto;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.entity.UserImage;
import com.sparta.myblog.entity.UserRoleEnum;
import com.sparta.myblog.exception.NotFoundException;
import com.sparta.myblog.file.FileStore;
import com.sparta.myblog.file.UploadFile;
import com.sparta.myblog.repository.UserImageRepository;
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
  private final FileStore fileStore;
  private final UserImageRepository userImageRepository;
  private final MessageSource messageSource;

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

    // 프로필 사진
    UploadFile storeImageFile = fileStore.storeFile(requestDto.getImageFile());
    String imageFile = storeImageFile.getStoreFileName();

    // 자기소개
    String selfText = requestDto.getSelfText();

    // 사용자 등록
    User user = new User(username, password, email, role, selfText);
    userRepository.save(user);

    // 사용자 프로필 사진 저장
    UserImage image = new UserImage(imageFile, user);
    userImageRepository.save(image);
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

    UserImage userImage = userImageRepository.findByUser_Id(userId);

    return new UserProfileResponseDto(user, userImage);
  }

  // 프로필 수정
  @Transactional
  public UserProfileResponseDto updateUserProfile(Long userId, UserProfileRequestDto requestDto)
      throws IOException {

    UploadFile storeImageFile = fileStore.storeFile(requestDto.getImageFile());

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException(
            messageSource.getMessage(
                "user.not.found",
                null,
                "User not found",
                Locale.getDefault()
            )));

    UserImage userImage = userImageRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException(
            messageSource.getMessage(
                "image.not.exist",
                null,
                "Image does not exist",
                Locale.getDefault()
            )));

      // 사용자 정보 업데이트
      user.setUsername(requestDto.getUsername());
      user.setSelfText(requestDto.getSelfText());
      userImage.setImageFile(storeImageFile.getStoreFileName()); // 서버용 파일이름으로 저장

      User updatedUser = userRepository.save(user);
      UserImage updateUserImage = userImageRepository.save(userImage);
      return new UserProfileResponseDto(updatedUser, updateUserImage);
    }
}
