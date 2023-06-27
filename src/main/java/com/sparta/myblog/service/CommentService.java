package com.sparta.myblog.service;

import com.sparta.myblog.dto.ApiResult;
import com.sparta.myblog.dto.CommentRequestDto;
import com.sparta.myblog.dto.CommentResponseDto;
import com.sparta.myblog.entity.Comment;
import com.sparta.myblog.entity.Post;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.entity.UserRoleEnum;
import com.sparta.myblog.repository.CommentRepository;
import com.sparta.myblog.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
    CommentRepository commentRepository;
    PostRepository postRepository;

    // 댓글 작성
    @Transactional
    public CommentResponseDto createComment(CommentRequestDto commentRequestDto, User user) {

        // 댓글을 작성할 게시글이 있는지 확인
        Post post = postRepository.findById(commentRequestDto.getPostId()).orElseThrow(
                () -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        );

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

        // 수정하려는 댓글이 존재하는지 확인
        Comment comment = findComment(commentId);

        if (this.checkValidUser(user, comment)) {
            throw new IllegalArgumentException("작성자만 수정/삭제할 수 있습니다.");
        }

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
    public ApiResult deleteComment(Long commentId, User user) {

        // 삭제하려는 댓글이 존재하는지 확인
        Comment comment = findComment(commentId);

        if (this.checkValidUser(user, comment)) {
            throw new IllegalArgumentException("작성자만 수정/삭제할 수 있습니다.");
        }
        commentRepository.delete(comment);

        return ApiResult.builder()
                .msg("댓글 삭제 성공")
                .statusCode(HttpStatus.OK.value())
                .build();
    }


    // 해당 댓글이 DB에 존재하는지 확인
    private Comment findComment(Long id) {
        // commnetRepository 에서 DB 를 통해 조회를 해온다.
        // commentRepository.findById(id) : JPA 기본 제공 메서드라 Optional 이라는 응답값으로 오게 됨 -> Optional 은 값이 없을 경우에도 처리를 해야 함
        return commentRepository.findById(id).orElseThrow(() ->  // orElseThrow 값이 없을 경우 어떤 예외를 던질지
                new IllegalArgumentException("해당 댓글이 존재하지 않습니다.")
        );
    }

    // 수정하려고 하는 댓글의 작성자가 본인인지, 관리자 계정으로 수정하려고 하는지 확인
    private boolean checkValidUser(User user, Comment comment) {
        return !(user.getUsername().equals(comment.getUser().getUsername())
                && user.getRole().equals(UserRoleEnum.ADMIN));
    }
}
