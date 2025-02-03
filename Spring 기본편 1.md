## 비즈니스 요구사항과 설계
- 회원
	- 회원을 가입하고 조회할 수 있다.
	- 회원은 BASIC과 VIP 두 가지 등급이 있다.
	- 회원 데이터는 자체 DB를 구축할 수 있고, 외부 시스템과 연동할 수 있다. (미확정 -> 추상화 생각하기)

요구사항을 보면 회원 저장소는 아직 정해지지 않았다. 정해질 때까지 무한정 기다릴 수는 없기 때문에 우리는 전에 배웠던 객체 지향 설계 방식으로 개발을 진행하면 된다. 

저장소를 인터페이스로 만들고 구현체를 만들면  나중에 변경이 되어도 쉽게 변경할 수 있을 것이다.

**회원 도메인 협력 관계**
![Image](https://github.com/user-attachments/assets/22389458-929e-49a1-991d-a60d3e573679)

**회원 클래스 다이어그램**
![Image](https://github.com/user-attachments/assets/579437d5-a507-4043-9e19-d9564d270f7d)

데이터베이스는 아직 확정이 안되었다. 따라서 무한정 기다릴 수는 없기 때문에 임시 저장소로 간단한 메모리 회원 저장소를 구현체로 만들었다.

**MemoryMemberRepository**
~~~ java
public class MemoryMemberRepository implements MemberRepository{  
  
    // HashMap은 동시성 이슈가 있으니 ConcurrentHashMap을 사용하자.(실무에서)  
    private final Map<Long, Member> store = new HashMap<>();  
  
    @Override  
    public void save(Member member) {  
        store.put(member.getId(), member);  
    }  
    @Override  
    public Member findById(Long id) {  
        return store.get(id);  
    }  
    public void clear() {  
        store.clear();  
    }
}
~~~

> 참고로 HashMap은 동시성 이슈가 있기 때문에 실무에서는 ConcurrentHashMap을 사용하자.


**MemberServiceImpl**
~~~ java
public class MemberServiceImpl implements MemberService{  
    private final MemberRepository memberRepository
			    = new MemoryMemberRepository();  
  
    @Override  
    public void join(Member member) {  
        memberRepository.save(member);  
    }  
    @Override  
    public Member findMember(Long memberId) {  
        return memberRepository.findById(memberId);  
    }
}
~~~


### 회원 도메인 - 회원 가입 테스트

애플리케이션 로직으로 테스트하는 것은 별로 좋은 방법이 아니다. JUnit 테스트를 이용하자

~~~ java
public class MemberServiceTest {  
    MemberService memberService = new MemberServiceImpl();  
  
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
~~~
test 실행 결과 성공적인것을 확인할 수 있다.


test할 때 given/when/then 으로 나눠서 하자!
- given: 값이 주어졌을 때
- when: 이런 사항이 일때
- then: 결과

JUnit과 assertj는 인터넷에 찾아 보기를..


#### 회원 도메인 설계의 문제점
- 이 코드의 설계상 문제점은 무엇인가?
- 다른 저장소로 변경할 때 OCP원칙을 잘 준수하는가?
- DIP를 잘 지키고 있는가?
- 의존관계가 인터페이스 뿐만 아니라 구현까지 모두 의존하는 문제점이 있다.
	- MemberRepository memberRepository = new MemoryMemberRepository();  

> OCP: 개방-폐쇄 원칙 (Open/closed principle)
> 
> "소프트웨어 요소는...확장에는 열려 있으나 변경에는 닫혀 있어야 한다."

> DIP: 의존관계 역전 원칙 (Dependency inversion principle) 
> 
> 프로그래머는 '추상화에 의존해야지, 구체화에 의존하면 안 된다.' 의존성 주입은 이 원칙을 따르는 방법 중 하나다.

### 주문과 할인 도메인 설계
- 주문과 할인 정책
	- 회원은 상품을 주문할 수 있다.
	- 회원 등급에 따라 할인 정책을 적용할 수 있다.
	- 할인 정책은 모든 VIP는 1000원을 할인해주는 고정 금액 할인을 적용 (나중에 변경 될 수 있음 -> 추상화 생각하기!)
	- 할인 정책은 변경 가능성이 높다. 회사의 할인 정책도 아직 정하지 않았으므로 인터페이스를 만들고 임시로 구현체를 만들 수 있도록 개발하자.

#### 주문 도메인 협력, 역할, 책임
![Image](https://github.com/user-attachments/assets/90db74a9-c397-4c16-a71f-ca0679616650)
1. 주문 생성: 클라이언트는 주문 서비스에 주문 생성을 요청한다.
2. 회원 조회: 할인을 하기위해서는 회원의 등급을 알아야하기 때문에 회원을 조회한다.
3. 할인 적용: 주문 서비스는 회원 등급에 따른 할인 여부를 할인 정책에 위임한다.
4. 주문 결과 반환: 주문 서비스는 할인 결과를 포함한 주문 결과를 반환한다.

**주문 도메인 클래스 다이어그램**
![Image](https://github.com/user-attachments/assets/64af6c4d-3388-42f3-9bc4-025b49133233)

**역할과 구현을 분리**해서 자유롭게 구현 객체를 조립할 수 있게 설계했다. 덕분에 회원 저장소는 물론이고, 할인 정책 또한 유연하게 변경할 수 있다.

회원을 메모리에서 조회하고, 정액 할인 정책(고정 금액) 또는 정률 할인 정책을 지원해도 주문 서비스를 변경하지 않아도 된다.(그것은 할인 정책에서 할 일이다.) 역할들의 협력 관계를 그대로 재사용 할 수 있다. 

**주문 서비스 구현체**
~~~ java
public class OrderServiceImpl implements OrderService {  
  
    private final MemberRepository memberRepository = new
MemoryMemberRepository();  
    private final DisCountPolicy disCountPolicy = new FixDiscountPolicy();  
   
    @Override  
    public Order createOrder(Long memberId, String itemName, int itemPrice) {  
        Member member = memberRepository.findById(memberId); // 등급 때문에 할인을 하려면 등급을 알아야 함.  
        int discountPrice = disCountPolicy.discount(member, itemPrice); // 할인 가격을 알았죠  
  
        return new Order(memberId, itemName, itemPrice, discountPrice);  
    }
}
~~~

하지만 여기서 문제점 하나

MemoryServiceImpl
~~~ java
public class MemberServiceImpl implements MemberService{  
    private final MemberRepository memberRepository = new MemoryMemberRepository();  
~~~

OrderServiceImpl
~~~ java
 public class OrderServiceImpl implements OrderService {

     private final MemberRepository memberRepository = new
 MemoryMemberRepository();
~~~
OrderServiceImpl 클래스에 createOrder() 메서드는 회원 저장소에서 회원을 조회하고 주문을 생성한다. 

하지만 여기서 문제점은 MemberServiceImpl 클래스에서 회원가입을 하고 그 회원 데이터가 회원 저장소에 있어야 하지만 여기서 memberRepository는 각각 다른 객체이다. 새로 객체를 만들었기 때문이다.

코드를 변경하면

**OrderServiceImpl 코드 변경**
~~~ java
 public class OrderServiceImpl implements OrderService {

    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;

	public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
		this.memberRespository = memberRepository;
		this.discountPolicy = discountPolicy;
	}
~~~

다른 클래스의 코드까지 모두 생성자 주입으로 받게 만들면 될 것이다.

**OrderApp**
~~~ java
public class OrderApp {  
    public static void main(String[] args) {  
        // 저장소 객체 생성  
        MemberRepository memberRepository = new MemoryMemberRepository();  
  
        // 같은 저장소 객체를 각각 주입  
        // 원래 문제점.  
        // MemberServiceImpl, OrderServiceImpl 각각의 코드에서 
        // Repository를 생성하다 보니  
        // 각각 다른 객체에 데이터를 저장하고 있었음...  
        // 정리하면 memberService에서 join()메서드로 회원을 저장시켜도  
        // orderService의 저장소는 다른 저장소이기 때문에 회원이 저장이 안되어 있다...  
        MemberService memberService = new MemberServiceImpl(memberRepository);  
        OrderService orderService = new OrderServiceImpl(memberRepository, new FixDiscountPolicy());  
  
        // 회원 가입  
        Long memberId = 1L;  
        Member member = new Member(memberId, "memberA", Grade.VIP);  
        memberService.join(member);  
  
        // 주문  
        Order order = orderService.createOrder(memberId, "itemA", 10000);  
  
        System.out.println("order = " + order);  
        System.out.println("order.calculatePrice = " + order.calculatePrice());  
  
    }
~~~

**문제점 정리**
- MemberServiceImpl, OrderServiceImpl 각각의 코드에서 memberRespository를 생성해서 회원을 저장하고 주문을 생성하니 서로 다른 저장소 객체에 저장하고 있었다. 
- 그러니 주문을 생성하더라도 member가 없어서 nullpointException이 뜨는게 아닌가

 이 문제는 의존관계 주입과 함께 뒤에서 다시 다룰 예정이다.





