package com.sparta.myblog.controller;

import com.sparta.myblog.dto.PostResponseDto;
import com.sparta.myblog.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PostService postService;

    // 게시글 목록 전체 조회
    @GetMapping("/")
    public String getPosts(Model model) {
        log.info("게시글 전체 조회");
        List<PostResponseDto> dto = postService.getPosts2();
        model.addAttribute("posts", dto);
        log.info(String.valueOf(dto.size())); // 게시글 갯수
        return "index";
    }
}
