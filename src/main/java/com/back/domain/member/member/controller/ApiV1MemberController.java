package com.back.domain.member.member.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "ApiV1MemberController", description = "API 회원 컨트롤러")
public class ApiV1MemberController {
    private final MemberService memberService;


    public record MemberJoinReqBody(
            String username,
            String password,
            String name
    ) {
    }

    @PostMapping("/join")
    @Transactional
    @Operation(summary = "회원가입")
    public RsData<Void> join(
            @RequestBody MemberJoinReqBody reqBody
    ) {
        Member member = memberService.join(reqBody.username, reqBody.password, reqBody.name);

        return new RsData<>(
                "201-1",
                "%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getName())
        );
    }
}
