package com.sparta.myblog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sparta.myblog.dto.PostRequestDto;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Builder
@Entity // JPA 가 관리할 수 있는 Entity(실제 DB에 저장되어 있는 데이터를 가지고 있음) 클래스 지정
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "post", uniqueConstraints = {
    @UniqueConstraint(name = "API_ID_UNIQUE", columnNames = {"apiId"})
}) // 매핑할 테이블의 이름을 지정

@EntityListeners(AuditingEntityListener.class) // 수정시간 update 위해 추가
@JsonIgnoreProperties(value = {"updatedDate"}, allowGetters = true)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto_increment 값을 자동으로 생성
    @Column(name = "post_id")
    private Long Id;

    @Builder.Default
    @Column(nullable = false, updatable = false, length = 50)
    private final String apiId = UUID.randomUUID().toString();

    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    //@Builder.Default
    @JsonIgnore
    // 부모 엔티티가 자식의 생명주기를 모두 관리할 수 있게 됨.
    // 다대일양방향
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentList;

    //@Builder.Default
    // 게시글 좋아요
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<PostLike> postLikes;

    //@Builder.Default
    // 게시글 다중 파일 첨부
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> ImagetList;

    @Builder
    public Post(PostRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.content = requestDto.getContent();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUser(User user) {
        this.user = user;
    }
}