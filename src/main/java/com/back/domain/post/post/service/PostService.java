package com.back.domain.post.post.service;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.post.postComment.entity.PostComment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public long count() {
        return postRepository.count();
    }

    public Post write(String title, String content) {
        return postRepository.save(new Post(title, content));
    }

    public Optional<Post> findById(long id) {
        return postRepository.findById(id);
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public void delete(Post post) {
        postRepository.delete(post);
    }

    public PostComment writeComment(Post post, String content) {
        return post.addComment(content);
    }

    public boolean deleteComment(Post post, PostComment postComment) {
        return post.deleteComment(postComment);
    }

    public void flush() {
        postRepository.flush();
    }
}
