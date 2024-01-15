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

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/83b85eb8-306d-45fd-87dc-6cba461efd74" width="60%"/><br>

<br>

### Partition
- topic 내에 실제로 **이벤트가 record 형태로 저장**되는 곳이다.
- **큐** 자료 구조로 되어있으며 이벤트 발행 시 **partition 내에 이벤트가 record로 저장**되고 **저장된 이벤트는 기본적으로는 삭제되지 않는다**.(설정에 따라 주기적으로 삭제)<br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4ae8997e-91c9-4882-af5e-6319fc9e7dd4" width="60%"/><br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1a3dae36-a4c7-4b7c-8afd-63a3a54827d9" width="30%"/><br>

> Queue란?<br>
> FIFO(First in First Out)형식의 자료구조를 의미한다.<br>
> 순차적으로 진행되는 Process에 사용<br>
> Message는 순차적으로 Queue에 들어오게 되고,<br>
> Queue는 순차적으로 들어오는 Message를 받아서 갖고 있다가 하나하나 순서에 맞게 전달한다.

- **record**에 포함된 메세지 키 또는 메세지 값에 따라서 partition의 위치가 결정된다.
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a833a9c2-d5d5-4138-b958-ff0ba3ddf2e2" width="30%"/> <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2e0c6600-8efb-429d-a81c-8c96ae2d17d9" width="30%"/><br>
- 동일한 메세지 키를 가진 record들은 동일한 partition에 들어가기 때문에 순서를 지켜서 데이터를 처리할 수 있다는 장점이 있다.
- **Partition 하나 당 consumer가 하나 붙어서 소비**할 수 있고 Partition 개수 만큼 Consumer를 생성하면 **병렬 처리가 가능**하기 때문에 **성능을 늘리는 데 활용**할 수 있다.
- Partition은 늘릴 수는 있지만 줄일 수 없기 때문에 **Partition 개수를 늘리는 것에 신중해야 한다**.
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b084704c-34eb-41ef-939d-de56c408e59c" width="60%"/><br> 
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/85b8dc9f-2c8d-4fa0-b9e3-a37febbee9cb" width="60%"/><br>
- Partition 개수보다 Consumer가 많은 경우 Consumer가 많아서 생기는 이점이 없기 때문에 **Consumer를 Partition개수보다 항상 같거나 작게 유지**해야 한다.
- 컨슈머가 이벤트를 소비해도 record가 사라지지 않으며 컨슈머 그룹의 **offset**이 변경되기만 한다.

<br>

### Producer
- topic에 이벤트를 publish한다.
- publish 시에 **Ack 조건을 설정**하여 **publish를 얼마나 보장할지 결정**할 수 있다.(고가용성 유지-partition의 replication과 관련)<br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1a3aef45-aee2-4e22-856d-1339388f1768" width="60%"/><br>
  - `ack = 0` ; leader partition에 데이터를 전송하고 응답값을 받지 않는다. publish가 실패해도 ack를 보낸다.<br>
    -> 속도는 빠르지만 데이터 유실 가능성이 있다.

  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/680b0a12-6346-4a57-850f-ee69a7f624d7" width="60%"/><br>  
  - `ack = 1` ; publish가 **leader에만 성공**하면 ack를 보낸다. 이 경우 leader가 fail하여 replica가 leader가 될 경우 이벤트가 유실 될 수 있다.

  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/249e2bf9-c2ec-42fe-a0ea-a25ee380f55f" width="60%"/><br>
  - `ack = all`; publish가 **leader에서 성공**하고 **replica(follower)에도 모두 복제 되어야** ack를 보낸다.<br> 
  ack = 1에서 leader가 fail하는 경우에도 이벤트가 유실 되지 않는다.<br>
  -> 속도가 현저히 느리다.

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

<br>

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
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/40681833-bc70-455f-96ac-beb81d5e1505" width="30%"/><br>
- `kafka 서버`를 말한다.<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/52be39fb-8e2a-46ee-af73-3aa223d8cf98" width="60%"/><br>
- 가용성을 위해서 broker를 **3이상 유지**하는 것이 권장된다.

<br>

#### Replication(Partition의 복제)
- **가용성**을 위해 **topic의 partition**을 **서로 다른 여러 broker에 복제하여 저장**하는 것을 말한다.
- DB Replication등과 다르게 Replica partition에서 Read역할 만해준다거나 하는 부하 분산의 역할은 하지 못하고 <br>
단순히 leader partition의 fail-over시 복구 하기 위한 용도로서 replication을 수행한다.
- 특정 브로커가 leader, follwer로 구분되어있는 것이 아니라 partition 마다 모두 다르다.
- 일반적인 database에서의 replication과는 다르게 leader에는 아예 요청이 가지 않는다. 단순히 레코드 저장을 보장해주는 역할만 수행한다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/f6a88e93-eaa5-4e52-ab20-5c7438511a27" width="60%"/><br>
- 만약 replication이 1이라면 partition은 한 개만 존재한다.<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/d9bb7075-14d1-4d4e-a42e-6bba1aedf13e" width="60%"/><br>
- replication이 2라면 partition은 원본 한 개와 복제본 한 개로 총 두 개가 존재한다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/80d3fac3-9623-4e3d-adff-fe4747699a8b" width="60%"/><br>
- replication이 3이라면 partition은 원본 한 개와 복제본 두 개로 총 세 개가 존재한다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0c8acc47-2633-4846-a5cd-1629378cdd1d" width="60%"/><br>
- broker 갯수에 따라서 replication 갯수가 달라진다.
- broker 갯수가 3이면 replication 갯수는 4가 될 수 없다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/50206019-35a9-44a9-92c9-8cad06603b1f" width="60%"/><br>
- 원본 partition은 `leader partition`, 나머지 복제본 partition은 `follwer partition`이라고 부른다.

> 3개 이상의 broker를 사용할 때 replication은 3으로 설정하는 것을 추천한다.
> <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a296a284-efdb-4c82-aed9-b8934b24d6db" width="60%"/><br>

<br>

#### In Sync Replica(`leader partition` + `follwer partition`)
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/69f876ab-ff54-4089-a97b-21bc86964567" width="60%"/><br>
- leader 파티션과 다른 broker들에 저장된 replica를 묶어 `ISR`이라고 한다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/420823de-2332-4c81-ab1f-e43c5b0a8553" width="60%"/><br>
- 만약 Replication이 2이면 broker 1개가 죽더라도 복제본, 즉 `follwer partition` 이 존재하므로 복구가 가능하다.
- Replication 개수를 지정하여 ISR에 묶일 partition의 개수를 지정할 수 있다(**적어도 2개이상** 으로 하는 것이 바람직하다).

> `leader partition` + `follwer partition`의 역할은?
> `leader partition` : producer가 topic의 parition에 데이터를 전달할 때 전달받는 주체

<br>

### ZooKeeper, Controller
- `ZooKeeper`는 **카프카의 메타데이터를 저장**하고 **상태관리** 등의 목적으로 이용한다.
- `Controller`는 Broker중 단 하나만 존재하는데, 각 브로커의 상태를 체크하고 특정 브로커가 fail하면 partition의 새로운 leader를 선축하고 각 broker에게 전달한다.

### Consumer Lag
- 파티션 내에서 이벤트 발행시 이벤트가 저장될 위치 offset과 이벤트가 소비될 offset의 차이를 말한다.
- 이 차이가 클 경우 소비되는 속도가 생산되는 속도 대비 느리다고 볼 수 있다.

<br>

## ⭐️AWS에 카프카 클러스터 설치, 실행하기
#### 카프카 고가용성의 핵심은 3개 이상의 카프카 broker로 이루어진 클러스터에서 진가를 발휘하게 된다.
1. **AWS로 EC2 3대 발급**<br>
테스트 목적의 머신이므로 Amazon Linux 2 AMI(Amazon Machine Image)를 t2.micro로 발급받습니다.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7264148b-d466-41d1-b76d-0571030bf92d" width="60%"/><br>

    1-1. **Mac OS 터미널을 이용한 EC2 접속**<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/fe927681-5a33-4450-868e-602e50b4785b" width="60%"/><br>
   AWS의 EC2 인스턴스 페이지에서 1개의 인스턴스 클릭 후 연결 클릭
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7128340e-e2ac-4ef6-b250-ec8ca65c5252" width="60%"/><br>
   인스턴스 액세스 방법 3번 아래의 명령어 복사 -> EC2 접속
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c2605b1e-70bf-4a82-8e65-cdcdeefcf93a" width="60%"/><br>
   yes 입력 후 해당 창이 뜨면 접속 성공

   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/bdb5e7d1-b67b-4fce-b7b4-bcf4129562d5" width="60%"/><br>
    root 계정으로 접속<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/80d7a013-e8f7-4903-ac37-ad172cfdefd1" width="60%"/><br>
    hostname 변경 (ex. test-broker01)<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/efd5b400-eb7f-4fda-b3c7-715f003e4eb6" width="60%"/><br>
 

<br>

2. **방화벽 설정 및 /etc/hosts 설정**<br>
    <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/5227519b-0757-4435-a581-c0575a1a2433" width="60%"/><br>
    zookeeper와 카프카 클러스터가 각각 통신을 하기 위해서는 아래와 같이 inbound규칙을 추가해야 한다.<br> 
    기본적으로 aws ec2를 발급받은 뒤 security group의 기본설정은 outbound에 대해 anywere로 open되어 있으므로 inbound만 추가해 준다.<br> 
    추가해야할 port는 아래와 같다. 각 port는 anywhere 기준으로 열도록 한다.

    ```groovy
    // 만약 test-broker01인 경우
    0.0.0.0 test-broker01
    14.252.123.4 test-broker02
    55.231.124.1 test-broker03
    ```
    > 기본적으로 SSH 22번 포트만 설정되어 있다.

   각자의 `/etc/hosts` 파일을 편집해 이름 지정<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1a4bb10d-3143-4718-9f33-251bbea1991f" width="60%"/><br>
   테스트의 편의를 위해 이번에 만든 3개의 인스턴스의 이름은 test-broker01, 02, 03으로 지정하여 진행한다.<br>
   자기자신의 host는 0.0.0.0으로 설정하고 나머지 host는 ip로 할당되도록 설정해 준다.

<br>

3. **각 서버 접속 후 wget 명령어로 Zookeeper 설치**<br>
이제 실질적인 애플리케이션 설치 및 실행을 하도록 한다. 여기부터는 3개의 인스턴스(노드)에 모두 동일하게 진행하면 된다.<br>

    zookeeper 설치를 위해 아래 명령어로 zookeeper 3.4.12 압축파일을 다운받아 준다,
    ```shell
    wget https://downloads.apache.org/zookeeper/zookeeper-3.7.2/apache-zookeeper-3.7.2-bin.tar.gz
    ````
    다운받은 zookeeper 압축파일은 아래 명령어로 풀어준다.
    ```shell
    tar xvf apache-zookeeper-3.7.2-bin.tar.gz
    ````

    <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/8e2e4a4f-cbc1-4055-8586-e57c6058e03f" width="60%"/><br>

    <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/6ffcee8d-455e-4c13-844a-cd71f5895258" width="60%"/><br>
   이제 zookeeper의 configuration을 설정해야 한다.<br>
   zookeeper폴더내부의 conf폴더에 zoo.cfg파일을 생성하여 아래와 같이 configuration을 넣어준다.<br>
    ```shell
   vi zoo.cfg
   ```
    zoo.cfg 파일 생성<br>
    (vi 명령어는 기본적으로 파일의 내용을 편집하는 것)<br>
    (파일이 존재하지 않을 경우 파일을 생성하여 편집창을 띄운다)

    ```groovy
    tickTime=2000
    dataDir=/var/lib/zookeeper
    clientPort=2181
    initLimit=20
    syncLimit=5
    server.1=test-broker01:2888:3888
    server.2=test-broker02:2888:3888
    server.3=test-broker03:2888:3888
    ```  
    <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/8affdac9-3e5d-49d1-be5a-91d0ceae41c9" width="60%"/><br>

   - tickTime
     - 기준 시간 (현재 2초)
   - dataDir
     - zookeeper의 상태, 로그 등을 저장하는 디렉토리 위치 지정
   - clientPort
     - client 의 연결을 감지하는 port
   - initLimit
     - follower 가 leader와 처음 연결을 시도할 때 가지는 tick 횟수. 제한 횟수 넘으면 timeout<br>
       (현재 40초로 설정) (tickTime * initLimit)
   - syncLimit
     - follower 가 leader와 연결 된 후에 앙상블 안에서 leader와의 연결을 유지하기 위한 tick 횟수
     - 제한 횟수 넘으면 time out
     - (현재 10초로 설정) (tickTime * syncLimit)
     - server.(zookeeper_server.pid의 내용)=(host name 이나 host ip):2888:3888
   - 앙상블을 이루기 위한 서버의 정보
     - 2888은 동기화를 위한 포트, 3888은 클러스터 구성시 leader를 산출하기 위한 포트
     - 여기서 서버의 id 를 dataDir 에 설정해 줘야 한다.
     - (서버id 설정 경로 : /var/lib/zookeeper 의 zookeeper_server.pid 파일)

    <br>

    이제 zookeeper 앙상블을 만들기 위해 각 zookeeper마다 myid라는 파일을 만들어줘야 한다.<br> 
    myid의 위치는 `/var/lib/zookeeper/myid` 이고, 해당 파일에는 숫자를 하나 넣으면 된다.<br> 
    test-broker01은 1, test-broker02는 2, test-broker03은 3 으로 지정한다.
    ```shell
    // 만약 test-broker01에서 실행한 경우 1이 나와야함
    cat /var/lib/zookeeper/myid
    ```
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1d3b1bde-c09b-45ff-a835-6e52534d9f9f" width="60%"/><br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/35514307-5add-454d-8e65-43142e1f9ef0" width="60%"/><br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b3aeaaf6-344c-457e-aae0-9da9103a89cd" width="60%"/><br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/375bcbbe-c6ca-448d-b5e5-e42664f7bee4" width="60%"/><br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1832e311-caf7-48fb-8f24-74971189e044" width="60%"/><br>    
    
    <br>
    
   OpenJDK 설치
    ```shell
   yum install java-1.8.0-openjdk-devel.x86_64
   ```
    이제 zookeeper을 실행한다.
    ```shell
    ./bin/zkServer.sh start
    ZooKeeper JMX enabled by default
    Using config: /home/ec2-user/zookeeper-3.7.2/bin/../conf/zoo.cfg
    Strating zookeeper ... STARTED
    ```


4. **Kafka 설치**
zookeeper가 설치완료되었으니 이제 kafka를 다운받고 실행하겠습니다. 이번에 다운받아서 테스트할 버젼은 2.1.0입니다.
```shell
wget https://archive.apache.org/dist/kafka/2.6.0/kafka_2.12-2.6.0.tgz
```
마찬가지로 아래 명령어를 통해 압축을 풀어준다,
```shell
tar xvf kafka_2.12-2.6.0.tgz
````
kafka실행을 위해서 broker.id 설정, zookeeper에 대한 설정과 listener설정을 아래와 같이 설정한다.<br> 
대상 파일은 kafka 폴더 내부에 config/server.properties 이다.
```shell
// test-broker01인 경우 아래와 같이 설정합니다.
# The id of the broker. This must be set to a unique integer for each broker.
broker.id=0
listeners=PLAINTEXT://:9092
advertised.listeners=PLAINTEXT://test-broker01:9092
zookeeper.connect=test-broker01:2181,test-broker02:2181,test-broker03/test
```

참고로 zookeeper 설정시 마지막에 /test 와 같이 route를 넣는 것을 추천한다.<br>
이렇게 넣을 경우 zookeeper의 root node가 아닌 child node에 카프카정보를 저장하게 되므로 유지보수에 이득이 있다.

이제 kafka를 실행해본다.
```shell
./bin/kafka-server-start.sh ../config/server.properties
````
이것으로 kafka 클러스터를 구축 및 실행 완료하였다!


5. **console-producer,consumer 테스트**
실제로 kafka 클러스터가 동작하는지는 외부망에서 정상적으로 접근이 되는지 테스트를 해야한다.<br> 
이번에는 local machine(맥북)에서 kafka-console-producer와 kafka-console-consumer로 정상 동작하는지 테스트해보도록 한다.
```shell
./bin/kafka-topics.sh --create --zookeeper test-broker01:2181,test-broker02:2181,test-broker03:2181/test \
  --replication-factor 3 --partitions 1 --topic test
```
이제 console-producer와 console-consumer를 동시에 켜고 topic에 데이터가 정상적으로 처리되는지 확인한다.
```shell
./bin/kafka-console-producer.sh --broker-list test-broker01:9092,test-broker02:9092,test-broker03:9092 \
  --topic test
> This is a message
> This is another message

./bin/kafka-console-consumer.sh --bootstrap-server test-broker01:9092,test-broker02:9092,test-broker03:9092 \
  --topic test --from-beginning
This is a message
This is another message
````

정상적으로 처리되는것을 확인할 수 있다!

<br>

---
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
> broker.id 만 겹치지 않게 추가하면 끝<br>
> 추가 후 **카프카 실행** systemctl start kafka-server.service<br>
> 추가한 서버들이 카프카 클러스터에 조인되었는지 확인 -> 주키퍼 서버에서 broker.id 조회하여 새로 추가한 id 가 잘 조회되는지 확인<br>
> 잘 조회 됐다면, 신규 카프카 서버에 파티션 추가

> 서버를 추가하는 방법은 매우 간단하다.
> `server.properties`에서 설정을 변경하면 되는데 다른 설정 값은 기존 kafka 설정과 동일하게 진행하고 일반적으로 변경이 필요한 부분은 아래 2가지 설정이다.
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

