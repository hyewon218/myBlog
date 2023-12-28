package com.sparta.myblog.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user")
@EqualsAndHashCode
public class User implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  @Enumerated(value = EnumType.STRING)
  private UserRoleEnum role;

  // 사용자의 자기소개
  @Column
  private String selfText;

  @Column
  private String imageUrl;

  public User(String username, String password, String email, UserRoleEnum role, String selfText,
      String imageUrl) {
    this.username = username;
    this.password = password;
    this.role = role;
    this.email = email;
    this.selfText = selfText;
    this.imageUrl = imageUrl;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setSelfText(String selfText) {
    this.selfText = selfText;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }
}