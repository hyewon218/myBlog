package com.sparta.myblog.service;

import com.sparta.myblog.dto.PostOnlyIdResponseDto;
import com.sparta.myblog.dto.PostRequestDto;
import com.sparta.myblog.dto.PostResponseDto;
import com.sparta.myblog.dto.ResponseWithNotificationEventDto;
import com.sparta.myblog.entity.NotificationArgs;
import com.sparta.myblog.entity.NotificationType;
import com.sparta.myblog.entity.Post;
import com.sparta.myblog.entity.PostImage;
import com.sparta.myblog.entity.PostLike;
import com.sparta.myblog.entity.SseEventName;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.exception.NotFoundException;
import com.sparta.myblog.file.S3Uploader;
import com.sparta.myblog.kafka.NotificationEvent;
import com.sparta.myblog.repository.PostImageRepository;
import com.sparta.myblog.repository.PostLikeRepository;
import com.sparta.myblog.repository.PostRepository;
import com.sparta.myblog.repository.PostRepositoryQuery;
import com.sparta.myblog.repository.PostSearchCond;
import com.sun.jdi.request.DuplicateRequestException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

  private final PostRepository postRepository;
  private final PostLikeRepository postLikeRepository;
  private final PostImageRepository postImageRepository;
  private final MessageSource messageSource;
  private final PostRepositoryQuery postRepositoryQuery;
  private final S3Uploader s3Uploader;

  // 전체 게시글 목록 조회
  public List<PostResponseDto> getPosts2() {
    // postRepository 결과로 넘어온 Post 의 stream 을 map 을 통해 PostResponseDto 로 변환 -> List 로 변환
    return postRepository.findAllByOrderByCreatedAtDesc().stream()
        .map(PostResponseDto::of)
        .collect(Collectors.toList());
  }

  // 키워드 검색 게시글 목록 조회
  public List<PostResponseDto> searchPost(PostSearchCond cond, Pageable pageable) {
    return postRepositoryQuery.searchPost(cond, pageable).stream()
        .map(PostResponseDto::of)
        .collect(Collectors.toList());
  }

  // 게시글 생성
  public PostResponseDto createPost(PostRequestDto requestDto, User user, List<MultipartFile> images)
      throws IOException {

    Post post = new Post(requestDto);
    post.setUser(user);
    postRepository.save(post);

    if (images != null) {
      for (MultipartFile image : images) {
        String imageUrl = s3Uploader.upload(image, "image");
        // 게시글 다중 파일 저장
          PostImage postImage = new PostImage(imageUrl, post);
          postImage.updateOriginalImageName(image.getOriginalFilename());

          postImageRepository.save(postImage);
        }
      }
    return PostResponseDto.of(post);
  }

  // 선택한 게시글 조회
  public PostResponseDto getPost(String id) {

    Post post = findPost(id);
    // 게시글 다중 파일 조회
    postImageRepository.findByPostId(post.getId());

    return PostResponseDto.of(post);
  }

  // 선택한 게시글 수정
  @Transactional // Entity 객체가 변환된 것을 메소드가 끝날 때 (Transaction 이 끝날 때) DB에 반영을 해 줌
  public PostResponseDto updatePost(String id, PostRequestDto requestDto, User user, List<MultipartFile> image)
      throws IOException {

    Post post = findPost(id);

    if (image != null) {
      for (MultipartFile multipartFile : image) {
        String imageUrl = s3Uploader.upload(multipartFile, "image");
        // 게시글 다중 파일 저장
        PostImage postImage = new PostImage(imageUrl, post);
        postImageRepository.save(postImage);
      }
    }

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

    return PostResponseDto.of(post);
  }

  // 선택한 게시글 삭제
  public void deletePost(String id, User user) {

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
  public ResponseWithNotificationEventDto<PostOnlyIdResponseDto> likePost(String postId, User user) {

    Post post = findPost(postId);

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

      // 알림 생성
      NotificationEvent notificationEvent = new NotificationEvent(NotificationType.LIKE,
          NotificationArgs.builder()
              .commentId(null)
              .postId(postId)
              /* 알람 발생시킨 멤버 */
              .callingMemberNickname(user.getUsername())
              .build(), post.getUser().getId(), SseEventName.NOTIFICATION_LIST);

      return ResponseWithNotificationEventDto.<PostOnlyIdResponseDto>builder()
          .response(PostOnlyIdResponseDto.of(postId))
          .notificationEvent(notificationEvent)
          .build();
    }
  }

  // 게시글 좋아요 기능 취소
  public void dislikePost(String id, User user) {

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
  public Post findPost(String id) {
    // postRepository.findById(id) : JPA 기본 제공 메서드라 Optional 이라는 응답값으로 오게 됨 -> Optional 은 값이 없을 경우에도 처리를 해야 함
    return postRepository.findByApiId(id).orElseThrow(() ->
        new NotFoundException(
            messageSource.getMessage(
                "post.not.exist",
                null,
                "Post does not exist",
                Locale.getDefault()
            )));
  }
}
