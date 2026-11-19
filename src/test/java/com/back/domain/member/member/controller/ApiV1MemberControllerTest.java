package com.back.domain.member.member.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import org.hamcrest.Matchers;
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
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1MemberControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("회원가입")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "usernew",
                                            "password": "12345678",
                                            "name": "무명"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("무명님 환영합니다. 회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));

        Member member = memberService.findByUsername("usernew").get();

        assertThat(member.getPassword()).isEqualTo("12345678");
        assertThat(member.getName()).isEqualTo("무명");
    }

    @Test
    @DisplayName("회원가입, 요청 본문 길이 제약 위반")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "abc",
                                            "password": "1234567",
                                            "name": "무"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        name-Size-size must be between 2 and 30
                        password-Size-size must be between 8 and 30
                        username-Size-size must be between 4 and 30
                        """.stripIndent().trim()));

        assertThat(memberService.findByUsername("abc")).isEmpty();
    }

    @Test
    @DisplayName("회원가입, 이미 사용중인 username, 409")
    void t3() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "user1",
                                            "password": "12345678",
                                            "name": "무명"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.resultCode").value("409-1"))
                .andExpect(jsonPath("$.msg").value("user1(은)는 이미 사용중인 username 입니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("내 정보")
    void t4() throws Exception {
        Member actor = memberService.findByUsername("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members/me")
                                .param("actorId", String.valueOf(actor.getId()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(actor.getId()))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(actor.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.updateDate").value(Matchers.startsWith(actor.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.name").value("유저1"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
