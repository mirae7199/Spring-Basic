package hello.re_core.discount;

import hello.re_core.domain.Member;

public interface DiscountPolicy {
    /**
     * @return 할인 대상 금액
     */

    int discount(Member member, int price);
}
