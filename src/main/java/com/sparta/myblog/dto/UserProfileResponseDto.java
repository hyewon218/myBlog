package com.sparta.myblog.dto;

import com.sparta.myblog.entity.User;
import com.sparta.myblog.entity.UserRoleEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileResponseDto {
    private Long id;
    private String username;
    private String selfText;
    private String imageFile;

    public UserProfileResponseDto(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.selfText = user.getSelfText();
        this.imageFile = user.getImageFile();
    }

}
