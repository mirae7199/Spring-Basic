package hello.re_core.service;

import hello.re_core.AppConfig;
import hello.re_core.domain.Grade;
import hello.re_core.domain.Member;
import hello.re_core.repository.MemoryMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class MemberServiceTest {
    MemberService memberService;

    @BeforeEach
    void beforeEach(){
        AppConfig appConfig = new AppConfig();
        memberService = appConfig.memberService();
    }

    @Test
    @DisplayName("회원가입")
    void join() {
        // given
        Member memberA = new Member(1L, "memberA", Grade.VIP);
        // when
        memberService.join(memberA);
        // then
        Member findMember = memberService.findMember(1L);
        assertEquals(memberA.getName(), findMember.getName());
        assertThat(memberA).isEqualTo(findMember);
    }

}
