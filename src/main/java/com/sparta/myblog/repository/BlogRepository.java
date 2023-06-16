package com.sparta.myblog.repository;

import com.sparta.myblog.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repository : 데이터베이스와 소통

public interface BlogRepository extends JpaRepository<Blog, Long> {
    // 작성날짜 기준 내림차순
    List<Blog> findAllByOrderByCreatedAtDesc();
}