package com.sparta.myblog.controller;

import com.sparta.myblog.dto.PostRequestDto;
import com.sparta.myblog.dto.PostResponseDto;
import com.sparta.myblog.service.PostService;
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

    // 1. 전체 게시글 목록 조회 (Get - 조회)
    // 제목, 작성자명, 작성내용, 작성날짜를 조회하기
    // 작성날짜 기준 내림차순으로 정렬하기
    @GetMapping("/posts")
    public List<PostResponseDto> getPostList() {
        // service 에서 만든 List 를 받아서 실제 api 에 응답값으로 return 해준다.
        return postService.getPostListV2();
    }

    // 2. 선택한 게시글 조회 (Get - 조회)
    // 선택한 게시글의  제목, 작성자명, 작성날짜, 작성내용을 조회하기
    // {경로변수} 로 id 값을 받아오면 그 id 값을 기준으로 단건을 조회
    @GetMapping("/posts/{id}")
    // Query Parameter 방법
    // posts?id=11&name=최혜원 - @RequestParam Long id 로 받을 수 있고 PostRequestDto postRequestDto 객체로도 받을 수 있다.
    // 그러려면 PostRequestDto 에는 생성자가 있어야 한다. @NoArgsConstructor - 생성자 역할
    // 빈 생성자가 있고 @Setter 가 있어야 요청으로 들어오는 parameter 들을 변수들에게 set 해줄 수 있다.
    public PostResponseDto getPost(@PathVariable Long id) {
        return postService.getPost(id);
    }

    // 3. 게시글 작성 (Post - 생성)
    // 제목, 작성자명, 비밀번호, 작성 내용을 저장하고
    // 저장된 게시글을 Client 로 반환하기
    // @RequestBody : 요청값으로 넘어 온 JSON 객체를 PostRequestDto 객체로 변환해 주는 역할
    @PostMapping("/posts")
    public PostResponseDto createPost(@RequestBody PostRequestDto requestDto) {
        // 응답값으로 생성된 객체의 정보를 return
        return postService.createPost(requestDto);
    }

    // 4. 선택한 게시글 수정 (Put- 수정)
    // 제목, 작성자명, 작성내용을 수정하고 수정된 게시글을 클라이언트로 반환하기
    // 경로변수를 받아오고 수정 내용이 생성할 때와 똑같이 body 에 담겨서 넘어옴 그 body 값을 기준으로 update
    @PutMapping("/posts/{id}")
    public PostResponseDto updatePost(@PathVariable Long id, @RequestBody PostRequestDto postRequestDto) {
        return postService.updatePost(id, postRequestDto);
    }

    // 선택한 게시글 삭제
    // 선택한 게시글을 삭제하고 클라이언트로 성공했다는 표시 반환하기
    // id 값을 지정을 해서 단건임을 명시
    // 요청 body에 포함되어 있는 값들을 BlogRequestDto 이 객체로 받아올 수 있도록 한다.
    @DeleteMapping("/posts/{id}")
    public PostResponseDto deletePost(@PathVariable Long id, @RequestBody PostRequestDto postRequestDto) {
        //return postService.deletePost(id, postRequestDto);
        // 삭제할 때 필요한 건 password -> PostRequestDto 에서 password 를 꺼내서 id 값이랑 같이 해서 deletePost 메서드 호출
        postService.deletePost(id, postRequestDto.getPassword());
        // success 값만 받아서 PostResponseDto 로 변환 해 응답
        return new PostResponseDto(true);
    }
}


