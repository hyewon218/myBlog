# Kafka란?
- Apache Kafka는 고성능 데이터 파이프라인, 스트리밍 분석, 데이터 통합 및 미션 크리티컬 애플리케이션을 위해<br>
오픈 소스 **분산 이벤트 스트리밍 플랫폼(distributed event streaming platform)**이다.

## Kafka와 같은 이벤트 브로커를 사용하는 이유
### 서버 컴포넌트간의 직접의존을 방지하기 위해서 사용한다.
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/70c1b781-83a4-44a7-9d09-7ce9e65aa238" width="60%"/><br>

#### 위 아키텍처의 문제점
- 서버 컴포넌트가 서로 직접 의존되어 있는 경우 상대방 시스템의 변경에 따라 의존하는 시스템도 변경해주어야 한다.
- 의존되는 시스템에 장애가 발생할 경우 의존하는 시스템도 영향을 받아서 장애가 발생할 수 있다.<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/47f499c0-228a-4bcd-8fec-a9a7acbefb08" width="60%"/><br>
- 다음과 같이 Kafka를 메세지 브로커로서 두면 시스템간에 직접 메시지를 주고 받지 않기 때문에 위의 두 문제를 해결할 수 있다.
- 이벤트 발행자는 **kafka에게 어떻게 메시지를 발행할지**만 고민하면 되고 이벤트 수신자는 **Kafka에서 메시지를 수신하여 어떻게 처리할지**만 고민하면 된다.

### 동기 방식을 비동기 방식으로 전환함과 동시에 관심사를 분리하기 위해서 사용한다.
- 예를 들어 내가 쓴 글에 타인이 좋아요를 누를 경우 알림이 울리는 기능이 있다고 하자.
- 이 기능에는 크게 두가지의 관심사가 있는데
  - 첫번째 관심사는, **like를 저장**하는 관심사
  - 두번째 관심사는, **알림을 저장하고 발송**하는 관심사
- 첫번째 관심사는 좋아요를 누르고 나서 사용자가 정상 응답을 받는 사이에 **동기적**으로 이루어지면서 좋아요가 저장되어야 한다. 
- 두번째 관심사는 좋아요를 저장하는 것과 꼭 함께 이루어져야할 필요는 없고 **비동기적으로 이루어져도 상관이 없다.**
- 이 두 관심사를 모두 동기적으로 처리하고 하나의 시스템에서 처리한다면 **알림 기능에서 장애가 발생했을 때 좋아요가 눌리는 기능 자체가 실패할 수 있다.**
- 이 때 `kafka`를 활용하면 좋아요를 눌렀을 때 **Kafka에 이벤트를 발행만 하는 것**으로 대체하고 **비동기적으로 Consumer 그룹에서 이벤트를 읽고 처리할 수 있다.**
- 따라서, **알림 기능에 장애가 발생하더라도 좋아요 기능은 제대로 수행 될 수 있다.**

### 단일 진실 공급원으로서 활용한다.
- 서버 아키텍처에서 데이터는 cache, RDB, 여러 NOSQL 등에 분산되어 저장될 수 있는데 이때 각 DataSource에서 데이터의 내용은 다를 수 있다.
- Kafka에 event를 발행하고 그 event를 각각의 DataSource가 서로 다른 `Consumer Group`으로서 저장 및 처리하는 방식으로 구성하면<br> 
각각의 DataSource의 현재 데이터 형상이 다를 때 실제 데이터는 Kafka에 발행한 Event라고 정의할 수 있다.

<br>

## Kafka 핵심 개념들
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/8a6c0e8a-a4e5-4b97-aeb5-9c4cfcacdd9f" width="60%"/><br>
### Message Broker와 Event Broker인 Kafka의 차이점
- Message Broker에는 `RabbitMQ`, `ActiveMQ`, `RedisQueue`등이 있다.
- 가장 큰 차이는 Event Broker Kafka의 경우 publish 한 event를 **반영구적으로 저장**할 수 있다는 점이다.<br> 
Message Broker에서는 subscribe한 메시지를 저장하기 힘들다.
  - kafka의 경우 이벤트를 저장하기 때문에 **이벤트 발행 및 소비 여부를 보장**할 수 있으며, **장애가 발생 했을 때 재처리**할 수 있다.
  - 단일 진실 공급원으로서 활용할 수 있다.
- 따라서 이벤트 브로커는 메시지 브로커로도 활용될 수 있지만 메시지 브로커를 이벤트 브로커로 활용할 수는 없다.

### Topic
- topic은 이벤트를 **발행**하거나 혹은 발행한 이벤트를 **소비**하는 곳을 **지정**하는 개념이다.
- 따라서 이벤트 발행 및 소비 시 토픽을 지정해 주어야 한다.
- 토픽 내에는 **여러 개의 Partition**이 존재하여 **이벤트는 각각의 Partition에 분리되어 저장된다**.

### Partition
- topic 내에 실제로 **이벤트가 record 형태로 저장**되는 곳이다.
- **큐** 자료 구조로 되어있으며 이벤트 발행 시 **partition 내에 이벤트가 레코드로 저장**되고 **저장된 이벤트는 기본적으로는 삭제되지 않는다**.(설정에 따라 주기적으로 삭제)
- **Partition 하나 당 consumer가 하나 붙어서 소비**할 수 있고 Partition 개수 만큼 Consumer를 생성하면 **병렬 처리가 가능**하기 때문에 **성능을 늘리는 데 활용**할 수 있다.
- Partition은 늘릴 수는 있지만 줄일 수 없기 때문에 **Partition 개수를 늘리는 것에 신중해야 한다**.
- Partition 개수보다 Consumer가 많은 경우 Consumer가 많아서 생기는 이점이 없기 때문에 **Consumer를 Partition개수보다 항상 같거나 작게 유지**해야한다.
- 컨슈머가 이벤트를 소비해도 record가 사라지지 않으며 컨슈머 그룹의 **offset**이 변경되기만 한다.

### Producer
- topic에 이벤트를 publish한다.
- publish 시에 **Ack 조건을 설정**하여 **publish를 얼마나 보장할지 결정**할 수 있다.
  - ack = 0 ; publish가 실패해도 ack를 보낸다.
  - ack = 1 ; publish가 **leader에만 성공**하면 ack를 보낸다. 이 경우 leader가 fail하여 replica가 leader가 될 경우 이벤트가 유실 될 수 있다.
  - ack = all; publish가 **leader에서 성공**하고 **replica에도 모두 복제 되어야** ack를 보낸다. ack = 1에서 leader가 fail하는 경우에도 이벤트가 유실 되지 않는다.

> (추가) acks:all 설정 시 `min.in-sync.replicas`도 2 이상으로 변경해 주어야 한다.<br>


- 이벤트 발행 시 어떤 파티션에 이벤트가 저장될 지는 랜덤이기 때문에 **이벤트 소비 순서는 보장되지 않는다**.
- 만약, 순서가 꼭 보장되어야 한다면 key값을 지정하여 publish할 수 있고 이 경우 하나의 partition에만 event가 저장된다.
  - 단, 이 경우 여러 파티션에서 consumer가 consume하여 성능적으로 병렬 처리를 할 수 있다는 이점은 사라지게 되고 <br>
  partition 개수를 증가 시킬 경우 순서가 보장되지 않을 수 있기 때문에(key 값으로 hashing을 하여 파티션을 대응하는 것이기 때문이다.) <br>
  **순서가 꼭 보장되어야 하는 경우가 아니면 key값을 지정하지 않는 것이 좋다**.


> ### acks = all 일 때 min.in-sync.replicas = 2로 설정해야 하는 이유는 무엇일까?
> `acks = all` : 리더는 ISR의 팔로워로부터 데이터에 대한 ack를 기다리고, 하나의 팔로워가 있는 한 데이터는 손실되지 않으며 **데이터 무손실에 대해 가장 강력하게 보장**<br>
> `ISR` : `In Sync Replica`의 약어로 현재 리플리케이션이 되고 있는 **리플리케이션 그룹(replication group)** 을 의미<br>
> `min.in-sync.replicas` : 최소 리플리케이션 팩터를 지정하는 옵션<br>
> `Replication Factor` 는 **토픽의 파티션의 복제본을 몇 개를 생성할지**에 대한 설정

### Consumer Group과 Consumer
- Consumer는 이벤트를 소비할 때 **Polling**을 활용하여 주기적으로 **소비할 이벤트가 존재하는지 확인**하는 방식을 취한다.<br>
즉, broker가 이벤트를 소비할 시점이나 소비할 주체를 결정하지 않고 **Consumer가 직접 결정**한다.(**Redis pub-sub의 경우 publish시 subscriber들에게 redis가 메시지를 전해준다**.)
- Consumer Group은 Topic에 발행된 이벤트를 consume하는 것에 대해서 **공통된 Consumer들의 그룹**이다.
- **메시지를 어디까지 소비했는지(offset)** 와 **메시지를 어떻게 처리할지**의 로직을 각각 정의할 수 있다.
- Consumer Group내에는 Consumer가 여러 개 존재할 수 있으며 Topic의 한개 혹은 그 이상의 partition에서 consume할 수 있다.<br> 
따라서 **partition 개수와 같거나 혹은 적게 consumer를 설정해주어야 한다.** (consumer 개수 <= partition 개수)

<br>

### Broker, Replication, In Sync Replica(ISR) for High Availability
#### Broker
- kafka 서버를 말한다.
- 가용성을 위해서 broker를 3이상 유지하는 것이 권장된다.

#### Replication
- 가용성을 위해 topic의 partition을 서로 다른 여러 broker에 복제하여 저장하는 것을 말한다.
- DB Replication등과 다르게 Replica partition에서 Read역할 만해준다거나 하는 부하 분산의 역할은 하지 못하고 <br>
단순히 leader partition의 fail-over시 복구 하기 위한 용도로서 replication을 수행한다.
- 특정 브로커가 leader, follwer로 구분되어있는 것이 아니라 partition마다 모두 다르다.
- 일반적인 database에서의 replication과는 다르게 leader에는 아예 요청이 가지 않는다. 단순히 레코드 저장을 보장해주는 역할만 수행한다.

#### In Sync Replica
- leader 파티션과 다른 broker들에 저장된 replica를 묶어 ISR이라고 한다.
- Replication 개수를 지정하여 ISR에 묶일 partition의 개수를 지정할 수 있다(**적어도 2개이상** 으로 하는 것이 바람직하다).

### ZooKeeper, Controller
- `ZooKeeper`는 **카프카의 메타데이터를 저장**하고 **상태관리** 등의 목적으로 이용한다.
- `Controller`는 Broker중 단 하나만 존재하는데, 각 브로커의 상태를 체크하고 특정 브로커가 fail하면 partition의 새로운 leader를 선축하고 각 broker에게 전달한다.

### Consumer Lag
- 파티션 내에서 이벤트 발행시 이벤트가 저장될 위치 offset과 이벤트가 소비될 offset의 차이를 말한다.
- 이 차이가 클 경우 소비되는 속도가 생산되는 속도 대비 느리다고 볼 수 있다.

<br>

## kafka 클러스터 local에 구축
1. kafka download를 구글에 검색하여 원하는 Kafka버전을 다운로드 한다.
2. 다운로드 받은 내용에 보면 bin, config 디렉토리가 있는데 각각 <br>
`bin`은 **서버 실행, 정지등의 동작을 수행**해주는 스크립트들이 들어있고<br> 
`config`는 **실행 시 옵션을 지정**해줄 수 있는 설정파일이 들어있다. <br>
이 설정파일을 적절히 수정하여 bin 안의 sh파일로 kafka를 실행할 수 있다.
3. Zookeeper server를 실행해준다.

> 주키퍼 실행 시 간혹 Quorum과 관련된 클래스를 찾을 수 없다는 에러가 발생할 수 있는데 이 경우 밑에 있는 gradlew 커맨드를 실행해주면 된다.<br>
> (오류: 기본 클래스 org.apache.zookeeper.server.quorum.QuorumPeerMain을(를) 찾거나 로드할 수 없습니다.)

```yaml
bin/zookeeper-server-start.sh config/zookeeper.properties
```

4. Kafka 서버들을 실행해준다.<br>
   설정 파일 수정<br>
   다음 두 옵션을 모두 다르게 설정 해준다.<br>
   - broker.id=0
   - listeners=PLAINTEXT://{domain}:{port}

```yaml
bin/kafka-server-start.sh config/server.properties
bin/kafka-server-start.sh config/server.properties1
bin/kafka-server-start.sh config/server.properties2
```
> ### kafka scale out<br>
> 서버를 추가하는 방법은 매우 간단하다.
> server.properties에서 설정을 변경하면 되는데 다른 설정 값은 기존 kafka 설정과 동일하게 진행하고 일반적으로 변경이 필요한 부분은 아래 2가지 설정이다.
> - broker.id
> - listeners
> broker.id는 kafka 클러스터 내부에서 유일한 값을 가져야 하기 때문에 기존에 사용하던 broker.id와 다른 값을 지정하고,
> listeners는 kafka broker가 토오신을 위해 열어둘 인터페이스를 지정하는 부분으로 일반적으로 localhost의 hostname으로 지정하기에 두가지 옵션을 변경했다.
>
> 위의 두가지 설정을 변경한 후 kafka broker을 실행시키면 kafka 클러스터에 추가가 된다.
> 제대로 붙었는지 확인하기 위해 zookeeper-shell.sh 파일을 사용할 수 있는데,
> 아래의 명령어로 확인할 수 있다.
> ```yaml
> ${kafka_home}/bin/zookeeper-shell.sh zookeeper_host ls /brokers/ids
> ```
> 위의 명령어를 실행하면 brokers들이 출력되며, 새로 추가한 broker.id가 나온다면 정상적으로 추가가 된 것으로 판단할 수 있다.
> kafka scale out은 이렇게 간단하게 가능하다.
> 그러나 이렇게 추가된 broker들에는 기존의 topic의 데이터는 저장되지 않고, 새로 만들어지는 topic에 대한 데이터만 저장이 되며, <br>
> 기존의 topic에서도 새로운 broker를 사용하기 위해서는 파티션 재할당 작업을 진행해야 한다.

<br>

## Kafka & Zookeeper 실행
/Users/choihyewon/Desktop/Work/kafka_docker_compose
#### docker-compose.yml 설정
```yaml
services:
  zookeeper:
    image: bitnami/zookeeper:latest
    container_name: zookeeper
    ports:
      - 2181:2181
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes

  kafka:
    image: bitnami/kafka:latest
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - 9092:9092
      - 9094:9094
    environment:
      - ALLOW_PLAINTEXT_LISTENER=yes
      - KAFKA_ENABLE_KRAFT=no
      - KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true
      - KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,EXTERNAL://localhost:9094
      - KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,EXTERNAL:PLAINTEXT,PLAINTEXT:PLAINTEXT

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    depends_on:
      - kafka
    ports:
      - 8080:8080
    environment:
      - DYNAMIC_CONFIG_ENABLED=true
      - KAFKA_CLUSTERS_0_NAME=peters_kafka
      - KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=kafka:9092
```
Docker Compose를 이용하여 `Kafka`와 `Zookeeper`를 도커 컨테이너로 실행시킨다. Zookeeper는 기본적으로 2181 포트를 사용한다.<br>

- 컴포즈 실행: docker-compose up -d
- 컴포즈 종료: docker-compose down

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/d1183437-3bfe-48bc-a37f-266c024561c0" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1cb80a67-35a7-4277-9311-9fe0b399335b" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/38c60d05-2a6c-4345-a763-445b5d9b9688" width="60%"/><br>



---
# 알림 기능 구조 변경
## 수정 내용
   - 알림을 생성하는 것과 sse알림을 보내는 것을 같은 이벤트에 대해서 다르게 처리하는 Consumer group 두 개로 분리 및 Kafka Consumer Config변경
   - 트랜잭션 외부에서 이벤트 발행

### 기능 개선 후 동작 flow
1. Client에서 **Sse연결을 요청**하면 Server에서는 **연결 객체를 생성**하여 이를 **서버 인메모리 내에 저장**하고 **Client에게 연결 정보를 제공**해줍니다.<br>
2. 댓글 작성, 글 참여 등의 **이벤트(특정 사용자가 API호출 시)가 발생**하면 해당 요청내용을 먼저 처리한 후 정상 처리 된다면 **알림 이벤트를 produce**하여 **kafka에 발행**합니다.
3. kafka에 저장된 알림 이벤트는 두 Consumer Group에 의해서 consume 되며<br>
**첫 번째 Consumer Group** 은 **알림 이벤트 데이터로 알림 엔티티를 만들어서 RDB에 저장**하고 **두 번째 Consumer Group**은 **SSE 응답을 보내기 위해 Redis pub/sub에 pub 메시지를 보냅니다**.
4. Redis channel에 pub 메시지가 보내지면 해당 채널을 구독하고 있는 subscriber 들에게 메시지가 push 됩니다. **subscriber** 들은 `Web Application Server`들 입니다.
5. `Web Application Server`가 redis pub 메시지를 수신하면 다음 로직을 수행합니다. <br>
   - 먼저 인메모리 내의 **ConcurrentHashMap** 에서 pub 메시지 정보에 해당하는 내용으로 **SseEmitter 객체**를 찾을 수 있는지 확인합니다.<br>
     - 이는 해당 WAS가 **SSE 응답을 보내야 하는 클라이언트와 연결된 WAS인지 확인**하는 과정이기도 합니다. <br>
   - **SseEmitter를 하나라도 찾은 WAS**는 **SSE 응답을 클라이언트에게 보내고** 이 응답을 통해 클라이언트는 스스로 요청(polling)하지 않고 **알림 내역을 비동기적으로 응답 받을 수 있습니다**.

