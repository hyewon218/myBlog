package com.sparta.myblog.repository;

import static com.sparta.myblog.entity.QNotification.notification;
import static com.sparta.myblog.entity.QUser.user;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.myblog.entity.Notification;
import groovy.util.logging.Slf4j;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Notification> findSliceByCondition(Pageable pageable, Long memberId) {
        JPAQuery<Notification> query = queryFactory.select(notification)
            .from(notification)
            .join(notification.receiver, user).fetchJoin()
            .where(
                isMemberId(memberId)
            );
        for (Sort.Order o : pageable.getSort()) {
            PathBuilder pathBuilder = new PathBuilder(notification.getType(),
                notification.getMetadata());
            query.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC,
                pathBuilder.get(o.getProperty())));
        }
        List<Notification> alarms = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .fetch();
        boolean hasnext = false;
        if (alarms.size() == pageable.getPageSize() + 1) {
            hasnext = true;
            alarms.remove(alarms.size() - 1);
        }
        return new SliceImpl<>(alarms, pageable, hasnext);
    }

    private BooleanExpression isMemberId(Long memberId) {
        return memberId == null ? null : notification.receiver.id.eq(memberId);
    }

}