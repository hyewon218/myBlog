package com.sparta.myblog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDto {
  private String title;
  private String content;
  private String imageUrl;

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }
}