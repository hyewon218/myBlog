# Builder 패턴이란?

빌더 패턴(Builder pattern)이란 복합 객체의 생성 과정과 표현 방법을 분리하여<br>
동일한 생성 절차에서 서로 다른 표현 결과를 만들 수 있게 하는 패턴이다.

생성자 인자로 너무 많은 인자가 넘겨지는 경우 **어떠한 인자가 어떠한 값을 나타내는지** 확인하기 힘들다.<br>
또 어떠한 인스턴스의 경우에는 **특정 인자만으로 생성해야 하는 경우**가 발생한다.<br>
특정 인자에 해당하는 값을 **null**로 전달해줘야 하는데, 이는 코드의 가독성 측면에서 매우 좋지 않다는 것을 알 수 있다.<br>
이러한 문제를 **해결**하기 위해서 빌더패턴을 사용할 수 있다.<br>

## Lombok을 이용하여 생성
@Builder 어노테이션을 사용하면 위와 같이 길게 따로 Builder Class를 만들지 않아도 된다.

### @Builder
위에서 설명한 듯이 Builder 패턴을 자동으로 생성해주는데, <br>
builderMethodName에 들어간 이름으로 빌더 메소드를 생성해 준다.

### @AllArgsConstructor
@Builder 어노테이션을 선언하면 **전체 인자를 갖는 생성자**를 자동으로 만든다.

### @NoArgsConstructor 
파라미터가 없는 **기본 생성자**를 만들어 준다.

### @RequiredArgsConstructor
final이나 @NonNull인 필드 값만 파라미터로 받는 생성자를 만든다.


```java
User user1 = new User(); // @NoArgsConstructor
User user2 = new User("user2", "1234"); // @RequiredArgsConstructor
User user3 = new User(1L, "user3", "1234", null); // @AllArgsConstructor
```