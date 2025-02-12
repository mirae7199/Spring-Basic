## 새로운 할인 정책 개발
기본적인 로직을 개발을 완료했다. 하지만..
기획자로 부터 할인 정책을 정률 할인 정책으로 바꿔야 하겠다는게 아닌가..
우리는 이런 상황을 대비해서 추상화를 구현하도록 개발했다.

**RateDiscountPolicy 코드 추가하기**
~~~ java
public class RateDiscountPolicy implements DiscountPolicy {
	private final discount = 10; // 10% 할인

	public int discount(Member member, int price) {
		if(member.getGrade == Grade.VIP) {
			return price * 10 / 100;
		}else {
			return 0;
		}
	}

}
~~~


### 새로운 할인 정책 적용과 문제점

**새로운 할인 정책 적용하기**
~~~ java
public class OrderServiceImpl implements OrderService {
 //    private final DiscountPolicy discountPolicy = new FixDiscountPolicy();

     private final DiscountPolicy discountPolicy = new RateDiscountPolicy();
 }
 ~~~
 **문제점 발견**
 - 역할와 구현을 충실하게 분리했다 -> OK
 - OCP, DIP 같은 객체지향 설계 원칙을 충실히 준수했다.
	 -    -> NOPE
- DIP -> 주문서비스 클라이언트(OrderServiceImpl)는 DiscountPolicy 인터페이스 뿐만 아니라 RateDiscountPolicy 구현체까지 구체적으로 의존한다.
	- 추상(인터페이스) 의존: DiscountPolicy
	- 구체(구현체) 의존: FixDiscountPolicy, RateDiscountPolicy
- OCP -> 지금 코드는 기능을 확장해서 변경하면, 클라이언트 코드에 영향을 준다. 따라서 OCP 위반!

지금까지는 OrderServiceImpl가 DiscountPolicy만을 의존하는 줄 알았다.

하지만 실제 의존관계를 보면 

![Image](https://github.com/user-attachments/assets/3111f585-609d-4eca-af15-66b67b61078c)
OrderServiceImpl는 DIscountPolicy뿐 아니라 RateDiscountPolicy, FixDiscountPolicy까지 의존하고 있던게 아닌가!

DIP 위반!

그러니 할인 정책을 바꾸려면 클라이언트 코드(OrderServiceImpl)를 바꿔야 했던 것이다.

OCP 위반!

문제를 해결할 방안이 무엇이 있을까?
- 클라이언트 코드가 인터페이스와 구체 모두 의존함.
- **DIP 위반!** ->  추상에만 의존하도록 변경

### 인터페이스만 의존하도록 코드 변경

![Image](https://github.com/user-attachments/assets/79fd879d-b68c-4984-a256-a01ad8ee96d9)

**인터페이스에만 의존하도록 코드 변경**
~~~ java
public class OrderServiceImpl implements OrderService {
     private final DiscountPolicy discountPolicy;

	}
 }
~~~
- 인터페이스만 의존하도록 코드를 변경했다.
- 그런데 구현체가 없어 실행이 불가하다.
- 실행하면 NPE(null pointer exception) 발생

그렇다면 문제를 해결하려면 도데체 어떻게 해야하는 걸까?

**해결방안**
- 누군가가 클라이언트 OrderServiceImpl에 DiscountPolicy의 구현체를 대신 생성하고 객체를 주입해주어야 한다.


### 관심사의 분리
애플리케이션을 공연이라고 생각하자. 인터페이스는 배역(로미오, 줄리엣), 구현체는 배역을 맡는 
배우(디카프리오)이다. 지금까지 우리가 설계했던 것을 보면 디카프리오(구현체)가 줄리엣의 역할(인터페이스)을 맡는 배우(구현체)를 직접 캐스팅한 것과 같다. 디카프리오는 공연을 하면서, 배우를 캐스팅하는 다양한 책임을 갖고 있는 것이다. 

- 배우는 연기만 잘하면 된다.(한가지 책임)
- 디카프리오는 여자 주인공이 누가 선택되든 똑같이 공연을 해야한다.
- 공연을 기획하고, 배우를 캐스팅하고 이런 일들은 **공연 기획자**가 알아서 할 일이다.

### AppConfig 등장
- 애플리케이션의 전체 동작 방식을 구성(config)하기 위해, 구현 객체를 생성하고, 연결하는 책임을 가지는 별도의 설정 클래스 만들기

**AppConfig**
~~~ java
public class AppConfig {

     public MemberService memberService() {
         return new MemberServiceImpl(new MemoryMemberRepository());

}

     public OrderService orderService() {
         return new OrderServiceImpl(
	         new MemoryMemberRespository(),
	         new FixDiscountPolicy());
	}

}
~~~
- AppConfig는 애플리케이션의 실제 동작에 필요한 구현 객체를 생성한다.
	- MemberServiceImpl
	- OrderServiceImpl
	- MemoryMemberRepository
	- FixDiscountPolicy
- AppConfig는 생성한 객체 인스턴스의 참조를 생성자를 통해서 주입해준다.
	- MemberServiceImpl -> MemoryMemberRespository
	- OrderServiceImpl -> MemoryMemberRespository, FixDiscountPolicy

**MemberServiceImpl 생성자 주입으로 코드 변경**
~~~ java
public class MemberServiceImpl implements MemberService {

     private final MemberRepository memberRepository;

     public MemberServiceImpl(MemberRepository memberRepository) {
         this.memberRepository = memberRepository;

	}

     public void join(Member member) {
         memberRepository.save(member);

	}

     public Member findMember(Long memberId) {
         return memberRepository.findById(memberId);

	} 
	
}
~~~

- 기존 MemberServiceImpl는 인터페이스, 구현체 모두 의존하고 있었지만 생성자 주입으로 변경후 에는 인터페이스만을 의존하고 있다. 
-  MemberServiceImpl는 생성자 주입을 통해서 외부에서 의존관계를 주입받기 때문에 어떤 구현체가 들어올지 알 수 없다.
- 생성자를 통해서 어떤 구현체가 들어올지는 오직 외부(AppConfig)에서 결정된다.
- MemberService는 어떤 구현체가 들어오든 자신에 로직만 수행하면 된다.

**클래스 다이어그램**

![Image](https://github.com/user-attachments/assets/cc2ee2a3-6a58-43d8-baa3-06f005eb54bb)
- AppConfig는 어떤 구현체를 쓸 것인지 결정한다.
- 또한 생성자 주입을 통해서 구현체를 주입해준다.
- DIP 완성: MemberService는 인터페이스 MemberRepository만 의존한다.
- OCP 완성: 할인 정책을 변경하더라도 AppConfig 코드만 변경하면 되니 클라이언트 코드는 변경하지 않아도 된다.

### AppConfig 리팩터링
중복 코드가 있고, 역할에 따른 구현이 잘 안보임

~~~ java
public class AppConfig {

     public MemberService memberService() {
         return new MemberServiceImpl(memoryMemberRepository());

}

     public OrderService orderService() {
         return new OrderServiceImpl(
	         memoryMemberRespository(),
	         fixDiscountPolicy());
	}

	public MemberRepository memoryMemberRespository(){
		return new MemoryMemberRepository();
	}

	public DiscountPolicy fixDiscountPolicy(){
		return new FixDiscountPolicy();
	}

}
~~~
- new MemoryMemberRepository() 이 부분이 중복 제거되었다. Repository를 다른 구현체로 변경할 때 한 부분만 변경하면 된다.
- AppConfig를 보면 역할과 구현 클래스가 한눈에 들어온다. 애플리케이션 전체 구성이 어떻게 되어있는지 빠르게 파악할 수 있다.

### 좋은 객체 지향 설계의 5가지 원칙의 적용
여기서 3가지 SRP, OCP, DIP 적용하였다.

#### SRP 단일 책임 원칙(Single responsibility principle)
> 한 클래스는 하나의 책임만 가져야 한다.

- 기존 클라이언트 객체는 구현체를 직접 생성하고 연결하고 실행하는 다양한 책임을 지고 있었음.
- SRP 단일 책임 원칙을 따르면서 관심사를 분리함.
- AppConfig가 구현체를 생성하고 연결해줌.
- 클라이언트 객체 실행만 하면 됨.

#### DIP 의존관계 역전 원칙
> 프로그래머는 "추상화에 의존해야지, 구체화에 의존하면 안된다." 의존성 주입은 이 원칙을 따르는 
> 방법 중 하나이다.

- 기존 클라이언트 객체가 인터페이스, 구현체 모두 의존 하고 있었음.
- 인터페이스만 의존하도록 변경했지만, 구현체가 없어 NPE 발생
- AppConfig에게 구현체 생성과 연결을 담당하게 함.
- AppConfig가 클라이언트 객체 의존관계를 주입 함. DIP 원칙을 지키면서 문제도 해결함.

#### OCP 개방-패쇄 원칙 (Open/closed principle)
> 소프트웨어 요소는... 확장에는 열려 있으나 변경에는 닫혀 있어야 한다.

- 다형성을 사용하고 클라이언트가 DIP를 지킴
- 애플리케이션을 사용 영역과 구성 영역으로 나눔
- AppConfig가 구현체 생성과 연결을 담당함.
- 클라이언트 코드 변경 안해도 됨.

### 제어의 역전 IoC(Inversion of Control)
- 기존 프로그램은 클라이언트 객체가 스스로 필요한 서버 구현 객체를 생성하고, 연결하고 실행했다. 한마디로 구현 객체가 프로그램의 제어 흐름을 스스로 조종하였다.
- 반면 AppConfig가 등장한 이후에 클라이언트 객체는 자신의 로직만 실행하면 된다.
- 프로그램의 제어 흐름에 대한 권한은 모두 AppConfig가 가지고 있다. OrderService의 구현 객체 OrderServiceImpl를 생성하며 또는 다른 구현 객체를 생성하기도 할 수 있다. OrderServiceImpl는 이런 사실도 모른체 자기 로직만 실행할 뿐이다.
- 이렇듯 프로그램의 제어 흐름을 외부에서 관리하는 것을 제어의 역전(IoC)라 한다.

### IoC 컨테이너, DI 컨테이너
- AppConfig 처럼 객체를 생성하고 관리하면서 의존관계를 연결해 주는 것을 IoC 컨테이너 또는 DI(Dependency Injection) 컨테이너라고 한다.
- 의존관계 주입에 초점을 두어 최근에는 DI 컨테이너라고도 함


## 스프링으로 전환하기
지금까지는 순수 자바 코드만으로 DI를 적용했다. 이제 스프링을 사용해보자

**AppConfig 스프링 기반으로 변경**
~~~ java
@Configuration
public class AppConfig {

    @Bean    
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }

    @Bean    
    public OrderService orderService() {
        return new OrderServiceImpl(
                memberRepository(),
                discountPolicy());

}

    @Bean    
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    @Bean    
    public DiscountPolicy discountPolicy() {
        return new RateDiscountPolicy();
    }

}
~~~
- AppConfig에 설정을 구성한다는 뜻의 @Configuration을 붙여준다.
- 각 메서드에 @Bean을 붙여준다. 스프링 컨테이너에 스프링 빈으로 등록되는 것이다.

**OrderApp에 스프링 컨테이너 적용**
~~~ java
public class OrderApp {

public static void main(String[] args) {
        ApplicationContext applicationContext = new
AnnotationConfigApplicationContext(AppConfig.class);

        MemberService memberService =
applicationContext.getBean("memberService", MemberService.class);

        OrderService orderService = applicationContext.getBean("orderService",OrderService.class);
	}
}
~~~

#### 스프링 컨테이너
- ApplicationContext를 스프링 컨테이너라 한다.
- 스프링 컨테이너는 @Configuration이 붙은 AppConfig를 설정(구성) 정보로 사용한다. @Bean이라 적힌 메서드를 모두 호출해서 반환된 객체를 스프링 컨테이너에 등록한다. 이렇게 스프링 컨테이너에 등록된 객체를 스프링 빈이라 한다.
- 스프링 빈은 @Bean이 붙은 메서드의 이름을 스프링 빈 이름으로 사용한다.
- 스프링 빈은 applicationContext.getBean()메서드를 사용해서 찾을 수 있다.
