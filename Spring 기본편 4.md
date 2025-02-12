### 웹 애플리케이션과 싱글톤
웹 애플리케이션은 보통 여러 고객이 동시에 요청을 한다.

![Image](https://github.com/user-attachments/assets/a604bafc-e3cc-4fe2-aecc-04ce39c67cf0)

**스프링 없는 순수한 DI 컨테이너 테스트**
~~~ java
public class SingletonTest {

    @Test
    @DisplayName("스프링 없는 순수한 DI 컨테이너")
    void pureContainer() {
        AppConfig appConfig = new AppConfig();
        // 1. 조회: 호출할 때마다 객체를 생성
        MemberService memberService1 = appConfig.memberService();

        // 2. 조회: 호출할 때마다 객체를 생성
        MemberService memberService2 = appConfig.memberService();
        
        // 참조값이 다른 것을 확인
        System.out.println("memberService1 = " + memberService1);
        System.out.println("memberService2 = " + memberService2);
  
        // memberService1 != memberService2
              Assertions.assertThat(memberService1).isNotSameAs(memberService2);

    }

}
~~~
- 전에 만들었던 스프링 없는 순수한 DI 컨테이너인 AppConfig는 요청을 할 때 마다 새로운 객체를 생성한다.
- 고객 트래픽이 초당 100이 나오면 초당 100개의 객체가 생성되고 소멸된다! -> 메모리 낭비가 심함.
- 해결방안
	- 해당 객체가 1개만 생성되고, 공유하도록 설계한다 -> 싱글톤 패턴

### 싱글톤 패턴
- 클래스의 인스턴스가 딱 1개만 생성되는 것을 보장하는 디자인 패턴이다.
- 객체 인스턴스가 2개 이상 생성되지 않게 막기
	-  private 생성자를 사용해서 외부에서 사용 못하게 막음

**싱글톤 패턴을 적용한 예제 코드**
~~~ java
public class SingletonService {

    // 1. static 영역에 객체를 딱 1개만 생성해둔다.
    private static final SingletonService instance = new SingletonService();

    // 2. public으로 열어서 객체 인스턴스가 필요하면 이 static 메서드를 통해서만 조회하도록 허용한다.
    public static SingletonService getInstance() {
        return instance; // new SingletonService()
    }

    // 3. 생성자를 private를 선언해서 외부에서 new 키워드를 사용한 객체 생성을 못하게 막는다.
    private SingletonService() {
    }

    public void logic() {
        System.out.println("싱글톤 객체 로직 호출");
    }

}
~~~

1. static 영역에 객체  instance를 미리 생성해둔다. (new SingletonService())
2. 객체 인스턴스가 필요할 때 호출하기 위해서 public 으로 메서드 생성. 이 메서드를 호출하면 항상 같은 인스턴스를 조회할 수 있다.
3.  private 생성자를 사용해서 외부에서 new 키워드를 사용하지 못하게 차단하였다. 새로운 객체를 생성할 수 없다는 것이다.

**싱글톤 패턴을 사용하는 테스트 코드**
~~~ java
public class SingletonServiceTest {

    @Test
    @DisplayName("싱글톤 패턴을 적용한 객체 사용")
    void singletonServiceTest() {
    
        // private으로 생성자를 막아두었다. 컴파일 오류가 발생
        // new SingletonService();
        // 1. 조회: 호출할 때 마다 같은 객체를 반환
        SingletonService singletonService1 = SingletonService.getInstance();

        // 2. 조회: 호출할  때 마다 같은 객체를 반환
        SingletonService singletonService2 = SingletonService.getInstance();
        
        // 참조값이 같은 것을 확인
        System.out.println("singletonService1 = " + singletonService1);
        System.out.println("singletonService2 = " + singletonService2);

        // singletonService1 == singletonService2
        assertThat(singletonService1).isSameAs(singletonService2);
        
        singletonService1.logic();
    }
}
~~~
- private으로 새 객체를 만드는 것을 막았다.
- 호출할 때 마다 같은 객체를 반환하는 것을 알 수 있다.

> 참고: 싱글톤 패턴을 구현하는 방법은 많지만 여기서는 안전하고 단순한 방법으로 구현하였다.

싱글톤 패턴을 적용하면 고객이 요청할 때 마다 객체를 생성해서 메모리를 낭비시키지 않고, 이미 만들어진 객체를 공유해서 효율적으로 사용할 수 있다.

하지만 싱글톤 패턴도 문제점은 있다.

**싱글톤 패턴 문제점**
- 싱글톤 패턴을 구현하는 코드가 많이 들어감
- 의존관계상 클라이언트가 구체 클래스에 의존한다. -> DIP를 위반
- 클라이언트가 구체 클래스에 의존해서 OCP 원칙을 위반할 가능성 있음
- 테스트하기 어려움
- 내부 속성을 변경하거나 초기화 하기 어려움
- private 생성자로 자식 클래스를 만들기 어려움
- 결론적으로 유연성이 떨어짐
- 안티패턴으로 불리기도 함

### 싱글톤 컨테이너
싱글톤 패턴은 위와 같이 많은 문제점이 있다. 이 문제점을 해결하는 것이 바로 싱글톤 컨테이너이다. 문제점을 해결하면서 객체를 하나만 생성하고, 공유한다. 지금까지 우리가 학습한 스프링 빈이 바로 싱글톤으로 관리되는 빈이었다!

**싱글톤 컨테이너**
- 싱글톤 컨테이너는 싱글톤 패턴으로 적용하지 않아도, 객체 인스턴스를 싱글톤으로 관리한다.
- 스프링 컨테이너 == 싱글톤 컨테이너 역할, 싱글톤 객체를 생성하고 관리하는 기능을 싱글톤 레지스트리라 한다.
- 스프링 컨테이너의 기능 덕분에 싱글톤 패턴의 모든 문제점을 해결하면서 객체를 싱글톤 패턴으로 유지할 수 있다.
	- 싱글톤 패턴을 위한 지저분한 코드 없음
	- DIP, OCP, private 생성자, 테스트로 부터 자유롭게 싱글톤을 사용할 수 있음.

**스프링 컨테이너를 사용하는 테스트 코드**
~~~ java
public class SpringContainerAndSingleton {

    @Test
    @DisplayName("스프링 컨테이너와 싱글톤")
    void springContainer() {

        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

        // 1. 조회: 호출할 때 마다 같은 객체를 반환
        MemberService memberService1 = ac.getBean("memberService", MemberService.class);

        // 2. 조회: 호출할 때 마다 같은 객체를 반환
        MemberService memberService2 = ac.getBean("memberService", MemberService.class);

        // 참조값이 같은 것을 확인
        System.out.println("memberService1 = " + memberService1);
        System.out.println("memberService2 = " + memberService2);
        Assertions.assertThat(memberService1).isSameAs(memberService2);

    }
}
~~~

**싱글톤 컨테이너 적용 후**
![Image](https://github.com/user-attachments/assets/ca0d5fbe-1a84-43b6-a395-b49faa7f10a8)
- 스프링 컨테이너를 쓰면 동일한 객체를 반환한다. 싱글톤 패턴을 적용하지 않아도 싱글톤을 사용할 수 있다!

> 참고: 스프링의 기본 빈 등록 방식은 싱글톤이지만, 싱글톤만 지원하는 것은 아니다.
> (빈 스코프에서 설명)

### 싱글톤 방식의 주의점
- 싱글톤 패턴이든, 싱글톤 컨테이너든 여러 클라이언트가 하나의 같은 객체 인스턴스를 공유하기 때문에 상태를 **유지(stateful)**하게 설계하면 안된다.
- **무상태(stateless)**로 설계해야 한다.
	- 특정 클라이언트가 의존적인 필드가 있으면 안됨.
	- 특정 클라이언트가 값을 변경할 수 있는 필드가 있으면 안됨.
	- 가급적 읽기만 가능해야 함.
	- 필드 대신에 자바에서 공유되지 않는, 지역변수, 파라미터, ThreadLocal 등을 사용해야 함.
- 스프링 빈의 필드에 공유 값을 설정하면 정말 큰 장애가 발생할 수 있다!!!

**상태를 유지할 경우 발생하는 문제점 예시**
~~~ java
public class StatefulService {

    private int price; // 상태를 유지하는 필드
    
    public void order(String name, int price) {
        System.out.println("name = " + name + " price = " + price);
        this.price = price; // 여기가 문제!!

    }

    public int getPrice() {
        return price;
    }

}
~~~

~~~ java
public class StatefulServiceTest {

    @Test
    @DisplayName("싱글톤 상태 유지")
    void statefulServiceSingleton() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(TestConfig.class);

        StatefulService statefulService1 = ac.getBean("statefulService", StatefulService.class);

        StatefulService statefulService2 = ac.getBean("statefulService", StatefulService.class);

        // ThreadA: A사용자 10000원 주문
        statefulService1.order("userA", 10000);

        // ThreadB: B사용자 20000원 주문
        statefulService2.order("userB", 20000);

        // ThreadA: 사용자A 주문 금액 조회
        int price = statefulService1.getPrice();

        // ThreadA: 사용자A는 10000원을 기대했지만, 기대와 다르게 20000원 출력
        System.out.println("price = " + price);
        Assertions.assertThat(statefulService1.getPrice()).isEqualTo(20000);

    }
    
    static class TestConfig {

        @Bean
        public StatefulService statefulService() {
            return new StatefulService();
        }
    }
}
~~~

실행 결과
~~~ 
name = userA price = 10000
name = userB price = 20000
price = 20000
~~~

- 단순하게 설명하기 위해서 실제 쓰레드는 사용 안함.
- statefulService1과 statefulService2는 같은 객체를 참조한다.(스프링 컨테이너)
- statefulService의 price 필드는 공유되는 필드인데, 특정 클라이언트가 값을 변경한다.
- 사용자A의 주문금액은 10000원이지만 어째서인지 20000원이라는 결과가 나왔다.
- 실무에서도 많이 발생하는 문제이다.
- 공유필드는 조심하자! 스프링 빈은 항상 무상태(stateless)로 설계하자!

### @Configuration과 싱글톤
**이상한점 AppConfig 코드**
~~~ java
@Configuration  
public class AppConfig {  
  
    @Bean  
    public MemberService memberService() {  
        return new MemberServiceImpl(memberRepository());  
    }  
    @Bean  
    public OrderService orderService() {  
        return new OrderServiceImpl(memberRepository(), discountPolicy());  
    }  
    @Bean  
    public MemberRepository memberRepository(){  
        return new MemoryMemberRepository();  
    }  
    @Bean  
    public DisCountPolicy discountPolicy() {  
        return new RateDiscountPolicy();  
    }  
  
}
~~~
- memberService 메서드에서 memberRepository() 호출 -> new MemoryMemberRepository() 호출
- orderService 메서드에서 memberRepository() 호출 -> new MemoryMemberRepository() 호출

여기서 이상한점은 memberRepository()를 호출할 때 마다 새로운 인스턴스 객체 MemoryMemberRepository를 생성한다는 점이다. 
이것은 싱글톤 패턴이 전혀 적용되지 않아 보이는데 어째서인지 싱글톤으로 작동되고 있다.

스프링 컨테이너는 어떻게 이 문제를 해결하는 것일까???

### @Configuration과 바이트코드 조작의 마법
스프링은 클래스의 바이트코드를 조작하는 라이브러리를 사용한다.
모든 비밀은 @Configuration을 적용한 AppConfig에 있다.

~~~ java
@Test  
void configurationDeep() {  
    ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);  
  
    // AppConfig도 스프링 빈으로 등록된다.  
    AppConfig bean = ac.getBean(AppConfig.class);  
  
    System.out.println("bean = " + bean.getClass());  
    // 출력: class hello.re_core.AppConfig$$SpringCGLIB$$0  
~~~

 **결과**
 ~~~
 bean = class hello.re_core.AppConfig$$SpringCGLIB$$0
 ~~~
- AnnotaionConfigurationApplicationContext에 파라미터를 넘긴 값은 스프링 빈으로 등록된다. 따라서 AppConfig도 스프링 빈으로 등록된다.
- 클래스 명에 xxxCGLIB가 붙는데 이것은 스프링이 CGLIB라는 바이트코드 조작 라이브러리를 사용해서 AppConfig 클래스를 상속받은 임의의 다른 클래스를 만들고, 그 다른 클래스를 스프링 빈으로 등록한 것이다!

![Image](https://github.com/user-attachments/assets/9849f6bd-507d-4a93-8e36-fe8fa431df59)
- AppConfig@CGLIB이 AppConfig를 상속하고 있다.
- AppConfig@CGLIB이 싱글톤이 보장되도록 해준다.

**AppConfig@CGLIB 예상 코드**
~~~ java
@Bean
 public MemberRepository memberRepository() {

if (memoryMemberRepository가 이미 스프링 컨테이너에 등록되어 있으면?) { 
	return 스프링 컨테이너에서 찾아서 반환;

} else { // 스프링 컨테이너에 없으면  
	기존 로직을 호출해서 MemoryMemberRepository를 생성하고 스프링 컨테이너에 등록 
	return 반환

	}

}
~~~
- @Bean이 붙은 메서드마다 이미 스프링 빈이 존재하면 존재하는 빈을 반환하고(같은 객체), 스프링 빈이 없으면 생성(새로운 객체 생성)해서 스프링 빈으로 등록하고 반환하는 코드가 동적으로 만들어진다.
- 덕분에 싱글톤이 보장되는 것이다!!!!

크게 고민할 것 없이 스프링 설정 정보에 @Configuration을 쓰자! 그럼 싱글톤은 보장될 것이다!!
