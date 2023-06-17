package com.sparta.myblog.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sparta.myblog.dto.PostRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity // JPA가 관리할 수 있는 Entity(실제 DB에 저장되어 있는 데이터를 가지고 있음) 클래스 지정
//@Setter : Entity 데이터는 DB와 직결되는 데이터이기 때문에 @Setter 로는 잘 쓰지 않는다.
@Getter
@Table(name = "post") // 매핑할 테이블의 이름을 지정
@NoArgsConstructor
// 수정시간 update 위해 추가
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value={"updatedDate"}, allowGetters=true)
// 생성일시와 수정일시를 자동으로 갱신하기 위해 BaseEntity 를 상속 받음
public class  Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto_increment 값을 자동으로 생성
    private Long id;

    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false, length = 500)
    private String contents;

    @Column(nullable = false)
    private String password;


    public Post(PostRequestDto requestDto) {
        // id 는 @GeneratedValue 를 통해서 값을 자동으로 생성하도록 했기 때문에 id 는 필요 없다.
        this.title = requestDto.getTitle();
        this.author = requestDto.getAuthor();
        this.contents = requestDto.getContents();
        this.password = requestDto.getPassword();
    }

    // setter 메소드를 정의해 놓고 제한적으로 사용한다.
    public void setTitle(String title) {
        this.title = title;
    }
    public void setAuthor(String name) {
        this.author = name;
    }
    public void setContents(String content) {
        this.contents = content;
    }

    // Service 함수
    // 비밀번호 체크하는 함수
    public void checkPassword(String inputPassword) {
        if (!password.equals(inputPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }
}