package com.back.domain.post.postComment.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.dto.PostCommentDto;
import com.back.domain.post.postComment.entity.PostComment;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class ApiV1PostCommentController {
    private final PostService postService;

    record PostCommentWriteReqBody(
            @NotBlank
            @Size(min = 2, max = 100)
            String content
    ) {
    }

    @PostMapping
    @Transactional
    public ResponseEntity<RsData<PostCommentDto>> write(
            @PathVariable long postId,
            @Valid @RequestBody PostCommentWriteReqBody reqBody
    ) {
        Post post = postService.findById(postId).get();

        PostComment postComment = postService.writeComment(post, reqBody.content());

        // 댓글 id 를 응답에 포함시키기 위해 INSERT 를 즉시 반영
        postService.flush();

        RsData<PostCommentDto> rsData = new RsData<>(
                "201-1",
                "%d번 댓글이 작성되었습니다.".formatted(postComment.getId()),
                new PostCommentDto(postComment)
        );

        return ResponseEntity.status(rsData.getStatusCode()).body(rsData);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public RsData<Void> delete(
            @PathVariable long postId,
            @PathVariable long id
    ) {
        Post post = postService.findById(postId).get();

        PostComment postComment = post.findCommentById(id).get();

        postService.deleteComment(post, postComment);

        return new RsData<>(
                "200-1",
                "%d번 댓글이 삭제되었습니다.".formatted(id)
        );
    }
}
