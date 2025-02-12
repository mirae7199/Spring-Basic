## 스프링 컨테이너 생성
~~~ java
ApplicationContext ac = new AnnotatationConfigApplicatioContext(AppConfig.class);
~~~
- ApplicationContext는 스프링 컨테이너이다.
- 스프링은 xml 방식으로 만들 수 있고, 애노테이션 기반의 자바 설정으로 만들 수 있다.
- Spring 기본편 2에서 사용했던 방식(AppConfig)이 애노테이션 기반의 자바 클래스로 스프링 컨테이너를 만들었다.
- 자바 기반 방식으로 스프링 컨테이너를 만들어 보자.
	- AnnotatationConfigApplicationContext(AppConfig.class);
	

### 스프링 컨테이너의 생성 과정

**1. 스프링 컨테이너 생성**
![[Pasted image 20250120105454.png]]
- new AnnotationConfigApplicationContext(AppConfig.class);
- 스프링 컨테이너를 생성할 때는 구성 정보를 넣어줘야 하는데 여기서는 AppConfig 구성 정보를 넣어주었다.
- AppConfig에서 @Bean으로 설정했던 메서드들이 이곳 스프링 컨테이너에 저장되는 것이다.

**2. 스프링 빈 등록**
![[Pasted image 20250120110528.png]]
- AppConfig에서 @Bean으로 설정했던 메서드들이 스프링 컨테이너로 저장.
	- 빈 이름은 AppConfig에서 @Bean으로 설정했던 메서드의 이름으로 저장된다. (기본값) (빈 이름을 직접 부여할 수 도 있음  @Bean(name=“memberService2) )
	- 빈 객체는 AppConfig에서 @Bean으로 설정했던 메서드의 반환 타입이다.

**스프링 빈 의존관계 설정**
![[Pasted image 20250120111306.png]]
- 스프링 컨테이너는 설정 정보를 참고해서 의존관계를 주입(DI)한다.
- 뒤에서 다시 설명(싱글톤 컨테이너)

### 컨테이너에 등록된 모든 빈 조회

~~~ java
public class ApplicationContextInfoTest {

    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

    @Test
    @DisplayName("모든 빈 출력하기")
    void findAllBean() {

        // 스프링에 등록된 빈 이름 모두 배열에 반환
        String[] beanDefinitionNames = ac.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
			// 이름에 해당하는 빈 가져오기
            Object bean = ac.getBean(beanDefinitionName); 
            System.out.println("name = " + beanDefinitionName + "object = " + bean);
        }
    }

    @Test
    @DisplayName("애플리케이션 빈 출력하기")
    void findApplicationBean() {

        String[] beanDefinitionNames = ac.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = ac.getBeanDefinition(beanDefinitionName);

            // Role ROLE_APPLICATION: 직접 등록한 애플리케이션 빈
            // Role ROLE_INFRASTRUCTURE: 스프링이 내부에서 사용하는 빈
            if(beanDefinition.getRole() == BeanDefinition.ROLE_APPLICATION){
                Object bean = ac.getBean(beanDefinitionName);
                System.out.println("name=" + beanDefinitionName + " object= " + bean);
            }
        }

    }

}
~~~
- 모든 빈 출력하기
	-  ac.getBeanDefinitionNames() -> 스프링 컨테이너에 등록된 모든 빈의 이름 가져오기
	-  ac.getBean() -> 해당하는 빈 객체 가져오기(빈 이름, 클래스 타입으로)
- 애플리케이션 빈 출력하기
	- 애플리케이션에서 내가 등록한 빈만 가져오기
		- ROLE_APPLICATION: 직접 등록한 애플리케이션 빈
		- ROLE_INFRASTRUCTURE: 스프링이 내부에서 사용하는 빈

### 스프링 빈 조회 - 기본
-  getBean(빈 이름, 반환 객체 타입)
- getBean(반환 객체 타입) 
	- 스프링 컨테이너에 해당하는 빈의 이름이나 객체 타입이 없으면 NoSuchBeanDefinitionExcetion 발생

 ~~~ java
 public class ApplicationContextBasicFindTest {

    ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

  

    @Test
    @DisplayName("빈 이름으로 조회")
    void findBeanByName() {

        MemberService memberService = ac.getBean("memberService", MemberService.class);

        assertThat(memberService).isInstanceOf(MemberService.class);
    }

  

    @Test
    @DisplayName("이름 없이 타입만으로 조회")
    void findBeanByType() {

        MemberRepository memberRepository = ac.getBean(MemoryMemberRepository.class);

        assertThat(memberRepository).isInstanceOf(MemberRepository.class);

    }

    @Test
    @DisplayName("구체 타입으로 조회")
    void findBeanByName2() {

        MemberRepository memberRepository = ac.getBean("memberRepository", MemoryMemberRepository.class);

        assertThat(memberRepository).isInstanceOf(MemberRepository.class);

    }

  

    @Test
    @DisplayName("빈 이름으로 조회X")
    void findBeanByNameX() {

        assertThrows(NoSuchBeanDefinitionException.class,
                ()->ac.getBean("XXXXX", MemberRepository.class));
    }
}
~~~
- 빈 이름, 타입으로 조회하기
	- ac.getBean(“memberService”);
- 빈 타입만으로 조회하기
	- ac.getBean(MemoryMemberRepository.class);
- 구체 타입으로 조회하기
	- ac.getBean(“memberRepository”, MemoryMemberRepository.class);
- 빈 이름으로 조회X
	- 해당하는 빈의 이름이 없다면 NoSuchBeanDefinitionException 발생
		- 예외 발생 예측
		- Assertions.assertThrows(NoSuchBeanDefinitionException.class, ()-> ac.getBean(“xxxxx”, MemberService.class));


### 스프링 빈 조회 - 동일한 타입이 둘 이상

타입으로 조회시 같은 타입의 스프링 빈이 둘 이상이면 오류가 발생한다. 이때는 빈 이름을 지정하자.
ac.getBeansOfType()을 사용하면 해당 타입의 모든 빈을 조회할 수 있다.


~~~ java
public class ApplicationContextSameBeanFindTest {

    ApplicationContext ac = new AnnotationConfigApplicationContext(SameBeanConfig.class);

    @Test
    @DisplayName("타입으로 조회시 같은 타입이 둘 이상 있으면, 중복 오류가 발생한다")
    void findBeanByTypeDuplicate() {
        assertThrows(NoUniqueBeanDefinitionException.class, ()-> ac.getBean(MemberRepository.class));
    }

    @Test
    @DisplayName("타입으로 조회시 같은 타입이 둘 이상 있으면, 빈 이름을 지정하면 된다")
    void findBeanByName() {
        MemberRepository memberRepository = ac.getBean("memberRepository2", MemberRepository.class);

        assertThat(memberRepository).isInstanceOf(MemberRepository.class);
    }

    @Test
    @DisplayName("특정 타입을 모두 조회하기")
    void findAllBeanByType() {

        Map<String, MemberRepository> beansOfType = ac.getBeansOfType(MemberRepository.class);

        for (String key : beansOfType.keySet()) {
            System.out.println("key = " + key + " value = " + beansOfType.get(key));
        }
        System.out.println("beansOfType = " + beansOfType);
        assertThat(beansOfType.size()).isEqualTo(2);
    }

  

    static class SameBeanConfig {

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
        
        // test 용
        @Bean
        public MemberRepository memberRepository2() {
            return new MemoryMemberRepository();
        }
    }
}
~~~
- 타입으로 조회시 같은 타입이 둘 이상 있으면 중복 오류 발생
	-   예외 예측 대비
		- Assertions.assertThrows(NoUniqueBeanDefinitionException.class, ()-> ac.getBean(MemberRepository.class);
- 타입으로 조회시 같은 타입이 둘 이상 있으면, 빈 이름을 지정하면 된다.
	- MemberRepository memberRepository = ac.getBean(“memberRepository2”, MemberRepository.class);
- 특정 타입을 모두 조회하기
	- Map\<String, MemberRepository> beansOfType = ac.getBeansOfType(MemberRepository.class);
		- MemberRepository 클래스 타입에 해당하는 모든 빈들을 Map에 저장 (Map\<빈이름, 반환 객체 타입>)

### 스프링 빈 조회 - 상속 관계

![[Pasted image 20250120131831.png]]

부모 타입으로 조회하면, 자식 타입도 함께 조회된다. 
따라서 Object타입으로 조회하면 모든 스프링 빈을 조회한다.

~~~ java
public class ApplicationContextExtendsFindTest {

    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(TestConfig.class);

    @Test
    @DisplayName("부모 타입으로 조회시, 자식이 둘 이상 있으면, 중복 오류가 발생한다")
    void findBeanParentTypeDuplicate() {
        assertThrows(NoUniqueBeanDefinitionException.class,
                ()->ac.getBean(DisCountPolicy.class));
    }

    @Test
    @DisplayName("부모 타입으로 조회시, 자식이 둘 이상 있으면, 빈 이름을 지정하면 된다")
    void findBeanByParentTypeBeanName() {

        DisCountPolicy discountPolicy = ac.getBean("rateDiscountPolicy", DisCountPolicy.class);

        assertThat(discountPolicy).isInstanceOf(DisCountPolicy.class);
    }

    @Test
    @DisplayName("특정 하위 타입으로 조회")
    void findBeanBySubType() {
        RateDiscountPolicy discountPolicy = ac.getBean(RateDiscountPolicy.class);

        assertThat(discountPolicy).isInstanceOf(RateDiscountPolicy.class);
    }

    @Test
    @DisplayName("부모 타입으로 모두 조회하기")
    void findAllBeanByParentType() {
        Map<String, DisCountPolicy> beansOfType = ac.getBeansOfType(DisCountPolicy.class);
        
        for (String key : beansOfType.keySet()) {
            System.out.println("name = " + key + "bean = " + beansOfType.get(key));
            assertThat(beansOfType.size()).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("부모 타입으로 모두 조회하기 - Object")
    void findAllBeanByObjectType() {
        Map<String, Object> beansOfType = ac.getBeansOfType(Object.class);
        for (String key : beansOfType.keySet()) {
            System.out.println("name = " + key + "bean = " + beansOfType.get(key));
            
            assertThat(beansOfType).isInstanceOf(Object.class);
        }
    }
    
    static class TestConfig {
        @Bean
        public DisCountPolicy rateDiscountPolicy() {
            return new RateDiscountPolicy();
        }

        @Bean
        public DisCountPolicy fixDiscountpolicy() {
            return new FixDiscountPolicy();
        }
    }
}
~~~
- 부모 타입으로 조회시, 자식이 둘 이상 있으면 중복 오류 발생
	- 예외 예측 대비
		- Assertions.assertThrows(NoUniqueBeanDefinitionException.class, ()-> ac.getBean(DiscountPolicy.class);
	-  부모 타입으로 조회시, 자식이 둘 이상 있으면  빈 이름 지정
		- DiscountPolicy discountPolicy = ac.getBean(“rateDiscountPolicy”, DiscountPolicy.class);
	- 특정 하위 타입으로 조회하기
		- RateDiscountPolicy discountPolicy = ac.getBean(RateDiscountPolicy.class);
	-  부모 타입으로 모두 조회하기
		- Map\<String, DiscountPolicy> beansOfType = ac.getBeansOfType(DiscountPolicy.class);
	- 부모 타입으로 모두 조회하기 - Object
		- Map\<String, Object> beansOfType = ac.getBeansOfType(Object.class);

### BeanFactory와 ApplicationContext
![[Pasted image 20250120133815.png]]

**BeanFactory**
- 스프링 컨테이너의 최상위 컨테이너이다.
- 스프링 빈을 조회하고 관리하는 역할
- getBean() 제공

**ApplicationContext가 제공하는 부가기능**
![[Pasted image 20250120134049.png]]
- MessageSource
	- 메시지소스를 활용한 국제화 기능
	- 한국 -> 한국어, 영어권 -> 영어
- EnvironmentCapable
	-  환경변수
	- 로컬, 개발, 운영등을 구분해서 처리
- ApplicationEventPublisher
	- 애플리케이션 이벤트
	- 이벤트를 발행하고 구독하는 모델을 편리하게 지원
- ResourceLoader
	- 편리한 리소스 조회
	- 파일, 클래스패스, 외부 등에서 리소스를 편리하게 조회

### 다양한 설정 형식 지원 - 자바 코드, XML 
![[Pasted image 20250120134519.png]]

### 스프링 빈 설정 메타 정보 - BeanDefinition
- 역할과 구현을 개념적으로 나눈 것
	- XML을 읽어서 BeanDefinition 만듬
	-  자바 코드를 읽어서 BeanDefinition 만듬
- BeanDefinition을 빈 설정 메타 정보라고 함.
	- @Bean, \<bean> 당 각각 하나씩 메타 정보가 생성된다.
- 스프링 컨테이너는 이 메타정보를 기반으로 스프링 빈을 생성한다.

![[Pasted image 20250120134929.png]]

