package com.sparta.myblog.service;

import com.sparta.myblog.dto.LoginRequestDto;
import com.sparta.myblog.dto.SignupRequestDto;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.jwt.JwtUtil;
import com.sparta.myblog.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public void signup(SignupRequestDto signupRequestDto) {
        String username = signupRequestDto.getUsername();
        String password = signupRequestDto.getPassword();
        String role = signupRequestDto.getRole();

        // 회원 중복 확인 -> 있을 때 error 처리
        // Optional : null 체크하기 위해 만들어진 타입
        Optional<User> checkUsername = userRepository.findByUsername(username);
        // isPresent() : Optional 내부에 존재하는 메서드, Optional 에 넣어준 값이 존재하는지 존재하지 않는지 확인해주는 메서드
        if (checkUsername.isPresent()) {
            throw new IllegalArgumentException("중복된 사용자가 존재합니다.");
        }

        // 사용자 등록
        // 데이터 베이스의 한 줄 즉, 한 row 는 해당하는 entity Class 에 하나의 객체다.
        User user = new User(username, password, role);
        // userRepository 에 의해 저장이 완료딤
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public void login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        String username = loginRequestDto.getUsername();
        String password = loginRequestDto.getPassword();

        // 사용자 확인
        User user = userRepository.findByUsername(username).orElseThrow(
                () ->  new IllegalArgumentException("회원을 찾을 수 없습니다.")
        );

        // 비밀번호 확인
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        // JWT Token 생성 및 반환
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(user.getUsername()));
    }

}