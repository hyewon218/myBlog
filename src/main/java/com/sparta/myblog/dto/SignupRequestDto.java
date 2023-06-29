package com.sparta.myblog.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {
    @NotBlank(message = "아이디는 필수 값입니다.")
    //username - 최소 4자 이상, 10자 이하이며 알파벳 소문자(a~z), 숫자(0~9)로 구성되어야 한다.
    @Size(min = 4, max = 10, message = "아이디는 4자 이상 10자 이하여야 합니다.")
    @Pattern(regexp = "^[a-z]{1}[a-z0-9]{3,10}+$", message = "아이디는 영어 소문자, 숫자로 구성되어야 합니다.")
    private String username;
    @NotBlank(message = "비밀번호는 필수 값입니다.")
    // password 는 최소 8자 이상, 15자 이하이며 알파벳 대소문자(a~z, A~Z), 숫자(0~9)로 구성되어야 한다.
    @Size(min = 8, max = 12, message = "비밀번호는 8자 이상 15자 이하여야 합니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])((?=.*\\d)|(?=.*\\W)).{8,15}+$", message = "비밀번호는 영어 대소문자, 숫자, 특수문자로 구성되어야 합니다.")
    private String password;
    @NotBlank
    @Pattern(regexp = "ADMIN|USER", message = "권한은 ADMIN 혹은 USER만 입력 가능합니다.")
    // 회원의 권한 부여하기 (ADMIN or USER)
    private String role;
}