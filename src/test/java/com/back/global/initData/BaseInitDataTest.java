package com.back.global.initData;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
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
import static org.assertj.core.api.Assertions.tuple;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class BaseInitDataTest {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private MemberRepository memberRepository;

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

    @Test
    @DisplayName("샘플 데이터 : 회원 6명, 비밀번호는 모두 12345678")
    void t2() {
        List<Member> members = memberRepository.findAll();

        assertThat(members)
                .extracting(Member::getUsername, Member::getName, Member::getPassword)
                .containsExactly(
                        tuple("system", "시스템", "12345678"),
                        tuple("admin", "관리자", "12345678"),
                        tuple("user1", "유저1", "12345678"),
                        tuple("user2", "유저2", "12345678"),
                        tuple("user3", "유저3", "12345678"),
                        tuple("user4", "유저4", "12345678")
                );
    }
}
