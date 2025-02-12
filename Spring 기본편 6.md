## 의존관계 자동 주입

### 다양한 의존관계 주입 방법
- 생성자 주입
- 수정자 주입 (setter 주입)
- 필드 주입
- 일반 메서드 주입

### 생성자 주입
- 생성자를 통해서 의존관계를 주입하는 것이다.
- 지금까지 의존관계를 주입한 방법이 생성자 주입이다.
- **불변, 필수** 의존관계에 필요

~~~ java
public class OrderServiceImpl implements OrderService {

	private MemberRepository memberRepository;
	private DiscountPolicy discountPolicy;

	@Autowired // 생성자가 하나만 있을 때 생략 가능하다!
	public OrderServiceImpl(MemberRepository memberRepository,
	DiscountPolicy discountPolicy) {
		this.memberRepository = memberRepository;
		this.discountPolicy = discountPolicy;
	}
}
~~~

생성자가 하나만 있으면 @Autowired 생략 가능! (스프링 빈에 등록된 클래스만 가능하다.)

### 수정자 주입(setter 주입)
- setter라 불리는 즉 자바빈 프로퍼티 규약의 수정자 메서드 방식이다.
- **선택, 변경** 가능성이 있는 의존관계 사용

~~~ java
public class OrderServiceImpl implements OrderService {

	private MemberRepository memberRepository;
	private DiscountPolicy discountPolicy;

	@Autowired 
	public void setMemberRepository(MemberRepository memberRepository) {
		this.memberRepository = memberRepsitory;
		}
	
	@Autowired 
	public void setDiscountPolicy(DiscountPolicy discountPolicy) {
		this.discountPolicy = discountPolicy;
	}
}
~~~

> 참고:  @Autowired의 기본 동작은 주입할 대상이 없으면 오류가 발생한다. 주입할 대상이 없어도 작동하려면 @Autowired(required = false)로 지정하면 된다.

### 필드 주입
- 필드에 바로 주입하는 방법이다.
- 코드가 간결해서 좋지만 외부에서 변경이 불가능해서 테스트하기 힘들다.
- DI 프레임워크가 없으면 아무것도 할 수 없다.
- 사용하지 말자!
	- 스프링 설정을 목적으로 하는 @Configuration 같은 곳에서만 특별한 용도로 사용.

~~~ java
public class OrderServiceImpl implements OrderService {

	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private DiscountPolicy discountPolicy;

~~~

> 참고: 순수한 자바 코드라면 당연히 @Autowired가 작동하지 않을 것이다. @SpringBootTest처럼 스프링 컨테이너를 테스트에 통합한 경우에만 가능하다.

### 일반 메서드 주입
- 일반 메서드를 통해서 주입받을 수 있다.
- 한번에 여러 필드를 주입 받을 수 있는 특징이 있다.
- 잘 사용 안함.

~~~ java
public class OrderServiceImpl implements OrderService {

	private MemberRepository memberRepository;
	private DiscountPolicy discountPolicy;

	@Autowired
	public void init(MemberRepository memberRepository, Discountpolicy discountpolicy) {
	this.memberRepository = memberRepository;
	this.discountPolicy = discountPolicy;
		}
	}
~~~

### 옵션 처리
주입할 스프링 빈이 없을 때에도 작동시켜야 할 때가 있다.
하지만 @Autowired 기본 설정은 required = true 로 되어 있어서 자동 주입 대상이 없으면 오류가 발생한다.

자동 주입 대상을 옵션을 처리하는 방법
- @Autowired(required = false):  자동 주입할 대상이 없으면 수정자 메서드가 호출이 안됨.
-  @Nullable: 자동 주입할 대상이 없으면 null이 입력된다.
- Optional<>: 자동 주입할 대상이 없으면 Optional.empty가 입력된다.

주입할 스프링 빈이 없다는 가정하에
~~~ java
// 메서드 자체가 호출이 안됨
@Autowired(required = false)
public void setNoBean1(Member member) {
	System.out.println(“setNoBean = “ + member);
}

// null 호출
@Autowired
public void setNoBean2(@Nullable Member member) {
	System.out.println(“setNoBean2 = “ + member);
}

// Optional.empty 호출
@Autowired(required = false)
public void setNoBean3(Optional<Member> member) {
	System.out.println(“setNoBean3 = “ + member);
}
~~~

- Member는 스프링 빈에 등록되어 있지 않다.

**실행 결과**
~~~
// setNoBean1()은 메서드 자체가 호출이 안된다.
setNoBean2 = null
setNoBean3 = Optional.empty
~~~

### 생성자 주입을 선택하라!!!
과거에는 수정자 주입, 필드 주입을 많이 사용했지만 최근에는 스프링을 포함한 DI 프레임워크 대부분이 생성자 주입을 권장한다.

**불변**
- 대부분의 의존관계 주입은 한번 주입하면 애플리케이션 종료시점까지 의존관계를 변경할 일이 없다. 오히려 애플리케이션 종료 전까지는 변하면 안된다. (불변 해야함)
- 수정자 주입을 사용하면 setXXX메서드를 public으로 열어 두어야 한다.
	- 어떤 개발자가 실수로 변경할 수 있고, 변경하면 안되는 메서드를 열어두는 것은 좋은 설계 방법이 아니다.
- 생성자 주입은 객체를 생성할 때 딱 1번만 호출되므로 이후에 호출되는 일이 없다. 따라서 불변하게 설계 가능하다.

**final 키워드**
생성자 주입시 필드에 final 키워드를 사용하면 혹시라도 생성자에 값이 들어오지 않는다면 오류를 컴파일 시점에 잡아준다.
~~~ java
public class OrderServiceImpl implements OrderService {

	private final MemberRepository memberRepository;
	private final DiscountPolicy discountPolicy;

	@Autowired 
	public OrderService(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
		this.memberRepository = memberRepository;
		// discountpolicy를 실수로 안넣음.
		// -> 컴파일 시점에서 오류 잡아줌		
	}
~~~
- 생성자에 discountPolicy를 설정해야 하지만 실수로 안넣음.
	- 자바가 컴파일 시점에 오류를 잡아줌
	- java: variable discountPolicy might not have been initialized
	- 컴파일 오류는 세상에 가장 빠르고 좋은 오류다!

> 참고: 수정자 주입을 포함한 나머지 주입 방식은 모두 생성자 이후에 호출되므로, 필드에 final 키워드를 사용할 수 없다. 오직 생성자 주입 방식만 final 키워드를 사용할 수 있다.

**정리**
- 생성자 주입 방식을 선택하는 이유는 여러가지가 있지만, 프레임워크에 의존하지 않고, 순수한 자바 언어의 특징을 잘 살리는 방법이기도 하다.
- 기본으로 생성자 주입을 사용하고, 필수 값이 아닌 경우에는 수정자 주입 방식을 옵션으로 부여하면 된다. 생성자 주입과 수정자 주입을 동시에 사용할 수 있다.
- 항상 생성자 주입을 선택하라! 그리고 가끔 옵션이 필요하면 수정자 주입을 선택하라! 필드 주입은 사용하지 않는 게 좋다.

### 롬복과 최신 트랜드
필드 주입 처럼 편리하게 사용하는 방법은 없을까?
개발자는 귀찮은 것은 못 참아!

- 롬복 적용해보기
- 롬복 라이브러리가 제공하는 @RequriedArgsConstructor 기능을 사용하면 final이 붙은 필드를 모아서 생성자를 자동으로 만들어준다. (즉 생성자를 따로 안만들어도 된다는 뜻)

~~~ java
@Component
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final MemberRepository memberRepository;
	private final DiscountPolicy discountPolicy;

}
~~~

롬복이 자바의 애노테이션 프로세서라는 기능을 이용해서 컴파일 시점에 생성자 코드를 자동으로 생성해준다. 실제 class를 열어보면 코드가 추가되어 있는 것을 확인할 수 있다.

~~~
public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountpolicy) {
	this.memberRepository = memberRepository;
	this.discountPolicy = discountPolicy;
}
~~~

### 조회 빈이 2개 이상일 때
@Autowired는 타입으로 조회한다.
한마디로 getBean(DiscountPolicy.class)로 조회한다고 볼 수 있다.

그런데 DiscountPolicy의 두 개의 구현체 모두 스프링 빈에 등록되어 있으면 어떻게 될까?

FixDiscountPolicy, RateDiscountPolicy를 모두 스프링 빈으로 등록하고 실행시키면 
NoUniqueBeanDefinitionException 오류가 발생한다.
~~~
 NoUniqueBeanDefinitionException: No qualifying bean of type
 'hello.core.discount.DiscountPolicy' available: expected single matching bean
 but found 2: fixDiscountPolicy,rateDiscountPolicy
~~~

### @Autowired 필드 명, @Qualifier, @primary
조회 빈이 2개 이상일 때 해결 방법
- @Autowired 필드 명 매칭
- @Qualifier -> @Qualifier끼리 매칭 -> 빈 이름 매칭
- @Primary 사용

### @Autowired 필드 명 매칭
@Autowired는 타입 매칭을 시도하고, 2개 이상의 빈이 조회된다면 필드 명, 파라미터 명으로 매칭한다.

**기존 코드**
~~~ java
@Autowired
private DiscountPolicy discountPolicy;
~~~
2개 이상의 빈이 조회 됨.

**필드 명을 빈 이름으로 변경**
~~~ java
@Autowired
private DiscountPolicy rateDiscountPolicy;
~~~
2개 이상의 빈이 조회 됨 -> 필드 명으로 매칭

필드 명이 rateDiscountPolicy 이므로 정상 주입된다.

### @Qualifier 사용
@Qualifier는 추가 구분자를 붙여주는 방법이다. 주입시 추가적인 방법을 제공하는 것이지 빈 이름을 변경하는 것은 아니다.

**빈 등록시 @Qualifier를 붙여 준다.**
~~~ java
@Component
@Qualifier(“mainDiscountPolicy”)
public class RateDiscountPolicy implements DiscountPolicy;{
}
~~~

~~~ java
@Component
@Qualifier(“fixDiscountPolicy”)
public class FixDiscountPolicy implements DiscountPolicy;{
}
~~~

주입 시에 @Qualifier를 붙여주고 등록한 이름을 적어준다.

**생성자 자동 주입 예시**
~~~ java
public OrderServiceImpl(MemberRepository memberRepository, @Qualifier(“mainDiscountPolicy”) DiscountPolicy discountPolicy) {
	this.memberRepository = memberRepository;
	this.discountPolicy = discountPolicy;
}
~~~

**수정자 자동 주입 예시**
~~~ java
public void setDiscountPolicy(@Qualifier(“fixDiscountPolicy”) DiscountPolicy discountPolicy) {
	this.discountPolicy = discountPolicy;
}
~~~

다음과 같이 직접 빈 등록시에도 @Qualifier를 사용할 수 있다.
~~~ java
@Bean
@Qualifier(“mainDiscountPolicy”)
public DiscountPolicy discountPolicy() {
return new ,,,
}
~~~

### @Primary 사용
@Primary는 우선순위를 정하는 방법이다. @Autowired 시에 여러 빈이 매칭되면 @Primary가 우선권을 갖는다.

rateDiscountPolicy가 우선권을 가지도록 하기
~~~ java
@Component
@Primary
public class RateDiscountPolicy implements DiscountPolicy {}

@Component
public class FixDiscountPolicy implements DiscountPolicy {}
~~~

**사용코드**
~~~ java
// 생성자
@Autowired
Public OrderServiceImpl(MembmerRepository memberRepository, DiscountPolicy discountPolicy) {
	this.memberRepository = memberRepository;
	this.discountPolicy = discountPolicy;
}

// 수정자
@Autowired
public void setDiscountPolicy(DiscountPolicy discountPolicy) {
	this.discountPolicy = discountPolicy;
}
~~~
코드를 실행해보면 RateDiscoutPolicy가 주입된 것을 알 수 있다.

@Primary, @Qualifier 중 어떤 것을 써야할 지 고민 될 것이다. @Qualifier의 단점은 주입 받을 때 모든 코드에 @Qualifier를 붙여주어야 한다는 점이다.

반면에 @Primary를 사용하면 귀찮게 모든 코드에 붙일 필요가 없다.

**우선 순위**
@Primary는 기본값 처럼 동작하는 것이고, @Qualifier는 매우 상세하게 동작한다. 스프링은 자동보다는 수동이, 넓은 범위보단 좁은 범위의 선택권이 우선 순위가 높다. 따라서 @Qualifier가 우선권이 높다.

### 조회할 빈이 모두 필요할 때, List, Map
스프링 빈이 모두 필요한 경우가 있다. 예를 들어 할인 서비스를 제공하는데 클라이언트가 할인의 종류(rate, fix)를 선택할 수 있다고 가정해보자. 스프링을 사용하면 전략 패턴을 매우 간단하게 구현할 수 있다.

~~~ java
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
 
		// 필드에 final를 선언했기 때문에 @Autowired를 생략해도 된다,,,
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
~~~

**로직 분석**
- DiscountService는 Map으로 모든 DiscountPolicy를 주입받는다. 이때 fixDiscountPolicy, rateDiscountPolicy가 주입된다.
- discount() 메서드는 discountCode로 “fixDiscountPolicy”가 넘어오면 map에서 fixDiscountPolicy 스프링 빈을 찾아서 실행한다. 물론 “rateDiscountPolicy”도 마찬가지다.

**주입 분석**
- Map<String, DiscountPolicy>: map의 key에 스프링 빈의 이름을 넣고,  value에 스프링 빈 이름의 해당하는 객체를 넣었다.
- List<DiscountPolicy>: DiscountPolicy 타입으로 조회한 모든 스프링 빈을 담아준다.


