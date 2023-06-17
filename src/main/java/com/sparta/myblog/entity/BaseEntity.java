package com.sparta.myblog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
// MyBlogApplication 에서 추가한 @EnableJpaAuditing 이 추가가 되어 있으면 AuditingEntityListener 가 잘 동작하게 됨
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    // BaseEntity 를 상속받는 Entity 객체가 생성되었을 때 AuditingEntityListener 를 통해서 시간 값을 넣어주고 createdAt 필드에 저장한다.
    @CreatedDate
    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    // 작성날짜
    private LocalDateTime createdAt;

    // BaseEntity 를 상속받는 Entity 객체가 @LastModifiedDate 를 읽어서 수정되었을 경우에 수정된 일시를 modifiedAt 필드에 저장한다.
    @LastModifiedDate
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime modifiedAt;
}
