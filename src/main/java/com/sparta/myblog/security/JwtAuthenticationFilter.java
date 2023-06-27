package com.sparta.myblog.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.myblog.dto.LoginRequestDto;
import com.sparta.myblog.entity.UserRoleEnum;
import com.sparta.myblog.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    // login 요청을 하면 로그인 시도를 위해서 실행되는 함수
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // ObjectMapper().readValue : JSON 형태의 String 데이터를 Object 로 바꾸는 것
            // request 요청 body 부분에 username 이랑 password 가 JSON 형식으로 넘어올 것, 첫 번째 파라미터에 그 테이터를 넣어준다.
            // 두 번째 파라미터는 변환할 Object 의 타입을 줘야 함. LoginRequestDto 의 username, password 그대로 사용할 것임
            LoginRequestDto requestDto = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDto.class);

            // 인증 처리를 하는 메서드
            return getAuthenticationManager().authenticate(
                    // 인증 객체 Token 을 넣어줘야 함
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getUsername(),
                            requestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    //로그인 성공 시, 로그인에 성공한 유저의 정보와 JWT 를 활용하여 토큰을 발급하고,
    //발급한 토큰을 Header 에 추가하고 성공했다는 메시지, 상태코드 와 함께 Client 에 반환하기
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        log.info("로그인 성공 및 JWT 생성");
        String username = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
        UserRoleEnum role = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getRole();

        // 역할 가지고 오는 이유? Token 생성할 때 역할을 두번째 파라미터에 넣어주기로 했었음
        String token = jwtUtil.createToken(username, role);
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, token);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        log.info("로그인 실패");
        response.setStatus(401);
    }

}