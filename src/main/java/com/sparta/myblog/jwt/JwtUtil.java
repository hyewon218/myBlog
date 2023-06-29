package com.sparta.myblog.jwt;

import com.sparta.myblog.entity.User;
import com.sparta.myblog.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
@RequiredArgsConstructor
public class JwtUtil {
    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization"; // Cookie 의 Name 값
    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";
    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";
    // 토큰 만료시간
    private final long TOKEN_TIME = 60 * 60 * 1000L; // 60분

    @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        // secretKey 값이 Base64 로 Encoding 한 값이기 때문에 secretKey 를 사용하려면 Decoding 을 해줘야 한다.
        // byte 배열 타입으로 Decoding 한 값을 반환
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // 토큰 생성
    public String createToken(String username) {
        Date date = new Date();

        return BEARER_PREFIX + // Bearer 과 공백 추가로 붙여줌
                Jwts.builder()
                        .setSubject(username) // 사용자 식별자값(ID)
                        // 생성되는 시점 기준으로 60분을 처리하기 위해
                        // date.getTime() : 현재 시간 + TOKEN_TIME : 60분
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME)) // 만료 시간
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();
    }

    // Header 에서 JWT 가져오기
    // HttpServletRequest 안에는 우리가 가져와야 할 토큰이 Header 에 들어있음
    public String getJwtFromHeader(HttpServletRequest request) {
        // bearer 붙은 토큰 값 가져온다.
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        // 순수한 토큰을 한번에 Header 에서 뽑아온다.
        // 코드가 있는지, BEARER 로 시작하는지 확인
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7); // 앞에 7글자를 지워줌 BEARER 가 6글자이고 한칸이 띄어져있기때문
        }
        return null;
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            // Token 의 위변조가 있는지, 만료가 되지 않았는지 등등에 검증을 추가로 할 수 있다.
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    // 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        // getBody() : Body 부분에 들어있는 Claims(데이터들이 들어있는 집합) 를 가지고 올 수 있다.
        // JWT : Claim 기반 Web Token
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public User checkToken(HttpServletRequest request) {

        String token = this.getJwtFromHeader(request);
        Claims claims;

        User user = null;

        if (token != null) {
            if (this.validateToken(token)) {
                // 토큰에서 사용자 정보 가져오기
                claims = this.getUserInfoFromToken(token);

            } else {
                throw new IllegalArgumentException("존재하지 않는 토큰입니다.");
            }
            // 토큰에서 가져온 사용자 정보를 사용하여 DB 조회
            user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
                    () -> new IllegalArgumentException("회원을 찾을 수 없습니다.")
            );
        }
        return user;
    }
}