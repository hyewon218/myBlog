package com.sparta.myblog.service;

import com.sparta.myblog.dto.CommentRequestDto;
import com.sparta.myblog.dto.CommentResponseDto;
import com.sparta.myblog.entity.Comment;
import com.sparta.myblog.entity.CommentLike;
import com.sparta.myblog.entity.Post;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.exception.NotFoundException;
import com.sparta.myblog.repository.CommentLikeRepository;
import com.sparta.myblog.repository.CommentRepository;
import com.sparta.myblog.repository.PostRepository;
import com.sun.jdi.request.DuplicateRequestException;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final CommentLikeRepository commentLikeRepository;
  private final MessageSource messageSource;

  // 댓글 작성
  public CommentResponseDto createComment(CommentRequestDto commentRequestDto, User user) {
    // 선택한 게시글이 있다면 댓글을 등록하고 등록된 댓글 반환하기
    Post post = postRepository.findById(commentRequestDto.getPostId()).orElseThrow(
        () -> new NotFoundException(
            messageSource.getMessage(
                "post.not.exist",
                null,
                "Post does not exist",
                Locale.getDefault()
            )));

    Comment comment = new Comment();
    comment.setContent(commentRequestDto.getContent());
    comment.setImageUrl(commentRequestDto.getImageUrl()); // 댓글 프로필 사진
    comment.setUser(user);
    comment.setPost(post);

    var savedComment = commentRepository.save(comment);

    return new CommentResponseDto(savedComment);
  }

  // 댓글 수정
  @Transactional
  public CommentResponseDto updateComment(Long commentId, CommentRequestDto commentRequestDto,
      User user) {
    // 선택한 댓글이 있다면 댓글 수정하고 수정된 댓글 반환하기
    Comment comment = findComment(commentId);

    if (!user.getUsername().equals(comment.getUser().getUsername())) {
      throw new IllegalArgumentException(
          messageSource.getMessage(
              "only.author.edit",
              null,
              "Only author can edit",
              Locale.getDefault()
          )
      );
    }

    comment.setContent(commentRequestDto.getContent());
    comment.setImageUrl(commentRequestDto.getImageUrl());

    return new CommentResponseDto(comment);
  }

  // 댓글 삭제
  @Transactional
  public void deleteComment(Long commentId, User user) {

    Comment comment = findComment(commentId);

    if (!user.getUsername().equals(comment.getUser().getUsername())) {
      throw new IllegalArgumentException(
          messageSource.getMessage(
              "only.author.delete",
              null,
              "Only author can delete",
              Locale.getDefault()
          )
      );
    }
    commentRepository.delete(comment);
  }

  // 댓글 좋아요
  @Transactional
  public void likeComment(Long id, User user) {

    Comment comment = findComment(id);

    if (commentLikeRepository.existsByUserAndComment(user, comment)) {
      throw new DuplicateRequestException(
          messageSource.getMessage(
              "comment.already.liked",
              null,
              "Comment already been liked",
              Locale.getDefault()
          )
      );
    } else {
      CommentLike commentLike = new CommentLike(user, comment);
      commentLikeRepository.save(commentLike);
    }
  }

  // 댓글 좋아요 취소
  @Transactional
  public void dislikeComment(Long id, User user) {

    Comment comment = findComment(id);
    Optional<CommentLike> commentLikeOptional = commentLikeRepository.findByUserAndComment(user,
        comment);

    if (commentLikeOptional.isPresent()) {
      commentLikeRepository.delete(commentLikeOptional.get());
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

  // 해당 댓글이 DB에 존재하는지 확인
  private Comment findComment(Long id) {

    return commentRepository.findById(id).orElseThrow(() ->
        new NotFoundException(
            messageSource.getMessage(
                "comment.not.exist",
                null,
                "Comment does not exist",
                Locale.getDefault()
            )));
  }
}
