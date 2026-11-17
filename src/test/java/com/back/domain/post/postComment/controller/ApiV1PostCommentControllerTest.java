package com.back.domain.post.postComment.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.entity.PostComment;
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
public class ApiV1PostCommentControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("댓글 작성")
    void t1() throws Exception {
        long postId = 1;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(postId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "새 댓글"
                                        }
                                        """)
                )
                .andDo(print());

        Post post = postService.findById(postId).get();
        PostComment postComment = post.getComments().getLast();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글이 작성되었습니다.".formatted(postComment.getId())))
                .andExpect(jsonPath("$.data.id").value(postComment.getId()))
                .andExpect(jsonPath("$.data.createDate").exists())
                .andExpect(jsonPath("$.data.modifyDate").exists())
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andExpect(jsonPath("$.data.content").value("새 댓글"));

        assertThat(post.getComments()).hasSize(4);
        assertThat(postComment.getContent()).isEqualTo("새 댓글");
    }

    @Test
    @DisplayName("댓글 작성, 내용이 비어있으면 400")
    void t2() throws Exception {
        long postId = 1;

        mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(postId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": ""
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        content-NotBlank-must not be blank
                        content-Size-size must be between 2 and 100
                        """.strip()));

        assertThat(postService.findById(postId).get().getComments()).hasSize(3);
    }

    @Test
    @DisplayName("댓글 작성, 존재하지 않는 글이면 404")
    void t3() throws Exception {
        long postId = Integer.MAX_VALUE;

        mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(postId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "새 댓글"
                                        }
                                        """)
                )
                .andDo(print())
                .andExpect(handler().methodName("write"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("댓글 삭제")
    void t4() throws Exception {
        long postId = 1;
        long id = 1;

        mvc
                .perform(delete("/api/v1/posts/%d/comments/%d".formatted(postId, id)))
                .andDo(print())
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글이 삭제되었습니다.".formatted(id)))
                .andExpect(jsonPath("$.data").doesNotExist());

        Post post = postService.findById(postId).get();
        assertThat(post.findCommentById(id)).isEmpty();
        assertThat(post.getComments()).hasSize(2);
    }

    @Test
    @DisplayName("댓글 삭제, 존재하지 않는 댓글이면 404")
    void t5() throws Exception {
        long postId = 1;
        long id = Integer.MAX_VALUE;

        mvc
                .perform(delete("/api/v1/posts/%d/comments/%d".formatted(postId, id)))
                .andDo(print())
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));

        assertThat(postService.findById(postId).get().getComments()).hasSize(3);
    }

    @Test
    @DisplayName("댓글 삭제, 다른 글의 댓글이면 404")
    void t6() throws Exception {
        long postId = 2;
        long id = 1; // 1번 댓글은 1번 글의 댓글

        mvc
                .perform(delete("/api/v1/posts/%d/comments/%d".formatted(postId, id)))
                .andDo(print())
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"));

        assertThat(postService.findById(1L).get().findCommentById(id)).isPresent();
    }
}
