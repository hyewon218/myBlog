package com.sparta.myblog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationDto {

    @Schema(name = "notificationId", example = "4f3dda35-3739-406c-ad22-eed438831d66", description = "알림 ID")
    @NotBlank
    private String notificationId;

    @Schema(name = "text", example = "작성한 글에 댓글이 달렸어요!", description = "알림 문구 그대로 보여주면 됨.")
    @NotBlank
    private String text;

    @Schema(name = "notificationArgsDto", example = "json 형식 객체", description = "알림 자세한 정보 담은 객체")
    private NotificationArgsDto notificationArgsDto;

    @Schema(name = "isRead", example = "true", description = "알림을 조회 했었는지, ui적으로 다르게 보이게 해주어야 함.")
    private Boolean isRead;

}
