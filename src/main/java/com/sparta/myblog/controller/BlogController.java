package com.sparta.myblog.controller;

import com.sparta.myblog.dto.BlogDeleteResponseDto;
import com.sparta.myblog.dto.BlogRequestDto;
import com.sparta.myblog.dto.BlogResponseDto;
import com.sparta.myblog.entity.Blog;
import com.sparta.myblog.service.BlogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller의 역할 : 클라이언트의 요청을 받고 클라이언트의 요청을 Service에 전달한다.
@RestController
@RequestMapping("/api")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {

        this.blogService = blogService;
    }

    // 전체 게시글 목록 조회
    // 제목, 작성자명, 작성내용, 작성날짜를 조회하기
    // 작성날짜 기준 내림차순으로 정렬하기
    @GetMapping("/posts")
    public List<BlogResponseDto> getPosts() {

        return blogService.getPosts();
    }

    // 게시글 작성
    // 제목, 작성자명, 비밀번호, 작성 내용을 저장하고
    //저장된 게시글을 Client 로 반환하기
    @PostMapping("/posts")
    public BlogResponseDto createPost(@RequestBody BlogRequestDto requestDto) {

        return blogService.createPost(requestDto);
    }

    // 선택한 게시글 조회
    // 선택한 게시글의  제목, 작성자명, 작성날짜, 작성내용을 조회하기
    @GetMapping("/posts/{id}")
    public BlogResponseDto getPost(@PathVariable Long id) {

        return blogService.getPost(id);
    }

    // 선택한 게시글 수정
    // 제목, 작성자명, 작성내용을 수정하고 수정된 게시글을 클라이언트로 반환하기
    @PutMapping("/posts/{id}")
    public BlogResponseDto updatePost(@PathVariable Long id, @RequestBody BlogRequestDto blogRequestDto ) {

        return blogService.updatePost(id, blogRequestDto);
    }

    // 선택한 게시글 삭제
    // 선택한 게시글을 삭제하고 클라이언트로 성공했다는 표시 반환하기
    @DeleteMapping("/posts/{id}")
    public BlogDeleteResponseDto deletePost(@PathVariable Long id, @RequestBody BlogRequestDto blogRequestDto) {

        return blogService.deletePost(id, blogRequestDto);
    }
}


