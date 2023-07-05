package com.sparta.myblog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sparta.myblog.dto.PostRequestDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Entity // JPA 가 관리할 수 있는 Entity(실제 DB에 저장되어 있는 데이터를 가지고 있음) 클래스 지정
@Getter
@NoArgsConstructor
@Table(name = "post") // 매핑할 테이블의 이름을 지정
@EntityListeners(AuditingEntityListener.class) // 수정시간 update 위해 추가
@JsonIgnoreProperties(value={"updatedDate"}, allowGetters=true)
public class  Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto_increment 값을 자동으로 생성
    @Column(name = "post_id")
    private Long postId;

    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "username")
    private User user;

    @JsonIgnore
    // 부모 엔티티가 자식의 생명주기를 모두 관리할 수 있게 됨.
    // 다대일양방향
    @OneToMany(mappedBy = "post",  cascade = CascadeType.ALL, orphanRemoval = true)// 게시글이 삭제되면 그 게시글에 있는 댓글들 모두 삭제가 되도록
    private List<Comment> commentList;

    @Builder
    public Post(PostRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.content= requestDto.getContent();
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