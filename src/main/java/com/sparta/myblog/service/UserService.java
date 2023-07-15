package com.sparta.myblog.service;

import com.sparta.myblog.dto.SignupRequestDto;
import com.sparta.myblog.dto.UserProfileRequestDto;
import com.sparta.myblog.dto.UserProfileResponseDto;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.entity.UserRoleEnum;
import com.sparta.myblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ADMIN_TOKEN
    private final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    @Transactional
    public void signup(SignupRequestDto requestDto) {
        String username = requestDto.getUsername();
        String password = passwordEncoder.encode(requestDto.getPassword());

        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다.");
        }

        // email 중복확인
        String email = requestDto.getEmail();
        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            throw new IllegalArgumentException("중복된 Email 입니다.");
        }

        // 사용자 ROLE 확인
        UserRoleEnum role = UserRoleEnum.USER;
        if (requestDto.isAdmin()) { // 관리자일 때
            if (!ADMIN_TOKEN.equals(requestDto.getAdminToken())) {
                throw new IllegalArgumentException("관리자 암호가 틀려 등록이 불가능합니다.");
            }
            role = UserRoleEnum.ADMIN;
        }

        // 사용자 등록
        User user = new User(username, password, email, role);
        userRepository.save(user);
    }

    // 프로필 조회
    public UserProfileResponseDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 Id를 찾을 수 없습니다. : " + userId));
        return new UserProfileResponseDto(user);
    }

    // 프로필 수정
    public UserProfileResponseDto updateUserProfile(Long userId, UserProfileRequestDto requestDto) { // 프로필 폼에서 받은 데이터를 넣어주어야 함
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 사용자 정보 업데이트
            user.setUsername(requestDto.getUsername());
            user.setEmail(requestDto.getEmail());
            user.setSelfText(requestDto.getSelfText());
            // TODO : 필요한 다른 정보 업데이트 작업 수행

            User updatedUser = userRepository.save(user);
            return new UserProfileResponseDto(updatedUser);
        }
        return null;
    }
}

/*    public void login(LoginRequestDto requestDto) {
        String username = requestDto.getUsername();
        String password = requestDto.getPassword();

        //사용자 확인
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("등록된 사용자가 없습니다.")
        );

        //비밀번호 확인
        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }*/