package com.sparta.myblog.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "comment")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long commentId;

    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", referencedColumnName = "username")
    private User user;

    public void setContent(String content) {
        this.content = content;
    }
    public void setPost(Post post) {
        this.post = post;
    }
    public void setUser(User user) {
        this.user = user;
    }
}
