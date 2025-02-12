package hello.re_core.discount;

import hello.re_core.domain.Grade;
import hello.re_core.domain.Member;
import hello.re_core.order.Order;
import hello.re_core.order.OrderService;
import hello.re_core.order.OrderServiceImpl;
import hello.re_core.repository.MemberRepository;
import hello.re_core.repository.MemoryMemberRepository;
import hello.re_core.service.MemberService;
import hello.re_core.service.MemberServiceImpl;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class RateDiscountPolicyTest {
    private final MemberRepository memberRepository = new MemoryMemberRepository();
    private final DiscountPolicy disCountPolicy = new RateDiscountPolicy();

    @Test
    @DisplayName("VIP는 10% 할인이 적용되어야 한다.")
    public void vip_o() {
        // given
        Member member = new Member(1L, "memberVIP", Grade.VIP);

        // when
        int discount = disCountPolicy.discount(member, 10000);

        // then
        assertEquals(discount, 1000);
    }

    @Test
    @DisplayName("VIP가 아니면 10% 할인이 적용되지 않아야 한다.")
    public void vip_x() {
        // given
        Member member = new Member(1L, "memberBASIC", Grade.BASIC);

        // when
        int discount = disCountPolicy.discount(member, 10000);

        // then
        assertThat(discount).isEqualTo(0);
    }


    @Test
    @DisplayName("정률_할인_정책")
    public void RateDiscount() {
        // given
        Member member = new Member(1L, "memberA", Grade.VIP);

        // when
        MemberService memberService = new MemberServiceImpl(memberRepository);
        memberService.join(member);

        // memberService와 같은 memberRepsitory 객체 의존관계 주입
        // RateDiscountPolicy 객체 의존관계 주입
        OrderService orderService = new OrderServiceImpl(memberRepository, disCountPolicy);
        Order order = orderService.createOrder(1L, "itemA", 10000);
        int discounted_price = order.calculatePrice();

        // then
        assertEquals(order.getDiscountPrice(), 1000);

    }

}
