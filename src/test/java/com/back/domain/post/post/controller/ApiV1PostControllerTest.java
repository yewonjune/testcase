package com.back.domain.post.post.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiV1PostControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("초기 데이터: 글 3건, 댓글 5건")
    void t0() {
        assertThat(postService.count()).isEqualTo(3);
        assertThat(
                postService.findAll().stream().mapToInt(post -> post.getComments().size()).sum()
        ).isEqualTo(5);
    }

    @Test
    @DisplayName("글 작성")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "새 제목",
                                            "content": "새 내용"
                                        }
                                        """)
                )
                .andDo(print());

        Post post = postService.findAll().getLast();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글이 작성되었습니다.".formatted(post.getId())))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.createDate").exists())
                .andExpect(jsonPath("$.data.modifyDate").exists())
                .andExpect(jsonPath("$.data.title").value("새 제목"))
                .andExpect(jsonPath("$.data.content").value("새 내용"));

        assertThat(postService.count()).isEqualTo(4);
        assertThat(post.getTitle()).isEqualTo("새 제목");
    }

    @Test
    @DisplayName("글 작성, 제목이 비어있으면 400")
    void t2() throws Exception {
        mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "",
                                            "content": "새 내용"
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        title-NotBlank-must not be blank
                        title-Size-size must be between 2 and 100
                        """.strip()));

        assertThat(postService.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("글 작성, 내용이 비어있으면 400")
    void t3() throws Exception {
        mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "새 제목",
                                            "content": ""
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        content-NotBlank-must not be blank
                        content-Size-size must be between 2 and 5000
                        """.strip()));

        assertThat(postService.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("글 작성, JSON 형식이 잘못되면 400")
    void t4() throws Exception {
        mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "새 제목",
                                            "content": "새 내용"
                                        """)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("요청 본문이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("글 삭제")
    void t5() throws Exception {
        long id = 1;

        mvc
                .perform(delete("/api/v1/posts/" + id))
                .andDo(print())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글이 삭제되었습니다.".formatted(id)))
                .andExpect(jsonPath("$.data").doesNotExist());

        assertThat(postService.findById(id)).isEmpty();
        assertThat(postService.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("글 삭제, 존재하지 않는 글이면 404")
    void t6() throws Exception {
        long id = Integer.MAX_VALUE;

        mvc
                .perform(delete("/api/v1/posts/" + id))
                .andDo(print())
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));

        assertThat(postService.count()).isEqualTo(3);
    }
}
