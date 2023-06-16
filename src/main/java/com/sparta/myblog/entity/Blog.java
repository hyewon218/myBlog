package com.sparta.myblog.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sparta.myblog.dto.BlogRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity // JPA가 관리할 수 있는 Entity 클래스 지정
@Getter
@Setter
@Table(name = "blog") // 매핑할 테이블의 이름을 지정
@NoArgsConstructor
// 수정시간 update 위해 추가
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value={"updatedDate"}, allowGetters=true)
public class Blog extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto_increment
    // 저장하기
    // 제목 - title
    // 작성자명 - author
    // 작성내용 - contents
    // 비밀번호 - password

    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "author", nullable = false, length = 30)
    private String author;

    @Column(name = "contents", nullable = false, length = 500)
    private String contents;

    @Column(name = "password", nullable = false, length = 8)
    private String password;


    public Blog(BlogRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.author = requestDto.getAuthor();
        this.contents = requestDto.getContents();
        this.password = requestDto.getPassword();
    }

    // 변경할 데이터(requestDto) 받아와서 변경
    // 변경 감지 -> 영속성 컨테스트가 존재해야 함
    public void update(BlogRequestDto blogRequestDto) {
        this.title = blogRequestDto.getTitle();
        this.author = blogRequestDto.getAuthor();
        this.contents = blogRequestDto.getContents();
        // 비밀번호는 수정하지 않음
        //this.password = requestDto.getPassword();
    }
}