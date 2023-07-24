package com.sparta.myblog.service;

import com.sparta.myblog.dto.PostListResponseDto;
import com.sparta.myblog.dto.PostRequestDto;
import com.sparta.myblog.dto.PostResponseDto;
import com.sparta.myblog.entity.Post;
import com.sparta.myblog.entity.PostLike;
import com.sparta.myblog.entity.Post_Image;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.repository.ImageRepository;
import com.sparta.myblog.repository.PostLikeRepository;
import com.sparta.myblog.repository.PostRepository;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

@Service // spring application 에 bean 으로 등록이 됨
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final ImageRepository imageRepository;

    // 1. 전체 게시글 목록 조회
    public PostListResponseDto getPosts() {
        // postRepository 결과로 넘어온 Post 의 stream 을 map 을 통해 PostResponseDto 로 변환 -> List 로 변환
        List<PostResponseDto> postList = postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toList());

        return new PostListResponseDto(postList);
    }
    public List<PostResponseDto> getPosts2() {
        // postRepository 결과로 넘어온 Post 의 stream 을 map 을 통해 PostResponseDto 로 변환 -> List 로 변환
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toList());
    }

    // 2. 게시글 작성 API (생성)
    public PostResponseDto createPost(PostRequestDto requestDto, User user, List<String> files) {
        // RequestDto -> Entity
        Post post = new Post(requestDto);
        post.setUser(user);
        postRepository.save(post);

        // 게시글 다중 파일 저장
        for (String file : files) {
            Post_Image image= new Post_Image(file, post);
            imageRepository.save(image);
        }
        // Entity -> ResponseDto
        return new PostResponseDto(post);
    }

    // 3. 선택한 게시글 조회
    public PostResponseDto getPost(Long id) {
        Post post = findPost(id);
        imageRepository.findByPostId(post.getId());
        // Entity -> ResponseDto
        return new PostResponseDto(post);
    }
////////////////////////////////////////////////////////////
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

    // 게시글 좋아요 기능 추가
    public void likePost(Long id, User user) {
        Post post = findPost(id);

        if (postLikeRepository.existsByUserAndPost(user, post)) {
            throw new DuplicateRequestException("이미 좋아요 한 게시글 입니다.");
        } else {
            PostLike postLike = new PostLike(user, post);
            postLikeRepository.save(postLike);
        }
    }

    // 게시글 좋아요 기능 취소
    public void dislikePost(Long id, User user) {
        Post post = findPost(id);
        Optional<PostLike> postLikeOptional = postLikeRepository.findByUserAndPost(user, post);
        if (postLikeOptional.isPresent()) {
            postLikeRepository.delete(postLikeOptional.get());
        } else {
            throw new IllegalArgumentException("해당 게시글에 취소할 좋아요가 없습니다.");
        }
    }

    // 해당 메모가 DB에 존재하는지 확인
    private Post findPost(Long id) {
        // postRepository.findById(id) : JPA 기본 제공 메서드라 Optional 이라는 응답값으로 오게 됨 -> Optional 은 값이 없을 경우에도 처리를 해야 함
        return postRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        );
    }

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
    }

        public List<PostResponseDto> getPostListV2() {
        // 방법2. Stream 형태로 변환해서 리스트로 바로 만들어주기
        return postRepository.findAllByOrderByCreatedAtDesc().stream() // DB 에서 조회한 List 를 Stream 으로 변환
                .map(PostResponseDto::new)  // Stream 처리를 통해 Post 를 PostResponseDto 로 변환 (PostResponseDto 생성자로 Post 객체가 들어간 메서드생성)
                .toList(); // Stream 을 List 로 다시 변환
    }*/
}