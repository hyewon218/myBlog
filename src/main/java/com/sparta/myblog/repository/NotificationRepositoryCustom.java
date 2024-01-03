package com.sparta.myblog.repository;

import com.sparta.myblog.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface NotificationRepositoryCustom {

    Slice<Notification> findSliceByCondition(Pageable pageable, Long memberId);
}