package com.sparta.myblog.service;

import com.sparta.myblog.dto.PostRequestDto;
import com.sparta.myblog.dto.PostResponseDto;
import com.sparta.myblog.entity.Post;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.jwt.JwtUtil;
import com.sparta.myblog.repository.PostRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// Service : Controller 뒤에서 실제 Controller 요청 에 대한 응답을 생성하거나 요청에 대한 처리를 담당 서비스 비지니스 로직 처리 layer
@Service // spring application 에 bean 으로 등록이 됨
public class PostService {

    private final PostRepository postRepository;
    private final JwtUtil jwtUtil;


    @Autowired
    public PostService(PostRepository postRepository, JwtUtil jwtUtil) {
        this.postRepository = postRepository;
        this.jwtUtil = jwtUtil;
    }

    // 전체 게시글 조회 API
    @Deprecated
    public List<PostResponseDto> getPostListV1() {
        // DB 조회
        //return blogRepository.findAllByOrderByCreatedAtDesc().stream().map(BlogResponseDto::new).toList();

        // 방법1. 리스트 반복하며 넣어주기
        // Repository 라는 객체를 통해서 DB를 통해서 조회해 올 수 있다.
        // findAll : 데이터 목록, OrderBy : 순서, CreateAt : 생성 일시를 기준으로, tDesc : 내림차순 정렬
        List<Post> postList = postRepository.findAllByOrderByCreatedAtDesc();
        // Dto 로 반환하기 위해서 postResponseDtoList 를 생성해 주고
        List<PostResponseDto> postResponseDtoList = new ArrayList<>();
        // postList 를 반복해 주면서
        for (Post post : postList) {
            // 생성자를 통해서 이 post 를 PostResponseDto 로 변환한 다음에 하나씩 add 를 해줘서 List 를 채워 응답하게 됨
            postResponseDtoList.add(new PostResponseDto(post));
        }
        // 값을 넣어서 Dto 가 완성이 되면 Service 가 응답하게 됨
        return postResponseDtoList;
    }

    public List<PostResponseDto> getPostListV2() {
        // 방법2. Stream 형태로 변환해서 리스트로 바로 만들어주기
        return postRepository.findAllByOrderByCreatedAtDesc().stream() // DB 에서 조회한 List 를 Stream 으로 변환
                .map(PostResponseDto::new)  // Stream 처리를 통해 Post 를 PostResponseDto 로 변환 (PostResponseDto 생성자로 넣어서 Post 객체가 들어간 메서드생성)
                .toList(); // Stream 을 List 로 다시 변환
    }

    // 게시글 작성 API (생성)
    public PostResponseDto createPost(PostRequestDto requestDto, HttpServletRequest request) {
        // RequestDto -> Entity
        // Entity 클래스(Post) 에서 생성자를 호출해서 각각 필드를 채워준다.
        // Post 클래스 객체 하나가 데이터 베이스의 한줄, 한 row 이다.

        // 토큰을 검사하여, 유효한 토큰일 경우에만 게시글 작성 가능
        // 토큰 체크 추가
        User user = jwtUtil.checkToken(request);
        Post post = Post.builder()
                .requestDto(requestDto)
                .user(user)
                .build();

        // DB 저장
        // post 한 줄 저장 후 Post 반환
        // Entity 객체 (post) 를 전달하면 객체는 저장 되어서 응답 값으로 Post 가 반환된다.
        Post savePost = postRepository.save(post);
        // Entity -> ResponseDto
        // PostResponseDto 생성자를 통해서 Entity 객체(savePost)를 ResponseDto 응답 객체로 변환
        PostResponseDto postResponseDto = new PostResponseDto(savePost);

        return postResponseDto;
    }

    // 선택한 게시글 조회 API
    public PostResponseDto getPost(Long id) {
        Post post = findPost(id);
        // Entity -> ResponseDto
        return new PostResponseDto(post);
    }

    // 선택한 게시글 수정 API
    @Transactional // Entity 객체가 변환된 것을 메소드가 끝날 때 (Transaction 이 끝날 때) DB에 반영을 해 줌
    public PostResponseDto updatePost(Long id, PostRequestDto postRequestDto, HttpServletRequest request) {
        //토큰을 검사한 후, 유효한 토큰이면서 해당 사용자가 작성한 게시글만 수정 가능
        // 토큰 체크 추가
        User user = jwtUtil.checkToken(request);
        Post post = findPost(id);

        if (!post.getUser().equals(user)) {
            throw new IllegalArgumentException("작성자만 수정/삭제 할 수 있습니다.");
        }
        post.update(postRequestDto);
        // update 했으면 Dto 로 변환해서 응답해 줌
        return new PostResponseDto(post);
    }

    // 선택한 게시글 삭제 API
    public void deletePost(Long id, HttpServletRequest request) {
        // 토큰을 검사한 후, 유효한 토큰이면서 해당 사용자가 작성한 게시글만 삭제 가능
        // 토큰 체크 추가
        User user = jwtUtil.checkToken(request);
        Post post = findPost(id);

        if (!post.getUser().equals(user)) {
            throw new IllegalArgumentException("작성자만 수정/삭제 할 수 있습니다.");
        }
        postRepository.delete(post);
    }

    // 해당 메모가 DB에 존재하는지 확인
    private Post findPost(Long id) {
        // postRepository.findById(id) : JPA 기본 제공 메서드라 Optional 이라는 응답값으로 오게 됨 -> Optional 은 값이 없을 경우에도 처리를 해야 함
        return postRepository.findById(id).orElseThrow(() ->  // orElseThrow 값이 없을 경우 어떤 예외를 던질지
                new IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        );
    }

}