package com.sparta.myblog.controller;

import com.sparta.myblog.dto.*;
import com.sparta.myblog.exception.ApiResponseDto;
import com.sparta.myblog.file.AwsS3Service;
import com.sparta.myblog.security.UserDetailsImpl;
import com.sparta.myblog.repository.PostSearchCond;
import com.sparta.myblog.service.PostService;
import com.sparta.myblog.service.UserService;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Slf4j
@Controller
@RequestMapping("/view")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final AwsS3Service awsS3Service;

    // 게시글 작성 페이지(메인페이지에서 클릭)
    // http://localhost:8080/view/posts
    @GetMapping("/posts")
    public String showPostPage(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        log.info("게시글 작성 창으로 이동");
        UserProfileResponseDto dto = userService.getUserProfile(userDetails.getUser().getId());
        model.addAttribute("user", dto);
        return "post";
    }

    // 게시글 작성 + S3 이미지 업로드
    // http://localhost:8080/view/posts
    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createPost(@ModelAttribute PostRequestDto requestDto,
                             @RequestPart(value = "multipartFileList", required = false) List<MultipartFile> multipartFileList,
                             @AuthenticationPrincipal UserDetailsImpl userDetails, RedirectAttributes redirectAttributes){
        if (multipartFileList.isEmpty()) {
            throw new IllegalArgumentException("파일이 유효하지 않습니다.");
        }
        log.info("게시글 작성");

        // AwsS3Service 의 uploadFile 메소드를 호출하여 S3에 저장
        List<String> uploadFile = awsS3Service.uploadFile(multipartFileList);
        PostResponseDto postResponseDto = postService.createPost(requestDto, userDetails.getUser(), uploadFile);

        redirectAttributes.addAttribute("id", postResponseDto.getId());
        log.info(postResponseDto.getContent());

        return "redirect:/view/posts/{id}";
    }

    // 키워드 검색 게시글 목록 조회 TODO : 화면에 검색 창 만들고 수정하기
    @GetMapping("posts/search")
    public String searchPost(PostSearchCond cond, Model model){

        List<PostResponseDto> postList = postService.searchPost(cond);

        if(!postList.isEmpty()){
            model.addAttribute("posts",postList);
            return "index";
        }
        return "redirect:/";
    }

    // 게시글 상세 페이지 조회
    // http://localhost:8080/view/post/1
    @GetMapping("/posts/{id}")
    public String detailPost(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 댓글 프로필 사진 (유저 정보 가져와서 보여주기)
        UserProfileResponseDto profileDto = userService.getUserProfile(userDetails.getUser().getId());
        model.addAttribute("user", profileDto);
        // 프로필 사진, 작성자, 제목, 내용, 댓글 목록
        PostResponseDto postDto = postService.getPost(id);
        model.addAttribute("post", postDto);

        return "post-view";
    }

    // 게시글 상세 페이지 수정
    @PutMapping("/posts/{id}")
    public String updatePost(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody PostRequestDto requestDto, Model model){
        log.info("상세 페이지 수정");
        log.info("유저 아이디: "+userDetails.getUser().getId());

        PostResponseDto dto = postService.updatePost(id, requestDto, userDetails.getUser());
        model.addAttribute("post", dto); // 수정한 데이터 화면에 뿌려주기

        return "redirect:/detail_page";
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{id}")
    public Long deletePost(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails){
        postService.deletePost(id, userDetails.getUser());

        return id;
    }

    // 게시글 좋아요 기능 추가
    @PostMapping("/posts/{id}/like")
    public ResponseEntity<ApiResponseDto> likePost(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            postService.likePost(id, userDetails.getUser());
        } catch (DuplicateRequestException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponseDto("게시글 좋아요 성공", HttpStatus.ACCEPTED.value()));
    }

    // 게시글 좋아요 기능 취소
    @DeleteMapping("/posts/{id}/like")
    public ResponseEntity<ApiResponseDto> dislikePost(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            postService.dislikePost(id, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDto(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponseDto("게시글 좋아요 취소 성공", HttpStatus.ACCEPTED.value()));
    }
}


