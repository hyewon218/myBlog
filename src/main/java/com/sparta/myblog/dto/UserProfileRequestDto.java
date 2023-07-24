package com.sparta.myblog.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@Data
public class UserProfileRequestDto {
    private String username;
    private String selfText;
    private MultipartFile imageFile;
}
