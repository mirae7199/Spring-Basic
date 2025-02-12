package hello.re_core.autowired;

import hello.re_core.AutoAppConfig;
import hello.re_core.discount.DiscountPolicy;
import hello.re_core.domain.Grade;
import hello.re_core.domain.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class AllBeanTest {

    @Test
    @DisplayName("모든 빈 조회하기")
    void findAllBean() {
        // 스프링 빈으로 등록된다.
        ApplicationContext ac = new AnnotationConfigApplicationContext(AutoAppConfig.class, DiscountService.class);
        DiscountService discountService = ac.getBean(DiscountService.class);
        Member member = new Member(1L, "userA", Grade.VIP);
        int discountPrice = discountService.discount(member, 10000, "fixDiscountPolicy");

        assertThat(discountPrice).isEqualTo(1000);
    }

    static class DiscountService {

        private final Map<String, DiscountPolicy> policyMap;
        private final List<DiscountPolicy> policies;

        // 생성자 하나 있으면 @Autowired 생략 가능!
        // 의존관계 자동 주입
        public DiscountService(Map<String, DiscountPolicy> policyMap, List<DiscountPolicy> policies) {
            this.policyMap = policyMap;
            this.policies = policies;

            System.out.println("policyMap = " + policyMap);
            System.out.println("policies = " + policies);
        }

        public int discount(Member member, int price, String discountCode) {

            DiscountPolicy discountPolicy = policyMap.get(discountCode);

            System.out.println("discountCode = " + discountCode);
            System.out.println("discountPolicy = " + discountPolicy); // 할인 정책 구현체들 정액/정률 할인 정책...등등

            return discountPolicy.discount(member, price); // 구현체마다 할인 정책이 달라짐,,,,
        }
    }
}
