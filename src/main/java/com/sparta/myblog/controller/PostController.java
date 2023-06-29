package com.sparta.myblog.controller;

import com.sparta.myblog.dto.ApiResponseDto;
import com.sparta.myblog.dto.PostListResponseDto;
import com.sparta.myblog.dto.PostRequestDto;
import com.sparta.myblog.dto.PostResponseDto;
import com.sparta.myblog.security.UserDetailsImpl;
import com.sparta.myblog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.RejectedExecutionException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 1. 전체 게시글 목록 조회
    // 제목, 작성자명, 작성내용, 작성날짜를 조회하기
    @GetMapping("/posts")
    public ResponseEntity<PostListResponseDto> getPosts() {
        PostListResponseDto result = postService.getPosts();

        return ResponseEntity.ok().body(result);
    }

    // 2. 게시글 작성
    // 제목, 작성자명(username), 작성 내용을 저장하고
    // 저장된 게시글을 Client 로 반환하기
    // @RequestBody : 요청값으로 넘어 온 JSON 객체를 PostRequestDto 객체로 변환해 주는 역할
    @PostMapping("/post")
    public ResponseEntity<PostResponseDto> createPost(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody PostRequestDto requestDto) {
        PostResponseDto result = postService.createPost(requestDto, userDetails.getUser());

        return ResponseEntity.status(201).body(result);
    }

    // 3. 선택한 게시글 조회
    // 선택한 게시글의  제목, 작성자명, 작성날짜, 작성내용을 조회하기
    // {경로변수} 로 id 값을 받아오면 그 id 값을 기준으로 단건을 조회
    @GetMapping("/post/{id}")
    public PostResponseDto getPost(@PathVariable Long id) {
        return postService.getPost(id);
    }

    // 4. 선택한 게시글 수정
    // 제목, 작성 내용을 수정하고 수정된 게시글을 Client 로 반환하기
    @PutMapping("/post/{id}")
    public ResponseEntity<PostResponseDto> updatePost(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long id, @RequestBody PostRequestDto requestDto) {
        try {
            PostResponseDto result = postService.updatePost(id, requestDto, userDetails.getUser());
            return ResponseEntity.ok().body(result);
        } catch (RejectedExecutionException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 5. 선택한 게시글 삭제
    // 선택한 게시글을 삭제하고 Client 로 성공했다는 메시지, 상태코드 반환하기
    @DeleteMapping("/post/{id}")
    public ResponseEntity<ApiResponseDto> deletePost(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long id) {
        try {
            postService.deletePost(id, userDetails.getUser());
            return ResponseEntity.ok().body(new ApiResponseDto("게시글 삭제 성공", HttpStatus.OK.value()));
        } catch (RejectedExecutionException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


