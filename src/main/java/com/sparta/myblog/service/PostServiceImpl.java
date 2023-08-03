package com.sparta.myblog.service;

import com.sparta.myblog.dto.PostRequestDto;
import com.sparta.myblog.dto.PostResponseDto;
import com.sparta.myblog.entity.Post;
import com.sparta.myblog.entity.PostImage;
import com.sparta.myblog.entity.PostLike;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.entity.UserImage;
import com.sparta.myblog.exception.NotFoundException;
import com.sparta.myblog.repository.PostImageRepository;
import com.sparta.myblog.repository.PostLikeRepository;
import com.sparta.myblog.repository.PostRepository;
import com.sparta.myblog.repository.UserImageRepository;
import com.sun.jdi.request.DuplicateRequestException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

  private final PostRepository postRepository;
  private final PostLikeRepository postLikeRepository;
  private final PostImageRepository postImageRepository;
  private final UserImageRepository userImageRepository;
  private final MessageSource messageSource;

  // 전체 게시글 목록 조회
  public List<PostResponseDto> getPosts2() {
    // postRepository 결과로 넘어온 Post 의 stream 을 map 을 통해 PostResponseDto 로 변환 -> List 로 변환
    return postRepository.findAllByOrderByCreatedAtDesc().stream()
        .map(PostResponseDto::new)
        .collect(Collectors.toList());
  }

  // 게시글 생성
  public PostResponseDto createPost(PostRequestDto requestDto, User user, List<String> files) {

    Post post = new Post(requestDto);
    post.setUser(user);
    postRepository.save(post);

    // 게시글 다중 파일 저장
    for (String file : files) {
      PostImage image = new PostImage(file, post);
      postImageRepository.save(image);
    }

    return new PostResponseDto(post);
  }

  // 선택한 게시글 조회
  public PostResponseDto getPost(Long id) {

    Post post = findPost(id);
    // 게시글 다중 파일 조회
    postImageRepository.findByPostId(post.getId());
    // 사용자 프로필 사진 조회
    UserImage userImage = userImageRepository.findByUser_Id(
        post.getUser().getId()); // post user_id 와 연결

    return new PostResponseDto(post, userImage);
  }

  //////////////////////////진행중//////////////////////////////////

  // 선택한 게시글 수정
  @Transactional // Entity 객체가 변환된 것을 메소드가 끝날 때 (Transaction 이 끝날 때) DB에 반영을 해 줌
  public PostResponseDto updatePost(Long id, PostRequestDto requestDto, User user) {

    Post post = findPost(id);

    if (!post.getUser().equals(user)) {
      throw new IllegalArgumentException(
          messageSource.getMessage(
              "only.author.edit",
              null,
              "Only author can edit",
              Locale.getDefault()
          )
      );
    }

    post.setTitle(requestDto.getTitle());
    post.setContent(requestDto.getContent());

    return new PostResponseDto(post);
  }

  // 선택한 게시글 삭제
  public void deletePost(Long id, User user) {

    Post post = findPost(id);

    if (!post.getUser().equals(user)) {
      throw new IllegalArgumentException(
          messageSource.getMessage(
              "only.author.delete",
              null,
              "Only author can delete",
              Locale.getDefault()
          )
      );
    }

    postRepository.delete(post);
  }

  // 게시글 좋아요 기능 추가
  public void likePost(Long id, User user) {

    Post post = findPost(id);

    if (postLikeRepository.existsByUserAndPost(user, post)) {
      throw new DuplicateRequestException(
          messageSource.getMessage(
              "post.already.liked",
              null,
              "Post already been liked",
              Locale.getDefault()
          )
      );
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
      throw new IllegalArgumentException(
          messageSource.getMessage(
              "no.like.to.cancel",
              null,
              "No like to cancel",
              Locale.getDefault()
          )
      );
    }
  }

  // 해당 게시글이 DB에 존재하는지 확인
  public Post findPost(Long id) {
    // postRepository.findById(id) : JPA 기본 제공 메서드라 Optional 이라는 응답값으로 오게 됨 -> Optional 은 값이 없을 경우에도 처리를 해야 함
    return postRepository.findById(id).orElseThrow(() ->
        new NotFoundException(
            messageSource.getMessage(
                "post.not.exist",
                null,
                "Post does not exist",
                Locale.getDefault()
            )));
  }
}
