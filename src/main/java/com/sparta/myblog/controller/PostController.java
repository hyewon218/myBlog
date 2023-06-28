package com.sparta.myblog.controller;

import com.sparta.myblog.dto.ApiResult;
import com.sparta.myblog.dto.PostRequestDto;
import com.sparta.myblog.dto.PostResponseDto;
import com.sparta.myblog.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller 의 역할 : 클라이언트의 요청을 받고 클라이언트의 요청을 Service 에 전달한다.
// Controller : 외부 api 요청 수신하고 그것에 대한 응답값을 주는 layer
@RestController
@RequestMapping("/api") // 공통 경로 표시
public class PostController {

    // service 객체는 final 변수로 선언됨 -> cotroller class 에서 service 는 한 번만 선언되고 계속 사용
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 1. 전체 게시글 목록 조회
    // 제목, 작성자명, 작성내용, 작성날짜를 조회하기
    // 작성날짜 기준 내림차순으로 정렬하기
    @GetMapping("/posts")
    public List<PostResponseDto> getPostList() {
        // service 에서 만든 List 를 받아서 실제 api 에 응답값으로 return 해준다.
        return postService.getPostListV2();
    }

    // 2. 선택한 게시글 조회
    // 선택한 게시글의  제목, 작성자명, 작성날짜, 작성내용을 조회하기
    // {경로변수} 로 id 값을 받아오면 그 id 값을 기준으로 단건을 조회
    @GetMapping("/post/{id}")
    // Query Parameter 방법
    // posts?id=11&name=최혜원 - @RequestParam Long id 로 받을 수 있고 PostRequestDto postRequestDto 객체로도 받을 수 있다.
    // 그러려면 PostRequestDto 에는 생성자가 있어야 한다. @NoArgsConstructor - 생성자 역할
    // 빈 생성자가 있고 @Setter 가 있어야 요청으로 들어오는 parameter 들을 변수들에게 set 해줄 수 있다.
    public PostResponseDto getPost(@PathVariable Long id) {
        return postService.getPost(id);
    }

    // 3. 게시글 작성
    // 제목, 작성자명(username), 작성 내용을 저장하고
    // 저장된 게시글을 Client 로 반환하기
    // @RequestBody : 요청값으로 넘어 온 JSON 객체를 PostRequestDto 객체로 변환해 주는 역할
    @PostMapping("/post")
    public PostResponseDto createPost(@RequestBody PostRequestDto requestDto, HttpServletRequest request) {
        // 응답값으로 생성된 객체의 정보를 return
        return postService.createPost(requestDto, request);
    }

    // 4. 선택한 게시글 수정
    // 제목, 작성 내용을 수정하고 수정된 게시글을 Client 로 반환하기
    // 경로변수를 받아오고 수정 내용이 생성할 때와 똑같이 body 에 담겨서 넘어옴 그 body 값을 기준으로 update
    @PutMapping("/post/{id}")
    public PostResponseDto updatePost(@PathVariable Long id, @RequestBody PostRequestDto postRequestDto, HttpServletRequest request) {
        return postService.updatePost(id, postRequestDto, request);
    }

    // 선택한 게시글 삭제
    // id 값을 지정을 해서 단건임을 명시
    // 선택한 게시글을 삭제하고 Client 로 성공했다는 메시지, 상태코드 반환하기
    @DeleteMapping("/post/{id}")
    public ApiResult deletePost(@PathVariable Long id, HttpServletRequest request) {
        postService.deletePost(id, request);
        return new ApiResult("게시글 삭제 성공", HttpStatus.OK.value()); // 게시글 삭제 성공시 ApiResult Dto를 사용하여 성공메세지와 statusCode를 띄움
    }
}


