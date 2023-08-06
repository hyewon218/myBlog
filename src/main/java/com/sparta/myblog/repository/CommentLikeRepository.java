package com.sparta.myblog.repository;

import java.util.Optional;

import com.sparta.myblog.entity.Comment;
import com.sparta.myblog.entity.CommentLike;
import com.sparta.myblog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
  Optional<CommentLike> findByUserAndComment(User user, Comment comment);
  Boolean existsByUserAndComment(User user, Comment comment);
}