package com.sparta.myblog.repository;

import com.sparta.myblog.entity.Post;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.repository.RepositoryDefinition;


@RepositoryDefinition(domainClass = Post.class, idClass = Long.class)
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByApiId(String apiId);
    // 작성날짜 기준 내림차순
    List<Post> findAllByOrderByCreatedAtDesc();
}