package com.sparta.myblog.repository;

import static com.sparta.myblog.entity.QPost.post;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.myblog.entity.Post;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostRepositoryQueryImpl implements PostRepositoryQuery {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Page<Post> searchPost(PostSearchCond cond, Pageable pageable) {
    var query = jpaQueryFactory.select(post)
        .from(post)
        .where(
            contentContains(cond.getKeyword())
        )
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize());

    var posts = query.fetch();

    long totalSize = jpaQueryFactory.select(Wildcard.count)
        .from(post)
        .where(contentContains(cond.getKeyword()))
        .fetch().get(0);

    return PageableExecutionUtils.getPage(posts, pageable, () -> totalSize);

  }

  private static BooleanExpression contentContains(String keyword) {
    return Objects.nonNull(keyword) ? post.content.contains(keyword) : null;
  }
}
