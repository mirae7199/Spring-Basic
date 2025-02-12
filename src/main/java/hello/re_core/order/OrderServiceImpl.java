package hello.re_core.order;

import hello.re_core.discount.DiscountPolicy;
import hello.re_core.domain.Member;
import hello.re_core.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
//@RequiredArgsConstructor // 롬복에서 지원하는 라이브러리 final를 쓴 필드가 있다면 생성자를 생략해도 생성자로 쓸 수 있음,,(final을 쓴 필드만)
public class OrderServiceImpl implements OrderService {

    // final 키워드 생성자에 값이 설정되지 않는다면 오류를 컴파일 시점에 막아줌...
    private final MemberRepository memberRepository;
    private DiscountPolicy discountPolicy; // DiscountPolicy타입이 2개이상 있을때 이름으로 매칭

    // @Qualifier 추가 구분자
//    public OrderServiceImpl(MemberRepository memberRepository,
//                            @Qualifier("mainDiscountPolicy") DisCountPolicy rateDisCountPolicy) {
//        this.memberRepository = memberRepository;
//        this.rateDisCountPolicy = rateDisCountPolicy;
//    }

     //생성자 자동 주입
     //생성자가 하나만 있으면 생략해도 자동 주입 된다.
    @Autowired
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy disCountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = disCountPolicy;
    }

    // setter 자동 주입
    // 수정자 주입
//    @Autowired
//    public void setMemberRepository(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }
//
    @Autowired
    public void setDisCountPolicy(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;

    }

    // 필드 자동 주입
//    @Autowired
//    private MemberRepository memberRepository;
//    @Autowired
//    private DisCountPolicy disCountPolicy;

    // 일반 메서드 주입 (잘 안씀)
//    @Autowired
//    public void init(MemberRepository memberRepository, DisCountPolicy disCountPolicy) {
//        this.memberRepository = memberRepository;
//        this.disCountPolicy = disCountPolicy;
//    }

    @Override
    public Order createOrder(Long memberId, String itemName, int itemPrice) {
        Member member = memberRepository.findById(memberId); // 등급 때문에 할인을 하려면 등급을 알아야 함.
        int discountPrice = discountPolicy.discount(member, itemPrice); // 할인 가격을 알았죠

        return new Order(memberId, itemName, itemPrice, discountPrice);
    }
}
