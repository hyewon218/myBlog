package com.sparta.myblog.security;

import com.sparta.myblog.entity.User;
import com.sparta.myblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 해당 user 가 있는지 없는지 확인(회원 유무 확인)
        User user = userRepository.findByUsername(username) // user 확인
                .orElseThrow(() -> new UsernameNotFoundException("Not Found " + username));

        // 조회된 회원 정보(user) 를 UserDetails 로 변환
        return new UserDetailsImpl(user); // 그 정보를 UserDetailsImpl 생성자로 보내서 UserDetailsImpl 을 반환
    }
}