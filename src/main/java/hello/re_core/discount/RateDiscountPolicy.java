package hello.re_core.discount;

import hello.re_core.domain.Grade;
import hello.re_core.domain.Member;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
// @Qualifier("mainDiscountPolicy")
@Primary // 우선권을 갖음
public class RateDiscountPolicy implements DiscountPolicy {
    private final int discountPercent = 10; // 10% 할인

    @Override
    public int discount(Member member, int price) {
        if(member.getGrade() == Grade.VIP){
            return price * discountPercent / 100;
        }else {
            return 0;
        }
    }
}
