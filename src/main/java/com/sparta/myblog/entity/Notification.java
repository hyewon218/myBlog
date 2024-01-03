package com.sparta.myblog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Builder
@Table(name = "notification", indexes = {
    @Index(name = "notification_read_index", columnList = "is_read")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Notification extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Builder.Default
    @Column(nullable = false, updatable = false, length = 50)
    private final String apiId = UUID.randomUUID().toString();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    // 알람을 받는 사람
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    private String content;

    private String url;

    @Builder.Default
    @Column(nullable = false)
    private boolean isRead;

    /**
     * 알람 기능이 확장 될 때 필요한 정보들을 저장.
     * json 형태로 저장하면 각 정보를 Column 으로 저장하는 것 보다 여러 알림 형태에 대응하기 좋음.
     * ex) 이벤트가 발생한 글 정보를 저장하여 링크 클릭 시 그 글로 이동할 수 있도록 함.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private NotificationArgs notificationArgs;


    public static Notification of(NotificationType notificationType, NotificationArgs notificationArgs, User calledMember) {
        Notification notification = Notification.builder()
            .notificationType(notificationType)
            .notificationArgs(notificationArgs)
            .build();
        notification.setReceiver(calledMember);
        return notification;
    }

    public void read() {
        this.isRead = true;
    }

    private void setReceiver(User receiver) {
        this.receiver = receiver;
        receiver.getNotifications().add(this);
    }
}