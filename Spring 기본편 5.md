## 컴포넌트 스캔
지금까지는 수동으로 스프링 컨테이너에 @Bean, \<bean> 등을 통해서 직접 스프링 빈을 등록했었다. 하지만 이렇게 등록해야 할 코드가 수백만가지가 넘어간다면 너무나도 귀찮아진다. 자동으로 스프링 빈을 등록할 방법은 없을까?

스프링은 자동으로 스프링 빈을 등록해주는 기능을 제공한다.
바로 컴포넌트 스캔이라는 기능을 제공한다.

또 의존관계를 자동으로 주입해주는 @Autowired라는 기능도 제공한다.

**AutoAppConfig**
~~~ java
@Configuration

@ComponentScan(
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class)
)
public class AutoAppConfig {

}
~~~
- @ComponentScan을 설정 정보에 붙여주면 컴포넌트 스캔을 할 수 있다.
- 기존의 AppConfig와 다르게 @Bean으로 등록한 클래스는 하나도 없다!

> 참고: @Configuration에는 @Component를 포함하고 있다. 따라서 AppConfig를 컴포넌트 스캔으로 찾기 때문에 여기서는 배제(excludeFilters)를 했다. 

컴포넌트 스캔은 이름 그대로 @Component가 붙은 클래스들을 스캔해서 스프링 빈으로 등록한다.

각 클래스에 @Component 붙여주기
-  MemoryMemberRepository @Component 추가
- RateDisountPolicy @Component 추가
- MemberServiceImpl @Component, @Autowired 추가
- OrderServiceImpl @Component, @Autowired 추가

**AutoAppConfigTest**
~~~ java
public class AutoAppConfigTest {

    @Test
    void basicScan() {

        ApplicationContext ac = new AnnotationConfigApplicationContext(AutoAppConfig.class);

        MemberService memberService = ac.getBean(MemberService.class);

assertThat(memberService).isInstanceOf(MemberService.class);
    }
}
~~~
-  AnnotationConfigApplicationContext를 쓰는 것은 기존과 동일하다.
- 설정 정보로 AutoAppConfig를 넘겨준다.

### 컴포넌트 스캔과 의존관계 자동 주입 그림으로 알아보기

**1. @ComponentScan**
![[Pasted image 20250122131251.png]]
- @ComponentScan이 클래스마다 돌면서 @Component가 붙은 클래스를 스프링 빈으로 등록한다.
	- 빈 이름은 클래스 명으로 하며 맨 앞글자만 소문자를 사용한다.
	- 만약 스프링 빈의 이름을 직접 지정하고 싶으면 @Component(“memberService2”) 이런식으로 이름을 부여하면 된다.

**2. @Autowired 의존관계 자동 주입**
![[Pasted image 20250122132007.png]]
- 생성자에 @Autowired를 지정하면, 스프링 컨테이너가 자동으로 스프링 빈을 찾아서 자동으로 주입해준다.
- 기본 조회 전략은 타입이 같은 빈을 찾아서 주입한다.
	- getBean(MemberRepsitory.class)와 동일하다고 이해하면 됨.

![[Pasted image 20250122132613.png]]
- 생성자에 파라미터가 몇 개든 스프링 빈에서 찾아서 주입해준다.

컴포넌트 스캔 대상
- @Component: 컴포넌트 스캔에서 사용 
- @Controller: 스프링 MVC 컨트롤러에서 사용
- @Service: 스프링 비즈니스 로직에서 사용
- @Repository: 스프링 데이터 접근 계층에서 사용
- @Configuration: 스프링 설정 정보에서 사용
해당 애노테이션들은 모두 @Component를 포함하고 있기 때문에 스캔 대상이다.

#### FilterType 옵션(@ComponentScan 할 때)
FilterType 옵션 FilterType은 5가지 옵션이 있다.

ANNOTATION: 기본값, 애노테이션을 인식해서 동작한다. ex) `org.example.SomeAnnotation`

ASSIGNABLE_TYPE: 지정한 타입과 자식 타입을 인식해서 동작한다. ex) `org.example.SomeClass`

ASPECTJ: AspectJ 패턴 사용

ex) `org.example..*Service+`

REGEX: 정규 표현식

ex) `org\.example\.Default.*`

CUSTOM: `TypeFilter` 이라는 인터페이스를 구현해서 처리 ex) `org.example.MyTypeFilter`

### 중복 등록과 충돌
컴포넌트 스캔에서 같은 빈 이름을 등록하면 어떻게 될까?

#### 자동 빈 등록 VS 자동 빈 등록
- 컴포넌트 스캔에 의해 자동으로 스프링 빈으로 등록되는데, 이름이 같은 경우 스프링은 오류를 발생시킨다.
	- ConflictingBeanDefinitionException 예외 발생

#### 수동 빈 등록 VS 자동 빈 등록

이 경우에 수동 빈 등록이 우선권을 갖는다. 왜냐 개발자가 직접 등록한 것이기 때문에,,,
최근에는 스프링 부트에서 수동 빈 등록과 자동 빈 등록이 충돌나면 오류가 발생하도록 기본 값을 바꾸었다. 이런 사소한 버그가 복잡한 버그를 낳는다,,,,,,