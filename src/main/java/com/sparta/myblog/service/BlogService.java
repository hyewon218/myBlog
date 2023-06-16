package com.sparta.myblog.service;

import com.sparta.myblog.dto.BlogDeleteResponseDto;
import com.sparta.myblog.dto.BlogRequestDto;
import com.sparta.myblog.dto.BlogResponseDto;
import com.sparta.myblog.entity.Blog;
import com.sparta.myblog.repository.BlogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@Service
public class BlogService {

    private final BlogRepository blogRepository;

    @Autowired
    public BlogService(BlogRepository blogRepository) {

        this.blogRepository = blogRepository;
    }

    // 전체 게시글 조회 API
    public List<BlogResponseDto> getPosts() {
        // DB 조회
        // stream 에서 blog 가 하나씩 빠져갈 거고 map 에 의해서 변환이 될 건데
        // Map To List
        // BlogResponseDto 의 생성자 중에서 Blog 를 파라미터로 가지고 있는 생성자가 호출이 되고
        // 그게 하나씩 변환이 되면서 그 뭉덩이를 List 타입으로 바꿔준다.
        return blogRepository.findAllByOrderByCreatedAtDesc().stream().map(BlogResponseDto::new).toList();
    }

    // 게시글 작성 API
    public BlogResponseDto createPost(BlogRequestDto requestDto) {
        // RequestDto -> Entity
        // 블로그 클래스 객체 하나가 데이터 베이스의 한줄, 한 row 이다.
        Blog blog = new Blog(requestDto);

        // DB 저장
        // 메모 한 줄 저장 후 Blog 반환
        Blog savePost = blogRepository.save(blog);

        // Entity -> ResponseDto
        BlogResponseDto blogResponseDto = new BlogResponseDto(savePost);

        return blogResponseDto;
    }

    // 선택한 게시글 조회 API
    public BlogResponseDto getPost(Long id) {

        Blog blog = findPost(id);

        // Entity -> ResponseDto
        BlogResponseDto blogResponseDto = new BlogResponseDto(blog);

        return blogResponseDto;
    }

    // 선택한 게시글 수정 API
    @Transactional
    public BlogResponseDto updatePost(Long id, BlogRequestDto blogRequestDto) {

        Blog blog = findPost(id);
        // 비밀번호 일치 여부 확인
        boolean checkPassword = checkPassword(id, blogRequestDto.getPassword());

        if (checkPassword) {
            // blog 내용 수정
            blog.update(blogRequestDto);
        }
        // Entity -> ResponseDto
        BlogResponseDto blogResponseDto = new BlogResponseDto(blog);

        return blogResponseDto;
    }

    // 선택한 게시글 삭제 API
    public BlogDeleteResponseDto deletePost(Long id, BlogRequestDto blogRequestDto) {
        boolean flag = true;

        Blog blog = findPost(id);

        // 비밀번호 일치 여부 확인
        boolean checkPassword = checkPassword(id, blogRequestDto.getPassword());

        if (checkPassword) {

            blogRepository.delete(blog);
        } else {
            flag = false;
        }

        // Entity -> ResponseDto
        BlogDeleteResponseDto blogDeleteResponseDto = new BlogDeleteResponseDto(flag);

        return blogDeleteResponseDto;
    }

    // 해당 메모가 DB에 존재하는지 확인
    private Blog findPost(Long id) {
        return blogRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("선택한 메모는 존재하지 않습니다.")
        );
    }

    // 비밀번호 일치 여부 확인
    public boolean checkPassword(Long id, String pass) {
        Blog blog = blogRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
        String realPassword = blog.getPassword();
        return Objects.equals(realPassword, pass);
    }


}