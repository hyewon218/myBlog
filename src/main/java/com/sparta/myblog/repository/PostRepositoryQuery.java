package com.sparta.myblog.repository;

import com.sparta.myblog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryQuery {

  Page<Post> searchPost(PostSearchCond cond, Pageable pageable);
}
