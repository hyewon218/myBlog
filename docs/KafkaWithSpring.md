# Kafka with spring

## 토픽
### 토픽 생성시 고려할 점
- 토픽명은 한정 정하면 바꾸기 어렵기 때문에 동료들과 컨벤션을 정하여 패턴을 정하는 것이 중요합니다.
- 토픽의 파티션 개수 계산
  - 파티션은 늘릴 수 있지만 줄이는 것은 X
  - 파티션은 필요한 만큼만 생성해야 합니다. 그렇지 않으면 서버에 불필요한 비용이 발생합니다. 
- Retention 시간 (메모리 저장 기간)
  - kafka의 데이터 저장 기간을 설정하여 기간이 지나면 삭제하도록 합니다. <br>
  이 기간을 잘 설정하지 않으면 많은 데이터가 저장되고 이는 장애를 유발 할 수 있습니다.

<br>

## 생성자(KafkaProducerConfig)
spring boot에서 kafka로 생성할 때 사용할 수 있는 Template은 3가지 입니다.
### kafkaTemplate


<br>

## 소비자(KafkaConsumerConfig)
### Message listener

### Message listener Container

### @KafkaListener

<br>

## 구성하기(KafkaTopicConfig)
### kafka Admin
Spring boot에서는 **KafkaAdmin**이 Bean으로 autoConfiguration 되어 모든 설정이 default 값으로 **자동으로 생성**된다.

### Admin Client in kafka
spring boot에서 kafka 내부의 brokers, topics, partition 등을 관리해주는 역할을 하는 것이 admin Clinet이고<br> 
이는 KafkaAdmin에 설정된 셋팅 값을 통해서 생성된다.<br>
KafkaAdmin 객체가 자동으로 생성되는 것과 달리 **admin client는 직접 생성해줘야** 한다.

#### topic 관리하기
Spring boot에서는 KafkaAdmin Bean이 자동으로 생성된다.

```java
@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.bootstrapAddress}")
    private String bootstrapServers;

    @Value("${kafka.my.push.topic.name}")
    private String topicName;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic newTopic() {
        return new NewTopic(topicName, 3, (short) 3);
    }
}
```
KafkaAdmin 객체를 적절하게 초기화시켜주고 **NewTopic 객체를 빈으로 등록**해서 애플리케이션 **로딩 시점에 카프카에 토픽을 등록**할 수 있다.<br> 
만약 같은 이름의 토픽이 등록되어있다면 아무런 동작도 하지 않는다. 위에서 사용한 NewTopic 객체의 생성자는 다음과 같다.

```java
  public NewTopic(String name, int numPartitions, short replicationFactor) {
        this(name, Optional.of(numPartitions), Optional.of(replicationFactor));
    }
```
broker 를 두개만 설정하였으므로 최소 Replication Factor 로 2를 설정하고<br>
Partition 의 경우 Event 의 Consumer 인 WAS 를 2대까지만 실행되도록 해두었기 때문에 2로 설정함.<br>
이보다 Partition 을 크게 설정한다고 해서 Consume 속도가 빨라지지 않기 때문이다.


<br>

#### kafkaTemplate(producer)
kafka에 데이터를 생성하는 template 양식이다.
+) Spring에서 kafka를 사용할 때 생성한 데이터가 브로커에 잘 전달 됐는지 확인 하는 설정은 acks 이다.
acks는 브로커로 부터 데이터가 왔다고 응답받는 수 이다.
```
3개의 브로커에서 모두 응답을 받는다면 acks = 3
1개의 leader 브로커에서만 응답을 받는다면 acks = 1
데이터의 저장 여부는 상관없이 없다면 akcs = 0
```
acks는 브로커로부터 응답을 받기 때문에 acks가 높을 수록 신뢰성이 높아지지만 속도는 내려간다. spring의 acks defulat 값은 -1 (all) 이다.


데이터 보내기
```java
@PostMapping(
    value = ["/api/v1/kafka/chat"]
)
fun testChatDto(@RequestBody dto:ChatDto){
    kafkaCustomTemplate.send("viva2", dto) // (토픽, 생성할 값)
}
```

보낸 값
```
{
    "type" : "ENTER",
    "sender" : "viva",
    "message" : "test case 1"
}
```

kafka에서 실제로 받은 값
```
{"type":"ENTER","sender":"viva","message":"test case 1","createdAt":"2022.03.02 16:07"}
```


#### kafka consumer (consumer)
kafka에서 데이터를 받아와 사용하는 cunsumer를 spring에서는 listener라고 지칭한다.
kafka listener를 생성하는 방법에는 2가지가 있다.
- @KafkaListener
- MessageListener

