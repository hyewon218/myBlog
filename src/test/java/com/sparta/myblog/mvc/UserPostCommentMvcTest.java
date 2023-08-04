package com.sparta.myblog.mvc;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.myblog.config.WebSecurityConfig;
import com.sparta.myblog.controller.PostController;
import com.sparta.myblog.controller.UserController;
import com.sparta.myblog.dto.PostRequestDto;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.entity.UserRoleEnum;
import com.sparta.myblog.file.AwsS3Service;
import com.sparta.myblog.file.FileStore;
import com.sparta.myblog.security.UserDetailsImpl;
import com.sparta.myblog.service.CommentService;
import com.sparta.myblog.service.PostService;
import com.sparta.myblog.service.UserService;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest( // controller 쪽 테스트를 할 수 있다.
    controllers = {UserController.class, PostController.class}, // 테스트할 controller 지정
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = WebSecurityConfig.class
        )
    }
)
public class UserPostCommentMvcTest {

  private MockMvc mvc;

  private Principal mockPrincipal;

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  UserService userService;

  @MockBean
  PostService postService;

  @MockBean
  AwsS3Service awsS3Service;

  @MockBean
  CommentService commentService;

  @MockBean
  FileStore fileStore;

  @BeforeEach
  public void setup() {
    mvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(springSecurity(new MockSpringSecurityFilter())) // 만들어둔 필터 넣어줌
        .build();
  }

  private void mockUserSetup() {
    // Mock 테스트 유져 생성
    String username = "apple0218";
    String password = "robbie1234!";
    String email = "qwerty@sparta.com";
    UserRoleEnum role = UserRoleEnum.USER;
    String selfText = "Hi. my name is....";
    User testUser = new User(username, password, email, role, selfText);
    UserDetailsImpl testUserDetails = new UserDetailsImpl(testUser);
    mockPrincipal = new UsernamePasswordAuthenticationToken(testUserDetails, "",
        testUserDetails.getAuthorities());
  }

  @Test
  @DisplayName("로그인 Page")
  void test1() throws Exception {
    // when - then
    mvc.perform(get("/view/user/login-page"))
        .andExpect(status().isOk())
        .andExpect(view().name("login"))
        .andDo(print());
  }

  @Test
  @DisplayName("회원 가입 요청 처리")
  void test2() throws Exception {
    // given
    MultiValueMap<String, String> signupRequestForm = new LinkedMultiValueMap<>();
    signupRequestForm.add("username", "apple0218");
    signupRequestForm.add("password", "robbie1234");
    signupRequestForm.add("email", "qwerty@sparta.com");
    signupRequestForm.add("admin", "false");

    // when - then
    mvc.perform(post("/view/user/signup")
            .params(signupRequestForm)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/view/user/login-page"))
        .andDo(print());
  }

  @Test
  @DisplayName("게시글 작성")
  void test3() throws Exception {
    // given
    this.mockUserSetup();

    PostRequestDto requestDto = new PostRequestDto("title1", "content1");
    String requestDtoJson = objectMapper.writeValueAsString(requestDto);
    MockMultipartFile request = new MockMultipartFile("notice", "notice", "application/json", requestDtoJson.getBytes(
        StandardCharsets.UTF_8));


    List<MultipartFile> imageFiles = List.of(
        new MockMultipartFile("test1", "test1.PNG", MediaType.MULTIPART_FORM_DATA_VALUE,
            "test1".getBytes()),
        new MockMultipartFile("test2", "test2.PNG", MediaType.MULTIPART_FORM_DATA_VALUE,
            "test2".getBytes())
    );

    // when, then
    mvc.perform(
            multipart("/view/posts")
                .file("images", imageFiles.get(0).getBytes())
                .file("images", imageFiles.get(1).getBytes())
                .file(request)
                .principal(mockPrincipal)
                .requestAttr("id",1)
                .contentType(MediaType.MULTIPART_FORM_DATA)) // 4
                .andExpect(status().isCreated())
                .andExpect(view().name("redirect:/view/posts/{id}"));

  }
}
