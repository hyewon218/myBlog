package com.sparta.myblog.service;

import com.sparta.myblog.dto.PostListResponseDto;
import com.sparta.myblog.dto.PostRequestDto;
import com.sparta.myblog.dto.PostResponseDto;
import com.sparta.myblog.entity.Post;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

@Service // spring application 에 bean 으로 등록이 됨
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    // 전체 게시글 조회 API
/*    public List<PostResponseDto> getPostListV1() {
        // 방법1. 리스트 반복하며 넣어주기
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
    }*/

/*    public List<PostResponseDto> getPostListV2() {
        // 방법2. Stream 형태로 변환해서 리스트로 바로 만들어주기
        return postRepository.findAllByOrderByCreatedAtDesc().stream() // DB 에서 조회한 List 를 Stream 으로 변환
                .map(PostResponseDto::new)  // Stream 처리를 통해 Post 를 PostResponseDto 로 변환 (PostResponseDto 생성자로 Post 객체가 들어간 메서드생성)
                .toList(); // Stream 을 List 로 다시 변환
    }*/

    // 1. 전체 게시글 목록 조회
    // 작성날짜 기준 내림차순으로 정렬하기
    public PostListResponseDto getPosts() {
        List<PostResponseDto> postList = postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toList());

        return new PostListResponseDto(postList);
    }

    // 2. 게시글 작성 API (생성)
    public PostResponseDto createPost(PostRequestDto requestDto, User user) {
        // RequestDto -> Entity
        Post post = new Post(requestDto);
        post.setUser(user);

        postRepository.save(post);

        // Entity -> ResponseDto
        return new PostResponseDto(post);
    }

    // 3. 선택한 게시글 조회 API
    public PostResponseDto getPost(Long id) {
        Post post = findPost(id);

        // Entity -> ResponseDto
        return new PostResponseDto(post);
    }

    // 4. 선택한 게시글 수정 API
    @Transactional // Entity 객체가 변환된 것을 메소드가 끝날 때 (Transaction 이 끝날 때) DB에 반영을 해 줌
    public PostResponseDto updatePost(Long id, PostRequestDto requestDto, User user) {
        Post post = findPost(id);

        if (!post.getUser().equals(user)) {
            throw new RejectedExecutionException();
        }

        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());

        return new PostResponseDto(post);
    }

    // 선택한 게시글 삭제 API
    public void deletePost(Long id, User user) {
        Post post = findPost(id);

        if (!post.getUser().equals(user)) {
            throw new RejectedExecutionException();
        }

        postRepository.delete(post);
    }

    // 해당 메모가 DB에 존재하는지 확인
    private Post findPost(Long id) {
        // postRepository.findById(id) : JPA 기본 제공 메서드라 Optional 이라는 응답값으로 오게 됨 -> Optional 은 값이 없을 경우에도 처리를 해야 함
        return postRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        );
    }
}