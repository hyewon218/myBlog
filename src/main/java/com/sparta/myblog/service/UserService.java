package com.sparta.myblog.service;

import com.sparta.myblog.dto.SignupRequestDto;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.entity.UserRoleEnum;
import com.sparta.myblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ADMIN_TOKEN : 일반 사용자인지 관리자인지 구분하기 위해
    private final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    // 회원가입할 데이터를 requestDto 로 받아온다.
    public void signup(SignupRequestDto requestDto) {
        // requestDto 에서 username 뽑아오기
        String username = requestDto.getUsername();
        // 암호화 처리
        String password = passwordEncoder.encode(requestDto.getPassword());

        // 회원 중복 확인 -> 있을 때 error 처리
        // Optional : null 체크하기 위해 만들어진 타입
        Optional<User> checkUsername = userRepository.findByUsername(username);
        // isPresent() : Optional 내부에 존재하는 메서드, Optional 에 넣어준 값이 존재하는지 존재하지 않는지 확인해주는 메서드
        if (checkUsername.isPresent()) {
            throw new IllegalArgumentException("중복된 사용자가 존재합니다.");
        }

        // 사용자 ROLE 확인(권한 확인)
        UserRoleEnum role = UserRoleEnum.USER;
        // boolean 타입은 is 로 시작
        // isAdmin 이 true 이면 -> 관리자 권한으로 회원가입 하겠다는 의미
        if (requestDto.isAdmin()) {
            if (!ADMIN_TOKEN.equals(requestDto.getAdminToken())) {
                throw new IllegalArgumentException("관리자 암호가 틀려 등록이 불가능합니다.");
            }
            // ADMIN 권한으로 덮어씌우기
            role = UserRoleEnum.ADMIN;
        }

        // 사용자 등록
        // 데이터 베이스의 한 줄 즉, 한 row 는 해당하는 entity Class 에 하나의 객체다.
        User user = new User(username, password, role);
        // userRepository 에 의해 저장이 완료딤
        userRepository.save(user);
    }

}