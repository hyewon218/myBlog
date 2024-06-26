package com.sparta.myblog.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

import java.util.List;

@Getter
@Entity
@Table(name = "comment", uniqueConstraints = {
    @UniqueConstraint(name = "API_ID_UNIQUE", columnNames = {"apiId"})
})
public class Comment extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "comment_id")
  private long commentId;

  @Builder.Default
  @Column(nullable = false, updatable = false, length = 50)
  private final String apiId = UUID.randomUUID().toString();

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "image_url")
  private String imageUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "user_id")
  private User user;

  @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE)
  private List<CommentLike> commentLikes;

  public void setContent(String content) {
    this.content = content;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void setPost(Post post) {
    this.post = post;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
