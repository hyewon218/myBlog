package com.sparta.myblog.dto;

import com.sparta.myblog.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileResponseDto {
  private Long id;
  private String username;
  private String selfText;
  private String imageUrl;

  public UserProfileResponseDto(User user) {
    this.id = user.getId();
    this.username = user.getUsername();
    this.selfText = user.getSelfText();
    this.imageUrl = user.getImageUrl();
  }
}
