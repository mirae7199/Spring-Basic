## 빈 생명주기 콜백
데이터베이스 커넥션 풀, 네트워크 소켓처럼 애플리케이션 시작 시점에 필요한 연결을 미리 해두고, 종료 시점에 연결을 모두 종료하는 작업을 진행하려면 객체의 초기화와 종료 작업이 필요하다.

외부 네트워크에 미리 연결하는 객체를 하나 생성한다고 가정하자.

**예제 코드**
~~~ java
public class NetworkClient {

    private String url;

	// 생성 시점에 네트워크 연결
    public NetworkClient() {
        System.out.println("생성자 호출, url = " + url);
        connect();
        call("초기화 연결 메시지");

    }

    // 서비스 시작시 호출
    public void connect() {
        System.out.println("connect: " + url);

    }

    public void call(String message) {
        System.out.println("call: " + url + "message = " + message);

    }

    // 서비스 종료시 호출
    public void discounnect() {
        System.out.println("close: " + url);

    }

    public void setUrl(String url) {
        this.url = url;

    }
}
~~~

**테스트 코드**
~~~ java
public class BeanLifeCycleTest {

    @Test
    public void lifeCycleTest() {
        ConfigurableApplicationContext ac = new AnnotationConfigApplicationContext(LifeCycleConfig.class);

        NetworkClient client = ac.getBean("networkClient", NetworkClient.class);

        ac.close(); // 스프링 컨테이너를 종료, ConfigurableApplicationContext 필요

    }

	@Confiuration
    static class LifeCycleConfig {

        @Bean
        public NetworkClient networkClient() {
            NetworkClient networkClient = new NetworkClient();
            networkClient.setUrl("http://hello-spring.dev");

            return networkClient;

        }
    }
}
~~~

**실행 결과**
~~~
생성자 호출, url = null
connect: null
call: null message = 초기화 연결 메시지
~~~
생성자 호출 부분을 보면 url = null이 나오는 것을 확인할 수 있다. 
당연한 이야기지만 생성자 부분(객체를 생성하는 단계)에는 url이 없고, 객체를 생성한 다음에 수정자 주입을 통해서 setUrl()이 호출되어야 url이 존재하게 된다.

스프링 빈의 라이프사이클
**객체 생성 -> 의존관계 주입**

스프링 빈은 객체를 생성하고 의존관계 주입이 끝나야 다음에 필요한 데이터를 사용할 수 있는 준비가 완료된다. 즉 초기화를 하고 의존관계 주입이 모두 완료되어서야 호출이 가능하다. 그런데 개발자가 의존관계 주입이 완료된 시점을 어떻게 알 수 있는가?

스프링은 의존관계 주입이 완료되면 스프링 빈에게 콜백 메서드를 통해서 초기화 시점을 알려주는 기능들을 제공한다. 또 스프링 컨테이너가 종료되기 직전에 소멸 콜백을 준다.

스프링 빈의 이벤트 라이프사이클
**스프링 컨테이너 생성 -> 스프링 빈 생성 -> 의존관계 주입 -> 초기화 콜백 -> 사용 -> 소멸전 콜백 -> 스프링 종료**

- 초기화 콜백: 스프링 빈이 생성되고 의존관계 주입이 완료된 후 호출
- 소멸 전 콜백:  빈이 소멸되기 직전에 호출

**스프링 빈 생명주기 콜백 지원 방법 3가지**
- 인터페이스(InitalizingBean, DisposableBean)
- 설정 정보에 초기화 메서드, 종료 메서드 지정
- @PostConstruct, @PreDestroy 애노테이션 지원

#### 인터페이스 InitializingBean, DisposableBean
-  InitializingBean은 afterPropertiesSet() 메서드로 초기화를 지원한다.
-  DisposableBean은 destroy() 메서드로 소멸을 지원한다.

이 인터페이스는 스프링 전용 인터페이스이다. 따라서 해당 코드가 스프링 전용 인터페이스에 의존할 수 밖에 없다. 초기화, 소멸 메서드의 이름도 변경할 수 없다.

이 방법은 스프링 초창기에 나온 방법이다. 지금은 거의 사용하지 않는다…

#### 빈 등록 초기화, 소멸 메서드 지정
설정 정보에 @Bean(initMethod = “init”, destroyMethod = “close”) 처럼 초기화, 소멸 메서드를 지정할 수 있다.

**NetworkClient class에 init, close 메서드 추가하기**
~~~ java
public class NetworkClient {
	public void init() {
		System.out.println(“NetworkClient.init”);
		connect();
		call(“초기화 연결 메시지“);
	}

	public void close() {
		System.out.println(“NetworkClient.close”);
		disConnect();	
	}
}
~~~


~~~ java
 @Bean(initMethod = “init”, destroyMethod = “close”)
 public NetworkClient networkClient() {
	 NetworkClient networkClient = new NetworkClient();
     networkClient.setUrl("http://hello-spring.dev");

     return networkClient;
 }
~~~

 **테스트 코드 실행 결과**
 ~~~
 생성자 호출, url = null
 NetworkClient.init
 connect: http://hello-spring.dev
call: http://hello-spring.dev message = 초기화 연결 메시지
 13:33:10.029 [main] DEBUGorg.springframework.context.annotation.AnnotationConfigApplicationContext -
 Closing NetworkClient.close
 close + http://hello-spring.dev
 ~~~

**설정 정보 사용 특징**
- 메소드 이름을 자유롭게 줄 수 있음
- 스프링 빈이 스프링 코드에 의존적이지 않음
- 코드가 아니라 설정 정보를 사용하기 때문에 코드를 고칠 수 없는 외부 라이브러리에도 적용 가능

**종료 메서드 추론**
- @Bean의 destroyMethod 속성에서는 close, shutdown 이라는 종료 메서드를 추론하는 기능이 있어서 자동으로 메서드를 호출해준다.
	- 따라서 스프링 빈에 등록만 한다면 종료 메서드를 따로 적어주지 않아도 잘 동작함.
- 추론 기능은 기본값이기 때문에 destroyMethod=“ ” 공백으로 적용하면 추론 기능을 없앨수 있다.

#### 애노테이션 @PostConstruct, @PreDestory
~~~ java
public class NetworkClient {
	@PostConstruct
	public void init() {
		System.out.println(“NetworkClient.init”);
		connect();
		call(“초기화 연결 메시지“);
	}
	@PreDestory
	public void close() {
		System.out.println(“NetworkClient.close”);
		disConnect();	
	}
}
~~~

**실행 결과**
~~~
 생성자 호출, url = null
 NetworkClient.init
 connect: http://hello-spring.dev
call: http://hello-spring.dev message = 초기화 연결 메시지
 13:33:10.029 [main] DEBUGorg.springframework.context.annotation.AnnotationConfigApplicationContext -
 Closing NetworkClient.close
 close + http://hello-spring.dev
 ~~~
 
@PostConstruct, @PreDestroy 이 두 애노테이션을 사용하면 가장 편리하게 초기화와 종료를 실행할 수 있다.

**@PostConstruct, @PreDestroy 애노테이션 특징
- 최신 스프링에서 가장 권장하는 방법이다.
- 애노테이션만 붙이면 되므로 편리하다.
- 스프링에 종속적인 기술이 아니라 자바 표준이다. 따라서 스프링 외 다른 컨테이너에서도 동작한다.
- 유일한 단점은 외부 라이브러리에선 적용하지 못하는 것이다.

**정리**
- @PostConstruct, @PreDestory 애노테이션을 사용하자!
- 코드를 고칠 수 없는 외부 라이브러리를 초기화, 종료하려면 @Bean의 initMethod, destoryMethod를 사용하자.

