package com.sparta.myblog.security;

import com.sparta.myblog.entity.User;
import com.sparta.myblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 2. 인증정보 받아오기
 * UsernamePasswordAuthenticationFilter > UserDetailsService 구현 > loadUserByUsername() > UserDetails > Authentication (createSuccessAuthentication()에서 만들어짐)
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetails loadUserById(Long id){
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));
        return new UserDetailsImpl(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Not Found " + username));

        // 조회된 회원 정보(user) 를 UserDetails 로 변환
        return new UserDetailsImpl(user); // 그 정보를 UserDetailsImpl 생성자로 보내서 UserDetailsImpl 을 반환
    }
}