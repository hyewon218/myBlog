package com.sparta.myblog.repository;

import static com.sparta.myblog.entity.QPost.post;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.myblog.entity.Post;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostRepositoryQueryImpl implements PostRepositoryQuery {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public List<Post> searchPost(PostSearchCond cond) {
    var query = jpaQueryFactory.select(post)
        .from(post)
        .where(
            post.content.contains(cond.getKeyword())
        );

    var posts = query.fetch();

    return posts;
  }
}
