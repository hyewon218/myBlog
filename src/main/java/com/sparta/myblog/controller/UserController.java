package com.sparta.myblog.controller;

import com.sparta.myblog.dto.SignupRequestDto;
import com.sparta.myblog.dto.UserInfoDto;
import com.sparta.myblog.dto.UserProfileResponseDto;
import com.sparta.myblog.dto.UserProfileRequestDto;
import com.sparta.myblog.entity.UserRoleEnum;
import com.sparta.myblog.file.FileStore;
import com.sparta.myblog.security.UserDetailsImpl;
import com.sparta.myblog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/view/user")
public class UserController {

    private final UserService userService;
    private final FileStore fileStore;

    @GetMapping("/login-page")
    public String loginPage() {
        return "signup_login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup_login";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupRequestDto requestDto, BindingResult bindingResult) throws IOException {
        // Validation 예외처리
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        if(fieldErrors.size() > 0) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
            }
            return "redirect:/view/user/signup";
        }
        userService.signup(requestDto);
        return "redirect:/view/user/login-page";
    }

    // 회원 관련 정보 받기
    @GetMapping("/user-info")
    @ResponseBody
    public UserInfoDto getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        String username = userDetails.getUser().getUsername();
        UserRoleEnum role = userDetails.getUser().getRole();
        boolean isAdmin = (role == UserRoleEnum.ADMIN);
        String selfText = userDetails.getUser().getSelfText();

        return new UserInfoDto(username, isAdmin, selfText);
    }

    // 프로필 조회
    //@PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        log.info("프로필 창으로 이동");
        Long userId = userDetails.getUser().getId();
        UserProfileResponseDto dto = userService.getUserProfile(userId);
        model.addAttribute("user", dto);

        return "profile";
    }

    // 프로필 수정 (PutMapping 하면 오류남..ajax 가 안되나?)
    //@PreAuthorize("isAuthenticated()")
    @PostMapping("/profile")
    public String modifyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails, @ModelAttribute UserProfileRequestDto requestDto) throws IOException {
        log.info("프로필 수정");
        Long userId=userDetails.getUser().getId(); // 사용자 id
        UserProfileResponseDto dto = userService.updateUserProfile(userId, requestDto);
        log.info(dto.getUsername());
        log.info(dto.getSelfText());
        log.info(dto.getImageFile());
        log.info("프로필 수정 완료");

        return "redirect:/view/user/profile"; // 수정 후 조회 화면에서 데이터 보여주기
    }

    // 저장된 경로에서 이미지 다운로드 하여 보여주기
    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

}

/*    // 회원가입 (postman 으로 실행)
    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto> signUp(@Valid @RequestBody SignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.status(201).body(new ApiResponseDto("회원가입 성공", HttpStatus.CREATED.value()));
    }
    // 로그인 (postsman 으로 실행 - JwtAuthenticationFilter 필요 X)
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        userService.login(loginRequestDto);
        //JWT 생성 및 쿠키에 저장 후 Response 객체에 추가
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(loginRequestDto.getUsername(), loginRequestDto.getRole()));
        return ResponseEntity.ok().body(new ApiResponseDto("로그인 성공", HttpStatus.CREATED.value()));
    }*/


