package com.sparta.myblog.repository;

import com.sparta.myblog.entity.Post;
import java.util.List;

public interface PostRepositoryQuery {

  List<Post> searchPost(PostSearchCond cond);
}
