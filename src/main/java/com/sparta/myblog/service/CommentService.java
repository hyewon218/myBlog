package com.sparta.myblog.service;

import com.sparta.myblog.dto.ApiResponseDto;
import com.sparta.myblog.dto.CommentRequestDto;
import com.sparta.myblog.dto.CommentResponseDto;
import com.sparta.myblog.entity.Comment;
import com.sparta.myblog.entity.Post;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.repository.CommentRepository;
import com.sparta.myblog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // 댓글 작성
    @Transactional
    public CommentResponseDto createComment(CommentRequestDto commentRequestDto, User user) {
        // 선택한 게시글의 DB 저장 유무를 확인하기
        Post post = postRepository.findById(commentRequestDto.getPostId()).orElseThrow(
                () -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        );

        // 선택한 게시글이 있다면 댓글을 등록하고 등록된 댓글 반환하기
        // 댓글 저장
        Comment comment = new Comment();
        comment.setContent(commentRequestDto.getContent());
        comment.setUser(user);
        comment.setPost(post);

        commentRepository.save(comment);

        return CommentResponseDto.builder()
                .postId(post.getPostId())
                .commentId(comment.getCommentId())
                .content(comment.getContent())
                .username(user.getUsername())
                .build();
    }

    // 댓글 수정
    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto commentRequestDto, User user) {
        // 선택한 댓글의 DB 저장 유무를 확인하기
        Comment comment = findComment(commentId);

        if (!user.getUsername().equals(comment.getUser().getUsername())) {
            throw new IllegalArgumentException("작성자만 수정/삭제할 수 있습니다.");
        }

        // 선택한 댓글이 있다면 댓글 수정하고 수정된 댓글 반환하기
        comment.setContent(commentRequestDto.getContent());
        commentRepository.save(comment);

        return CommentResponseDto.builder()
                .postId(comment.getPost().getPostId())
                .commentId(comment.getCommentId())
                .username(comment.getUser().getUsername())
                .content(comment.getContent())
                .build();
    }

    // 댓글 삭제
    @Transactional
    public ApiResponseDto deleteComment(Long commentId, User user) {
        // 선택한 댓글의 DB 저장 유무를 확인하기
        Comment comment = findComment(commentId);

        if (!user.getUsername().equals(comment.getUser().getUsername())) {
            throw new IllegalArgumentException("작성자만 수정/삭제할 수 있습니다.");
        }
        // 선택한 댓글이 있다면 댓글 삭제하고 Client 로 성공했다는 메시지, 상태코드 반환하기
        commentRepository.delete(comment);

        return ApiResponseDto.builder()
                .msg("댓글 삭제 성공")
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    // 해당 댓글이 DB에 존재하는지 확인
    private Comment findComment(Long id) {
        // commnetRepository 에서 DB 를 통해 조회를 해온다.
        return commentRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 댓글이 존재하지 않습니다.")
        );
    }
}
