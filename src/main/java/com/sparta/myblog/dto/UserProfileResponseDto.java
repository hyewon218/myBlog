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
    private String email;
    private UserRoleEnum role;
    private String selfText;

    public UserProfileResponseDto(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.selfText = user.getSelfText();
    }

}
