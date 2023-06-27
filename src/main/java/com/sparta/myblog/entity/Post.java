package com.sparta.myblog.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sparta.myblog.dto.PostRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @Column(name = "post_id")
    private Long postId;

    private String title;

    @Column(nullable = false, length = 500)
    private String contents;

    // FetchType.LAZY 는 연관 관계로 걸린 엔티티가 참조 되어야 하는 시점에 읽는 방법.
    // JPA N + 1 Problem 을 방지하기 위한 가장 기초적인 옵션 값.
    @ManyToOne(fetch = FetchType.LAZY)
    // 외래 키를 매핑할 때 사용하는 어노테이션, name = "매핑할 외래 키 컬럼명", referencedColumnName = 대상 테이블의 컬럼명
    // 해당 어노테이션을 생략해도 연관 관계가 걸려 있을 경우, 자동으로 외래 키를 탐색함.
    @JoinColumn(name = "username", referencedColumnName = "username")
    private User user;


    public Post(PostRequestDto requestDto, User user) {
        // id 는 @GeneratedValue 를 통해서 값을 자동으로 생성하도록 했기 때문에 id 는 필요 없다.
        this.title = requestDto.getTitle();
        this.contents = requestDto.getContents();
        this.user = user;
    }

    public void update(PostRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.contents = requestDto.getContents();
    }

    // setter 메소드를 정의해 놓고 제한적으로 사용한다.
    //public void setTitle(String title) {
    //    this.title = title;
    //}
    //public void setContents(String content) {
    //    this.contents = content;
    //}


}