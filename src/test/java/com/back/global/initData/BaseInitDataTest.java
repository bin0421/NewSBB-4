package com.back.global.initData;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class BaseInitDataTest {
    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("샘플 데이터 : 글 3건, 댓글 5건")
    void t1() {
        List<Post> posts = postRepository.findAll();

        assertThat(posts).hasSize(3);

        int commentsCount = posts.stream()
                .mapToInt(post -> post.getComments().size())
                .sum();

        assertThat(commentsCount).isEqualTo(5);
    }
}
