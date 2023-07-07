package com.sparta.myblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// 데이터베이스 Entity 로 선언한 객체에 데이터를 생성, 수정할 때
// Auditing 이벤트를 받아서 jpa 가 생성일시나 수정일시를 입력할 수 있도록 Enable 해주겠다.
@EnableJpaAuditing
@SpringBootApplication // java application 을 SpringBoot application 으로.
public class MyBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBlogApplication.class, args);
    }

}
