package hello.re_core.order;

import hello.re_core.AppConfig;
import hello.re_core.discount.FixDiscountPolicy;
import hello.re_core.domain.Grade;
import hello.re_core.domain.Member;
import hello.re_core.repository.MemberRepository;
import hello.re_core.repository.MemoryMemberRepository;
import hello.re_core.service.MemberService;
import hello.re_core.service.MemberServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {
    MemberService memberService;
    OrderService orderService;

    @BeforeEach
    void beforEach(){
        AppConfig appConfig = new AppConfig();
        memberService = appConfig.memberService();
        orderService = appConfig.orderService();
    }

    @Test
    void createOrder() {
        // given
        Long memberId = 1L;
        Member memberA = new Member(memberId, "memberA", Grade.VIP);
        // when
        memberService.join(memberA);
        Order order = orderService.createOrder(memberId, "itemA", 10000);

        // then
        assertEquals(order.calculatePrice(), 9000);
        assertThat(order.getDiscountPrice()).isEqualTo(1000);
    }

}
