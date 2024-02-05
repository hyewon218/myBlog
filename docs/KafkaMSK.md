# Amazon MSK(Managed Streaming for Apache Kafka) - [아파치 카프카 애플리케이션 프로그래밍 with 자바]
## MSK란?
**MSK** 는 AWS에서 제공하는 Sass형 아파치 카프카 서비스이다. <br>
AWS MSK는 AWS 인프라에서 **카프카 클러스터를 생성, 업데이트, 삭제** 등과 같은 운영 요소를 대시보드를 통해 제공한다.<br>
또한, 안전하게 접속할 수 있도록 클러스터와 연동시 TLS인증 보안을 설정할 수 있다.<br>

## MSK 활용
- 카프카 클러스터 생성
- 토픽 생성
- 모니터링을 위한 프로메테우스, 그라파나 연동
- 콘솔 프로듀서, 컨슈머 연동

### 카프카 클러스터 생성
#### 📍 VPC
VPC는 **Virtual Private Cloud**의 약자로서 사용자가 정의한 **가상의 네트워크**이다.<br>
리전을 선택하고 특정 IP 대역을 VPC로 생성할 수 있다. VPC로 생성한 네트워크 대역에 프라이빗 IP를 가진 EC2 인스턴스를 생성할 수 있다.<br> 
네트워크 대역은 **CIDR(Classless Inter-Domain Routing)** 표기법으로 선언하여 할당한다.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7269d470-06c1-4038-993d-f3ecd093c1c9" width="30%"/><br>

#### 📍 AZ(available zone)
AZ는 **AWS 리전에 존재하는 개별 데이터 센터**를 뜻한다. 각 AZ는 다른 AZ와 물리적으로 구분되어있기 때문에 **AZ의 장애는 다른 AZ에 전파되지 않는다.**<br>
동일 리전의 AZ 간에는 네트워크 지연이 매우 낮기 때문에 AZ 장애에 대응하여 **다수의 AZ에 서비스를 다중 운영**하면 안전 하게 서비스를 운영할 수 있다.<br>
AWS의 **서울 리전(ap-northeast-2)** 에서는 총 **4개의 AZ(ap-northeast-2a, ap-northeast-2b, ap-northeast-2c, ap-northeast-2d)** 를 제공한다.

#### 📍 서브넷(subnet)
서브넷은 VPC 내부에서 생성할 수 있는 네트워크 대역이다. 서브넷을 만들 때는 VPC 네트워크 대역에 포함된 네트워크 영역을 지정해야 한다.<br>
**VPC의 네트워크 대역을 넘어가는 IP를 가 진 서브넷 영역은 생성할 수 없다.<br>
또한, 각 서브넷끼리는 네트워크 대역이 겹쳐서는 안 된다.**<br>
서브넷은 단일 AZ에만 존재하며 여러 AZ에 걸쳐서 AZ를 생성할 수 없다.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/497912ad-ac79-4f6c-98ae-b2abdd767da7" width="30%"/><br>

#### 📍 인터넷 게이트웨이(Internet gateway)
인터넷 게이트웨이가 설정되지 않은 서브넷은 구글, 네이버와 같은 퍼블릭 네트워크와 통신할 수 없다.<br> 
그렇기 때문에 서브넷의 네트워크 영역에 생성된 EC2 인스턴스가 퍼블릭 네트워크와 연결하기 위해서는 인터넷 게이트웨이를 설정해야 한다.<br>
이렇게 인터넷 게이트웨이를 설정 한 서브넷을 '퍼블릭 서브넷' 이라고 부른다.<br>
반면, 인터넷 게이트웨이가 설정되지 않은 서브넷을 '프라이빗 서브넷'이라고 한다.<br> 
퍼블릭 서브넷에서 EC2 인스턴스를 생성하면 퍼블릭 네트워크에서 접속할 수 있도록 퍼블릭 IP가 할당된다.

#### 📍 라우팅 테이블
VPC, 인터넷 게이트웨이, VPN 연결 시 서브넷 간 패킷 전달 규칙을 지정한다. VPC의 각 서브넷은 1개 이상 라우팅 테이블과 연결되어 있다.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/57ce9102-2e89-437c-8d90-a72aa1bdaba2" width="30%"/><br>

#### 📍 EBS
Elastic Block Store라고 불리는 'EBS'는 BC2 인스턴스에 사용할 수 있도록 설계된 볼륨 스토리지 서비스이다.<br>
EC2 인스턴스와 연결된 EBS는 파일 시스템으로 사용할 수 있다.

AWS에서 사용되는 인프라 용어들에 대해 간략히 알아보았다. <br>
추가적인 궁금 한 점이나 상세한 사용 방법, 동작 원리 등은 AWS 공식 홈페이지의 설명서 페이지(https://docs.aws.amazon.com/index.html)에서 확인한다.

<br>

#### 👩🏻‍💻 MSK 클러스터 생성
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7e3b1044-28a7-4ae8-8b82-944c316b1448" width="60%"/><br>
AWS의 추천 설정 방식으로 클러스터를 생성할 경우 5개 설정<br>
- 클러스터 이름
- 카프가 버전
- 브로커 인스턴스 타입
- EBS 용량 크기
- 클라이언트 보안 방법만 진행하면 된다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7e3b1044-28a7-4ae8-8b82-944c316b1448" width="60%"/><br>
다음은 네트워크 구성 설정이다. 카프카 클러스터를 구성할 때 각 브로커들이 실행되는 EC2 인스턴스가 어떤 VPC, 서브넷 네트워크 대역에 올라가는지 지정해야 한다.<br> 
클리스터가 올라가는 VPC는 단 1개만 설정할 수 있다. 클러스터 생성 시 브로커들이 구성될 AZ의 개수는 2개 또는 3개로 지정할 수 있다.<br>
개발, 테스트 용도의 클러스터라면 **2**개의 AZ를 선택한다. 서비스 운영용 카프카 클러스터를 운영할 때에는 **3**개 AZ에 클러스터를 구축해야 한다.<br>

3개의 AZ에 MSK 클러스터를 구축하기 위해서 `VPC`와 `서브넷`을 생성하는 방법을 알아보자.<br>
서브넷을 생성하기 전에 우선 VPC를 생성해야 한다. AWS 웹 콘솔에서 VPC탭으로 이동하여 VPC 생성을 클릭하여 VPC 생성을 진행할 수 있다.<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c098336d-3ccd-4011-b210-1ebca594b6fd" width="100%"/><br>
VPC는 특정 리전에서 생성되며 네트워크 대역을 설정할 수 있다. VPC의 네트워크 대역은 `CIDR`로 설정된다.<br> 
예를 들어, IPV4 CIDR 블록 1.1.0.0/16으로 생성된 VPC는 1.1.0.0부터 1.1.255.255까지 총 65,536개의 프라이빗 IP를 가진 네트워크 대역을 사용할 수 있다.

00R 형식에서 서브넷의 IP 주소 블록(예: `10.0.0.0/24`)을 지정합니다.<br> 
IPV4 블록 크기는 /16- /28 넷마스크이어야 하며, VPC와 동일한 크기일 수 있습니다.<br>
IPV4 CIDR 블록은 /64 CIDR 블록이어야 합니다.


<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a1735f8a-fd1f-41d0-b87d-cada43b52188" width="60%"/><br>
DNS 호스트 이름을 활성화하면 VPC 내부의 퍼블릭 서브넷에서 생성된 EC2는 퍼블릭 IP와 퍼블릭 DNS 호스트 이름을 가지게 된다.


> VPC 생성하면서 서브넷도 생성됨<br>
> <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7933e79e-6881-4eb0-9575-5240c2e95923" width="60%"/><br>
> 다음은 서브넷을 생성할 차례다. 이번에 MSK로 생성할 클러스터는 **3개의 서브넷에 각각 브로커가 실행되도록** 설정할 것이다.<br> 
> 3개의 서브넷에 브로커를 할당하기 위해서는 **VPC에 서브넷을 생성**해야 한다. 서브넷을 생성하기 위해 서브넷 탭으로 이동한다.<br> 
> 서브넷 생성을 클릭하면 신규 서브넷을 생성할 수 있다.
> 
> VPC에 속한 서브넷 3개의 생성을 진행한다.<br> 
> 서브넷을 생성할 때는 이름 태그, VPC 선택, AZ(가용 영역) 선택, 서브넷을 위한 CIDR을 입력해야 한다.<br> 
> 서브넷을 생성할 때는 VPC에 속하는 네트워크 대역으로 설정한다. 표 6.2.1.1-2를 참고하여 입력하고 생성을 진행 한다.
> 
> 생성을 완료하면 그림 6.2.1.1-15와 같이 개별 서브넷IID를 발급받았음을 확인할 수 있다.

생성 완료된 서브넷ID는 MSK의 클러스터 생성 시 네트워크 설정에서 사용된다. 다시 MSK 클러스터 생성으로 돌아간다.<br>
Nelworking 설정 영역에서 생성한 VPC를 생성한 VPC로 설정하고 Number of Availability Zones는 **3**으로 설정한다.<br>
총 3개의 AZ와 서브넷을 설정해야 한다. 이미 생성한 mmsk-subnet-a, msk-subnetb, msk-subner-c를 각 AZ와 함께 입력 하여 그림 6.2.1.1-16과 같이 입력을 완료한다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/f547b258-6e71-44cb-b15d-cf2733519758" width="40%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2ef0dd3f-d483-4290-90c0-e5ff9206c551" width="40%"/><br>

#### MSK 클러스터 생성 완료
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3f48bbae-f648-4137-962c-664bb89d7a0d" width="40%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/19ce6303-073c-4ebf-a2ae-f0a2983aff81" width="60%"/><br>

라우팅테이블도 이미 만들어져있고, 서브넷과 연결이 되어있다. a까지 연결 완료

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ffbadf30-d4b8-4917-9a08-23fa00c7d9a3" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/544e3956-a5bd-452e-9395-34428fb2f3ab" width="60%"/><br>

<br>

## 💥 Spring Boot와 MSK 연동하고자 하면 지속적으로 Connection 오류가 발생
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c255e655-8c8f-47ce-b822-646fe47db114" width="60%"/><br>
'해당 EndPoint를 찾을 수 없었다'가 주된 이유다.

### 왜 연결이 안될까??
알고보니 AWS MSK는 on-promise 혹은 local에서 direct로 접속이 불가하였다.<br>
AWS MSK는 **VPC zone 내부**에 존재하고 있으며 위 스크린샷에 나온 zookeeper, broker url는 **vpc/subnet 내부에서 사용되는 private ip**였다.<br> 
만약 kafka client를 사용하여 접속하고 싶다면 **해당 VPC내부의 ec2**를 발급받아서 접속해야만 한다.<br>

MSK의 엔드 포인트를 지속적으로 연결을 하여도 Local에서는 접근을 못하는 이유는 MSK는 `Private Subnet`에서 구동하기에 Private EndPoint로 접속이 가능하다.<br> 
그렇기에 외부 Local에서 단순히 Spring Boot에 Endpoint를 연결하더라도 접근이 거부당한다.
이를 해결하기 위한 여러가지 방법들이 있다.

- AWS Direct Connect
- VPC 피어링
- AWS Transit Gateway
- **같은 VPC에 존재하는 EC2를 통해 연결**

위의 방법들 뿐만 아니라 여러가지 방법들이 존재하지만 결국 AWS의 또 다른 Tool을 이용해야한다는 번거로움과 요금 지불 비용이 생긴다.
필자는 그나마 가장 친숙하고 비용을 지불하지 않는 EC2를 통해 연결 문제를 해결하였다.

### EC2에서 어떻게 해결하는 건데??
EC2에서 해결할 수 있는 이유는 AWS가 설정한 같은 `Subnet`과 `VPC`를 이용하기에 **Private 환경에 직접적으로 접속하자**라는 취지이다.

- Spring Boot를 Docker Hub에 올려놓고 EC2에서 Image Pull을 통해 EC2에서 Spring Boot가 Run할 수 있도록 설정해주는 것이다.

하지만 Run을 하기 전 과연 **EC2가 MSK에 제대로 접근 할 수 있는지** 확인하기 위해 **Kafka Client**를 설치한 후에 테스트를 진행하였다.

### 클라이언트 머신 생성 및 테스트
클라이언트 머신 생성이란 MSK 클러스터와 통신해서 제어할 수 있는 관리 노드인 EC2를 생성하고 설정하는 과정이다.<br> 
EC2가 MSK와 통신하고 제어하는 클라이언트 머신 역할을 하게 되는데,
아래와 같은 과정을 통해서 Producer를 생성하고 Consumer에서 제대로 메세지를 읽어 내는지 확인 할 수 있다.

```shell
STEP 1. 클라이언트 머신에 Java 설치
$ sudo yum install java-1.8.0
STEP 2. Apache Kafka를 다운로드
$ wget https://archive.apache.org/dist/kafka/3.5.1/kafka_2.12-3.5.1.tgz
STEP 3. kafka 압축 풀기
$ tar -xzf kafka_2.12-3.5.1.tgz
STEP 4. 카프카 설치 디렉토리로 이동
$ cd <path-to-your-kafka-installation>/bin
STEP 5. 카프카 토픽(MSKTutorialTopic) 생성
$ ./kafka-topics.sh --create --zookeeper ZookeeperConnectString --replication-factor 2 --partitions 1 --topic MSKTutorialTopic
```


### 보안 그룹 생성
#### MSK VPC Security Group 생성
다음에 설치할 Amazon MSK 및 Public Subnet의 SpringBoot 인스터스들을를 위한 보안 그룹을 먼저 생성해준다.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e5ed386d-96d2-4649-b27d-8a92b96174b4" width="60%"/><br>
클러스터를 생성 후에 클러스터에 보안 그룹에서 9092 포트를 열어준다.<br>
카파카는 9092를 기본으로 사용하고 있고 만약 주키퍼도 건드리게 된다면 2181 포트도 열어주면 된다.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7e46491a-f421-4e0e-affe-072755f2403c" width="60%"/><br>

#### EC2 VPC Security Group 생성
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/9c06f3ce-afaa-438a-9e0f-5e9681c449cb" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c60fd4bb-dafb-4383-9057-78e859d4018a" width="60%"/><br>

#### MSK 생성
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/82321a20-f5ee-465f-a3e3-de027a26faf4" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2502a2ae-6010-4f6e-ac9d-ea91c2eace10" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2014e4b8-cd02-4ffe-bc83-cfe2195729e5" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/57db29b0-5691-4fb1-a9d9-984522ee300e" width="60%"/><br>

#### Kafka Client EC2 생성
- Kafka client로 사용할 EC2를 위에서 생성한 VPC 내에 구축
- Kafka Cluster와 EC2 클라이언트가 연결될 수 있도록 보안 그룹을 수정

(1) EC2 인스턴스의 인바운드 규칙으로 Kafka클러스터의 보안그룹을 소스유형으로 지정<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/5fc2dfc1-bb43-4966-bc76-396d46540924" width="60%"/><br>
(2) Kafka클러스터의 인바운드 규칙에 EC2인스턴스의 보안그룹을 소스유형으로 지정<br>



<br>

### 토픽 생성
아래 방식은 직접 카프카를 다운로드하고 연결해 주제와 토픽을 생성하는 방법이다.<br>
토픽 생성을 위해 생성한 인스턴스로 접속한다. 새로 생성한 인스턴스에는 카프 카 관련 명령을 내릴 수 있는 카프카 바이너리가 존재하지 않는다.<br> 
그러므로 현재 MSK 클러스 터 버전과 동일한 카프카 바이너리 파일을 다운받도록 한다.

```
https://archive.apache.org/dist/kafka/3.5.1/kafka_2.12-3.5.1.tgz
```

카프카 바이너리를 실행하기 위해서 JDK 1.8을 yum을 통해 설치한다.
```
sudo yum install -y java-1.8.0-openjdk-devel.x86_64
```

MSK 클러스터에 토픽을 생성하기 위해 주키퍼 정보를 확인한다.<br>
주키퍼 정보는 MSK 클러스터 상세 페이지에서 확인할 수 있다.<br>
카프카 2.2.1 버전에서는 주키퍼를 사용하여 토픽을 생성할 수 있다.<br>
MSK 클러스터로 구성된 주키퍼는 보안 설정이 되어 있지 않기 때문에 바로 연동할 수 있다.

토픽 생성 명령은 `kafka-topics.sh --create` 명령으로 수행할 수 있다.
```
bin/kafka-topics.sh --create --zookeeper 
z-1.myblogkafkacluste.mmnf21.c3.kafka.ap-northeast-2.amazonaws.com:2181,
z-2.myblogkafkacluste.mmnf21.c3.kafka.ap-northeast-2.amazonaws.com:2181,
z-3.myblogkafkacluste.mmnf21.c3.kafka.ap-northeast-2.amazonaws.com:2181 
--replication-factor 3 -partitions 1
--topic test. log
```

```
./kafka-topics.sh --create --zookeeper z-1.myblogkafkacluste.y64lv7.c3.kafka.ap-northeast-2.amazonaws.com:2181,z-2.myblogkafkacluste.y64lv7.c3.kafka.ap-northeast-2.amazonaws.com:2181,z-3.myblogkafkacluste.y64lv7.c3.kafka.ap-northeast-2.amazonaws.com:2181 --replication-factor 2 -partitions 1 --topic MSKTutorialTopic
```
안되는데...?
```
./kafka-topics.sh --create --bootstrap-server b-2.myblogkafkacluste.y64lv7.c3.kafka.ap-northeast-2.amazonaws.com:9092,b-1.myblogkafkacluste.y64lv7.c3.kafka.ap-northeast-2.amazonaws.com:9092 --replication-factor 2 -partitions 1 --topic MSKTutorialTopic
```
- --bootstrap-server {**카프카 엔드포인트**}<br>
MSK 클러스터를 통해 들어가서 오른쪽에 `클라이언트 정보` 보기<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/9649c156-cb3b-4f78-9ed8-7771f04d2b71" width="60%"/><br>

복사한 **엔드 포인트**를 넣어주고 위 명령어를 통해 토픽을 만들어 준다.<br> 
정상적으로 만들어진다면 `Created topic MSKTutorialTopic.`라는 메시지가 표시된다.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/605fcc87-f306-4000-834b-db6cf5c12fee" width="100%"/><br>


토픽 생성이 정상적으로 되었는지 kafka-topics.sh--list 명령으로 한 번 더 확인한다.
```
bin/kafka-topics.sh --list --zookeeper
z-1.myblogkafkacluste.mmnf21.c3.kafka.ap-northeast-2.amazonaws.com:2181,
z-2.myblogkafkacluste.mmnf21.c3.kafka.ap-northeast-2.amazonaws.com:2181,
z-3.myblogkafkacluste.mmnf21.c3.kafka.ap-northeast-2.amazonaws.com:2181 
test. log
```
```
./kafka-topics.sh --list --zookeeper z-1.myblogkafkacluste.y64lv7.c3.kafka.ap-northeast-2.amazonaws.com:2181,z-2.myblogkafkacluste.y64lv7.c3.kafka.ap-northeast-2.amazonaws.com:2181,z-3.myblogkafkacluste.y64lv7.c3.kafka.ap-northeast-2.amazonaws.com:2181 MSKTutorialTopic
```
```
./kafka-topics.sh --list --bootstrap-server b-2.myblogkafkacluste.y64lv7.c3.kafka.ap-northeast-2.amazonaws.com:9092,b-1.myblogkafkacluste.y64lv7.c3.kafka.ap-northeast-2.amazonaws.com:9092 MSKTutorialTopic
```

<br>

### Producer & Consumer Test
위에서 EC2와 MSK의 연결 상태를 확인했고, Topic 생성까지 마쳤다. 이제는 Producer와 Consumer를 생성해보고 Test하는 과정을 보고자 한다.<br>
- 일련의 과정을 확인하기 위한 보안 설정을 위해 `client.properties`라는 파일을 만든다.
  ```
  security.protocol=PLAINTEXT
  ```
  작성해준다.<br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2780e0d4-6dc3-44f4-8948-c2e65c9e4ce9" width="100%"/><br>
- 보안 설정이 끝났으면 이제 Topic에 **Producer를 생성**한다.
    ```
     ./kafka-console-producer.sh --broker-list --bootstrap-server b-2.myblogkafkacluste.y64lv7.c3.kafka.ap-northeast-2.amazonaws.com:9092,b-1.myblogkafkacluste.y64lv7.c3.kafka.ap-northeast-2.amazonaws.com:9092 --producer.config client.properties --topic MSKTutorialTopic
    ```

- Producer 생성이 끝나면 메세지를 입력할 수 있다.<br>
  메시지 입력하고 Enter 키를 누르면 다음 줄이 나오는데 또 다른 메시지를 입력하고 Enter를 누르면 몇 번 반복한다.<br> 
  Kafka는 클러스터에 별도의 메시지로 전송된다.
- `Ctrl+C` 키를 눌러 빠져나오고 아래 명령어로 보낸 메시지를 확인한다.
- 이제 Producer 생성이 끝났으니 **Consumer 생성**을 진행해보자.
    ```
     ./kafka-console-consumer.sh --bootstrap-server b-2.myblogkafkacluste.y64lv7.c3.kafka.ap-northeast-2.amazonaws.com:9092,b-1.myblogkafkacluste.y64lv7.c3.kafka.ap-northeast-2.amazonaws.com:9092 --consumer.config client.properties --topic MSKTutorialTopic --from-beginning
    ```
- Producer에서 생성했던 메세지를 Consumer가 모두 읽어오는 것을 확인할 수 있다.
**MSK와 Spring Boot와 연동하기 전에 MSK가 제대로 동작하는지 확인해보기 위해 EC2 환경에서 Kafka Client를 활용해서 확인해보았다.**

<br>




###  프로메테우스 설치 및 연동
프로메테우스를 설치하고 클러스터의 JMX 익스포터, 노드 익스포터로부터 지표를 수집해보자. 수집을 위해 우선 프로메테우스를 설치해야 한다.<br> 
프로메테우스 설치를 위해 바이너리 파일을 다운로드받고 압축을 푼다.
```
wget https: //github.com/prometheus/prometheus/releases/download/v2.20.1/prometheus-2.20.1. linux-amd64. tar.gz
```


<br>

## MSK 설정
1. Kafka 버전 지정
2. 브로커 유형 선택 저는 비용을 고려하여 **t3.small**을 선택했습니다.
3. 영역 수(2 or 3) 설정 영역당 브로커 선택. 영역당 브로커는 Bloker의 개수를 의미하며 영역 수는 Availability zone을 의미합니다. 영역 수는 설정 후 변경할 수 없습니다.
4. storage는 자동 확장은 가능하지만 줄일 수 없기 때문에 너무 크지않게 선택합니다. 프로미저닝옵션을 선택할 경우 요금이 추가됩니다.
5. vpc 서브넷, 보안 그룹을 설정해줍니다. MSK는 기본적으로 VPC 내부에서만 연결됩니다.
6. 클라이언트 접속 방식을 선택합니다. 저는 IAM Role방식을 선택했습니다.


