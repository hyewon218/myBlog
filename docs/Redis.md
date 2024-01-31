# Redis란?
- `Remote Dictionary Server`의 약자
- 오픈 소스 소프트웨어
- `휘발성`이면서 `영속성`을 가진 key-value 저장소

# Redis는 NoSQL
NoSQL은 데이터 간의 관계를 정의하지 않고 고정된 스키마를 갖지 않는 새로운 형태의 데이터베이스로서,<br>
**관계형 데이터베이스(RDBMS)를 경량화한 데이터베이스**입니다. <br>
관계형 데이터베이스의 특징을 제거하고 만들어진 다른 모든 형태의 DBMS를 칭하기도 하며, <br>
SQL 계열 질의어를 사용할 수 있다는 사실을 강조한다는 면에서 **“Not Only SQL”** 로 불리기도 합니다.<br>
Redis는 이러한 NoSQL의 종류 중 하나입니다.


## 첫번째 방법 : RedisRepository
Spring Data Redis 의 Redis Repository 를 이용하면 간단하게 Domain Entity 를 Redis Hash 로 만들 수 있습니다.<br>
다만 트랜잭션을 지원하지 않기 때문에 만약 트랜잭션을 적용하고 싶다면 RedisTemplate 을 사용해야 합니다.

### Entity
```java
@Getter
@RedisHash(value = "people", timeToLive = 30)
public class Person {

    @Id
    private String id;
    private String name;
    private Integer age;
    private LocalDateTime createdAt;

    public Person(String name, Integer age) {
        this.name = name;
        this.age = age;
        this.createdAt = LocalDateTime.now();
    }
}
```
- Redis 에 저장할 자료구조인 객체를 정의합니다.
- 일반적인 객체 선언 후 @RedisHash 를 붙이면 됩니다.
  - value : Redis 의 keyspace 값으로 사용됩니다.
  - timeToLive : 만료시간을 seconds 단위로 설정할 수 있습니다. 기본값은 만료시간이 없는 -1L 입니다.
- @Id 어노테이션이 붙은 필드가 Redis Key 값이 되며 null 로 세팅하면 랜덤값이 설정됩니다.
  - keyspace 와 합쳐져서 레디스에 저장된 최종 키 값은 keyspace:id 가 됩니다.

### Repository
```java
public interface PersonRedisRepository extends CrudRepository<Person, String> {
}
```
- CrudRepository 를 상속받는 Repository 클래스 추가합니다.

### Example
```java
@SpringBootTest
public class RedisRepositoryTest {

    @Autowired
    private PersonRedisRepository repo;

    @Test
    void test() {
        Person person = new Person("Park", 20);

        // 저장
        repo.save(person);

        // `keyspace:id` 값을 가져옴
        repo.findById(person.getId());

        // Person Entity 의 @RedisHash 에 정의되어 있는 keyspace (people) 에 속한 키의 갯수를 구함
        repo.count();

        // 삭제
        repo.delete(person);
    }
}
```
- JPA 와 동일하게 사용할 수 있습니다.
- 여기서는 id 값을 따로 설정하지 않아서 랜덤한 키값이 들어갑니다.
- 저장할때 save() 를 사용하고 값을 조회할 때 findById() 를 사용합니다.


### redis-cli 로 데이터 확인
```

```
- id 를 따로 설정하지 않은 null 값이라 랜덤한 키값이 들어갔습니다.
- 데이터를 저장하면 member 와 member:{randomKey} 라는 두개의 키값이 저장되었습니다.
- member 키값은 Set 자료구조이며, Member 엔티티에 해당하는 모든 Key 를 갖고 있습니다.
- member:{randomKey} 값은 Hash 자료구조이며 테스트 코드에서 작성한 값대로 field, value 가 세팅한 걸 확인할 수 있습니다.
- timeToLive 를 설정했기 때문에 30초 뒤에 사라집니다. ttl 명령어로 확인할 수 있습니다.


## 두번째 방법 : RedisTemplate
RedisTemplate 을 사용하면 특정 Entity 뿐만 아니라 여러가지 원하는 타입을 넣을 수 있습니다.<br>
template 을 선언한 후 원하는 타입에 맞는 Operations 을 꺼내서 사용합니다.

### config 설정 추가
```java
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate() {
        RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }
}
```
- RedisTemplate 에 LettuceConnectionFactory 을 적용해주기 위해 설정해 줍니다.


### Example
```java
@SpringBootTest
public class RedisTemplateTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void testStrings() {
        // given
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String key = "stringKey";

        // when
        valueOperations.set(key, "hello");

        // then
        String value = valueOperations.get(key);
        assertThat(value).isEqualTo("hello");
    }


    @Test
    void testSet() {
        // given
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        String key = "setKey";

        // when
        setOperations.add(key, "h", "e", "l", "l", "o");

        // then
        Set<String> members = setOperations.members(key);
        Long size = setOperations.size(key);

        assertThat(members).containsOnly("h", "e", "l", "o");
        assertThat(size).isEqualTo(4);
    }

    @Test
    void testHash() {
        // given
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        String key = "hashKey";

        // when
        hashOperations.put(key, "hello", "world");

        // then
        Object value = hashOperations.get(key, "hello");
        assertThat(value).isEqualTo("world");

        Map<Object, Object> entries = hashOperations.entries(key);
        assertThat(entries.keySet()).containsExactly("hello");
        assertThat(entries.values()).containsExactly("world");

        Long size = hashOperations.size(key);
        assertThat(size).isEqualTo(entries.size());
    }
}
```
- 위에서부터 차례대로 Strings, Set, Hash 자료구조에 대한 Operations 입니다.
- redisTemplate 을 주입받은 후에 원하는 Key, Value 타입에 맞게 Operations 을 선언해서 사용할 수 있습니다.
- 가장 흔하게 사용되는 `RedisTemplate<String, String>` 을 지원하는 `StringRedisTemplate` 타입도 따로 있습니다.

<br>

# 직렬화, 역직렬화

## GenericJackson2JsonRedisSerializer
제일 많이 사용하는 방법으로 redis value값을 class type 상관없이 값을 불러 올 수 있다.<br>
하지만 저장 시 클래스 패키지 명 등 클래스 타입 전체를 저장하여 해당 패키지 dto가 아닐경우 이 key값을 조회할 수 없는 치명적인 단점이있다.

## serialVersionUID란 무엇인가?
serialVersionUID는 시리얼 통신을 하는 클래스의 버전을 표시하는 것이다.
자바에서는 객체를 직렬화(Serialization)하여 바이트스트림으로 만들어서 저장한다.
이때 저장되는 바이트스트림에는 버전정보인 serialVersionUID가 포함되게 된다.
이 후 바이트스트림을 역직렬화(Deserialization)하여 자바의 객체로 만들때 버전을 체크하게되는데 이 때 사용되는 버전정보가 바로 serialVersionUID이다.

## 자바 직렬화란?
자바 객체를 바이트스트림으로 만들어서 파일, 데이터베이스, 메모리, 네트워크 송신이 가능하도록 하는 것이다.

## 자바 역직렬화란?
파일, 데이터베이스,메모리, 네트워크로 부터 수신된 바이트스트림을 자바객체로 변환하는 과정이다.

## 역직렬화 오류를 회피하기 위한 방법
간단하다. 반드시 명시적으로 SerialVersionUID를 써주는 것이다.

## 단순한 1L 대신 긴 serialVersionUID를 생성하는 이유는 무엇입니까?


<br>

# 🤔Redis로 해당 데이터를 저장하면 왜 성능개선이 있나요?
Redis는 인메모리 데이터베이스로 I/O가 많이 발생하는 데이터를 처리할때 많은 이점을 얻어갈 수 있다.<br> 
예를 들어, 조회수와 같은 경우도 게시글이 조회될때마다, 빠른 시간안에 DB에 계속 query가 들어가며 계속해서 업데이트 해줘야 한다.<br>
이런 경우, 서버에는 당연히 무리가 갈 것 이고 이를 인메모리에 저장하여 성능에 이점을 챙겨가는 것이다 💻

결론은,,<br>
Redis는 인메모리를 활용하며 데이터를 저장하는 경우에도 활용하지만 무거운 API를 캐싱하여 성능을 개선하는 이점도 존재한다.<br> 
Redis의 캐싱 활용은 데이터를 저장하는 것 보다 더욱 중요하고 성능 개선을 위한 방법이므로 Redis에 캐싱 기능을 활용하여 API 속도를 개선해보자.

<br>

# Cache
## Cache란?
> "오랜 시간이 걸리는 작업의 결과를 빠르게 접근 할 수 있는 곳에 저장하여 시간과 비용을 회피하는 방법" <br>
> 데이터나 값을 미리 복사해 놓는 임시 장소 <br>
> 캐시는 값을 저장해두고 불러오기 때문에 반복적으로 동일한 결과를 반환하는 경우에 용이하다. <br>
> 만약 매번 다른 결과를 돌려줘야 한다면 캐시를 확인하는 작업으로 인한 부하가 생겨 성능이 떨어지게 된다.
 
**이 접근 방법은 입력(인자)이 같으면 계속 호출되더라도 출력(결과)도 같다는 것을 보장하는 메서드에서만 적용 가능합니다.**

Cache란 나중에 요청할 결과를 **미리 저장해둔 후 빠르게 서비스**해주는 것을 의미한다.<br>
즉, 미리 결과를 저장하고 나중에 요청이 오면 그 요청에 대해서 DB 또는 API를 참조하지 않고 Cache에 접근하여 요청을 처리하게 된다.<br>
이러한 cache가 동작할 수 있는 철학에는 파레토 법칙이 있다.

파레토 법칙이란 80퍼센트의 결과는 20퍼센트의 원인으로 인해 발생 한다는 말이다.
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4fbfb657-ed9b-468a-8153-890f77f3f8bd" width="60%"/><br>
즉, 이것은 Cache가 효율적일 수 있는 이유가 될 수 있다.<br> 
모든 결과를 캐싱할 필요는 없으며, 우리는 서비스를 할 때 많이 사용되는 20%를 캐싱한다면 전체적으로 영향을 주어 효율을 극대화 할 수 있다는 말이다.

## Cache 사용 구조
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/33d53f42-6e25-4604-86bc-1ad8b16c569e" width="60%"/><br>
1. Client로부터 요청을 받는다.
2. Cache와 작업한다.
3. 실제 DB와 작업한다.
4. 다시 Cache와 작업한다.

Cache는 일반적으로 위와 같은 flow로 사용된다.<br> 
동일한 flow에서 어떻게 사용하냐에 따라 `look aside cache(Lazy Loading)`와 `write back`으로 나뉜다.

### ⭐️ look aside cache(Lazy Loading)
1. Cache에 Data 존재유무 확인
2. Data가 있다면 Cache의 Data 사용
3. Data가 없다면 실제 DB Data 사용 
4. **DB에서 가져온 Data를 Cache에 저장**

look aside cache는 캐시를 한번 접근하여 데이터가 있는지 판단한 후, <br>
있다면 cache의 데이터를 사용하며 없다면 실제 DB 또는 API를 호출하는 로직으로 구현된다.<br> 
**대부분의 cache를 사용한 개발이 해당 프로세스를 따르고 있다.**

### Write back
1. Data를 Cache에 저장
2. Cache에 있는 Data를 일정 기간 동안 Check
3. **모여있는 Data를 DB에 저장**
4. **Cache에 있는 Data 삭제**

write back은 cache를 다르게 이용하는 방법이다. **DB는 접근 횟수가 적을수록 전체 시스템의 퍼포먼스는 좋아진다.**<br>
데이터를 쓰거나 많은 데이터를 읽게 되면 DB에서 **Disk**를 접근하게 된다. 이렇게 되면 Application의 속도 저하가 일어날 수 있다.<br> 
따라서 write back은 **데이터를 cache에 모으고** 일정한 주기 또는 일정한 크기가 되면 **한번에 처리**하는 것이다.

<br>

## Spring에서 캐시 사용법(@Cacheable, @CachePut, @CacheEvict)

#### @Cacheable
- 데이터를 캐시에 저장
- 메서드를 호출할 때 캐시의 이름 (value) 과 키 (key) 를 확인하여 이미 저장된 데이터가 있으면 해당 데이터를 리턴
- 만약 데이터가 없다면 메서드를 수행 후 결과값을 저장
#### @CachePut
- @Cacheable 과 비슷하게 데이터를 캐시에 저장
- 차이점은 @Cacheable 은 캐시에 데이터가 이미 존재하면 메서드를 수행하지 않지만 @CachePut 은 항상 메서드를 수행
- 그래서 주로 캐시 데이터를 갱신할 때 많이 사용
#### @CacheEvict
- 캐시에 있는 데이터를 삭제
#### @CacheConfig
- 메서드가 아닌 클래스에 붙여서 공통된 캐시 기능을 모을 수 있음
- 예를 들면 cacheNames, cacheManager 등등
#### @Caching
- Cacheable, CachePut, CacheEvict 를 여러 개 사용할 때 묶어주는 기능

<br>

### 1. SpringBoot에 종속성 추가하기
#### gradle
```groovy
    // redis cache
    implementation 'org.springframework.boot:spring-boot-starter-cache'
```            

### 2. `@EnableCaching` 추가
Spring에서 `@Cacheable`과 같은 어노테이션 기반의 캐시 기능을 사용하기 위해서는 먼저 별도의 선언이 필요하다.<br>
그렇기 때문에 `@EnableCaching` 어노테이션을 설정 클래스에 추가해 주어야 한다.<br>
이 어노테이션을 붙여주면 이제 해당 애플리케이션의 캐싱 기능은 활성화 되어 <br>
DB에서 데이터를 읽어오는 부분에서 다른 어노테이션을 사용해 캐싱 기능을 이용할 수 있다.

```java
@EnableCaching
@EnableJpaAuditing
@SpringBootApplication
public class MyBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBlogApplication.class, args);
    }
}
```

```java
@EnableCaching
@Configuration 
public class CacheConfig {
    ... 
}
```
### 3. 캐시 매니저 빈 추가
캐시 추상화에서는 캐시 기술을 지원하는 캐시 매니저를 Bean으로 등록해야 한다.
물론 CacheManager 인터페이스를 직접 구현해도 되지만, 스프링에서는 이미 아래와 같이 다앙한 구현체를 제공하고 있다.

- `ConcurrentMapCacheManager` : JRE에서 제공하는 ConcurrentHashMap을 캐시 저장소로 사용할 수 있는 구현체다.<br> 
- 캐시 정보를 Map 타입으로 메모리에 저장해두기 때문에 빠르고 별다른 설정이 필요 없다는 장점이 있지만, 실제 서비스에서 사용하기엔 기능이 빈약하다.
- `SimpleCacheManager` : 기본적으로 제공하는 캐시가 없다. 사용할 캐시를 직접 등록하여 사용하기 위한 캐시 매니저 구현체다.
- `EhCacheCacheManager` : Java에서 유명한 캐시 프레임워크 중 하나인 EhCache를 지원하는 캐시 매니저 구현체다.
- `CaffeineCacheManager` : Java 8로 Guava 캐시를 재작성한 Caffeine 캐시 저장소를 사용할 수 있는 구현체다. EhCache와 함께 인기 있는 매니저인데, 이보다 좋은 성능을 갖는다고 한다.
- `JCacheCacheManager` : JSR-107 표준을 따르는 JCache 캐시 저장소를 사용할 수 있는 구현체다.
- `RedisCacheManager` : Redis를 캐시 저장소로 사용할 수 있는 구현체다.
- `CompositeCacheManager` : 한 개 이상의 캐시 매니저를 사용할 수 있는 혼합 캐시 매니저다.

```java
@Configuration
@EnableCaching
public class CachingConfig { 
  // 하나의 저장소를 사용하는 경우
  @Bean
  public CacheManager cacheManager() {
    return new ConcurrentMapCacheManager("memberCacheStore");
  }

  // 여러 개의 저장소를 추가할 경우
  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
    simpleCacheManager.setCaches(
        Arrays.asList(new ConcurrentMapCache("cacheStore1"), new ConcurrentMapCache("cacheStore2")));
    return simpleCacheManager;
  }
}
```


### 4. 캐시를 저장/조회하기 위한 @Cacheable
**동일한 파리 미터로 메서드를 호출 이력과 반환 결과가 캐시에 저장되어 있으면, 캐시를 사용함**

Redis Cache 활성화를 위한 @Annotation을 사용하였는데.<br> 
해당 @Cacheable은 **DB에서 애플리케이션으로 데이터를 가져오고 Cache에 저장**하는데 사용되며<br> 
**요청한 데이터가 redis에 존재하지 않을 경우 DB에서 조회**하며 **redis에 존재할경우 해당 저장소에서 바로 읽어 응답**한다.<br>
Value(아무렇게 해도 상관없음!)와 key를 설정하여 호출된 응답을 저장해주며 cacheManger 항목을 이용하여 해당 어노테이션에 적용될 config를 각각 설정할 수 있다.

클래스나 인터페이스에도 캐시를 지정할 수는 있지만 이렇게 작업하는 경우는 상당히 드물고, 보통 **메소드 단위**로 적용한다.<br>
캐시를 적용할 메소드에 @Cacheable 어노테이션을 붙여주면 **캐시에 데이터가 없을 경우에는 기존의 로직을 실행한 후에 캐시에 데이터를 추가하고,**<br> 
**캐시에 데이터가 있으면 캐시의 데이터를 반환한다.**

**@Cacheable 을 사용하여 캐시를 저장 및 조회**<br>
@Cacheable 은 SQL 중 SELECT, INSERT 를 제공하는 역할을 한다.<br> 
캐시 데이터가 없는 경우 입력을 하고 캐시 데이터가 있는 경우 조회하게 된다. 

```java
@Cacheable("bestSeller")
public Book getBestSeller(String bookNo) {

}
```
```java
@Cacheable(value = "bestSeller", key = "#bookNo")
public Book getBestSeller(String bookNo, User user, Date dateTime) {

}
```
cacheName(value) 값을 기준으로 캐시를 저장하는데, <br>
만약 더 디테일한 값으로 저장해야 한다면 key와 함께 저장한다.

만약 메소드의 **파라미터가 없**다면 0이라는 디폴트 값을 **Key**로 사용하여 저장한다.<br> 
그리고 만약 메소드의 **파라미터가 여러 개**라면 파라미터들의 hashCode 값을 조합하여 키를 생성한다.<br>
하지만 여러 개의 파라미터 중에서도 **1개의 키 값으로 지정**하고 싶은 경우도 있다. <br>
그러한 경우에는 다음과 같이 **Key 값을 별도로 지정**해주면 된다.

- 메서드의 Parameter가 없는 경우, SimpleKey.EMPTY 값으로 Key가 지정됨
- 메서드의 Parameter가 하나 이상인 경우, 파라미터 값을 기반으로 캐시의 Key가 생성됨

- 스프링은 AOP 기반으로 캐시가 작동하며 어노테이션으로 AOP 설정을 할 수 있어 간편하게 사용할 수 잇다.
- 캐시는 저장할 내용과 속성 정보로 메소드의 리턴값과 파라미터를 사용하기 때문에 보통 메소드 단위로 설정하게 되며 클래스나 인터페이스레벨에 캐시를 하는일은 드물다.

**Spring Cache는 트랜잭션(@Transcation)과 마찬가지로 AOP를 이용하여 코드에 주는 영향을 최소화하면서 다양한 캐시 기능을 일관성 있게 사용할 수 있도록 해준다.**

📌 캐시 사용방법
스프링 캐시는 기본적으로 메서드 단위의 AOP로 구현되어 있다.

메서드에 아래 어노테이션들을 사용하여 메서드 반환값을 캐싱할 수 있다. 캐싱된 경우 메서드는 아예 실행되지 않는다는 점을 유의하자.
🧨 @Transcational과 동일하게, @Cacheable이 걸려있는 클래스 내부 메서드를 호출(Self-invocation)하면 AOP가 동작하지 않는다.

SPEL 문법 ( 공식문서 : https://docs.spring.io/spring-framework/reference/core/expressions/language-ref.html )
조건에 따라 캐시를 다르게 동작하게 하거나, 캐시 key를 지정하고 싶다면 SPEL 문법을 사용하여 설정할 수 있다.
스프링 캐시는 기본적으로 메서드의 파라메타를 캐시 key 로 사용한다. ( #값, #객체명.변수명, #객체명.메서드()) 로 사용하면 된다.


