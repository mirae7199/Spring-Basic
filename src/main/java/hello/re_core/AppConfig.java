package hello.re_core;

import hello.re_core.discount.DiscountPolicy;
import hello.re_core.discount.RateDiscountPolicy;
import hello.re_core.order.OrderService;
import hello.re_core.order.OrderServiceImpl;
import hello.re_core.repository.MemberRepository;
import hello.re_core.repository.MemoryMemberRepository;
import hello.re_core.service.MemberService;
import hello.re_core.service.MemberServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public MemberService memberService() {
        // 1번
        System.out.println("call AppConfig.memberService");
        return new MemberServiceImpl(memberRepository());
    }

    @Bean
    public OrderService orderService() {
        // 1번
        System.out.println("call AppConfig.orderService");
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }

    @Bean
    public MemberRepository memberRepository(){
        // 2번? 3번?
        System.out.println("call AppConfig.memberRepository");
        return new MemoryMemberRepository();
    }

    @Bean
    public DiscountPolicy discountPolicy() {
        return new RateDiscountPolicy();
    }


}
