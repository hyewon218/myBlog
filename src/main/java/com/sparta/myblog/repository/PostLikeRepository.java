package com.sparta.myblog.repository;

import com.sparta.myblog.entity.Post;
import com.sparta.myblog.entity.PostLike;
import com.sparta.myblog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Boolean existsByUserAndPost(User user, Post post);
    Optional<PostLike> findByUserAndPost(User user, Post post);
}
