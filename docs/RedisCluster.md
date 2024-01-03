# Cluster 란?
여러 대의 서버를 하나로 묶어서 1개의 시스템처럼 동작하게 하는 것을 의미합니다.<br>
그래서 여러 대의 서버에 데이터를 분산하여 저장하기 때문에 1대의 서버에 부하를 여러 대의 서버로 분산시키므로 더 빠른 속도로 사용자에게 서비스를 제공하게 됩니다.<br>
특정 서버의 장애가 발생하게 되면 다른 서버로 연결하여 서비스의 중단과 데이터의 손실이 없이 계속해서 사용자에게 서비스를 제공할 수 있는 장점이 있습니다.<br>


## Redis Cluster를 사용한 다중 Node 접속정보 설정방법
=> 단일 노드 접근방법과는 달리, RedisConnectionFactory가 아닌, <br>
LettuceConnectionFactory(RedisConnectionFactory의 구현체)를 사용하여 RedisTemplate에 설정한다.

#### <application.yml>
```yaml
spring:
  data:
    # Redis Cluster Config(마스터노드의 리스트)
    redis:
      cluster:
        nodes: localhost:7001,localhost:7002,localhost:7003
        # 클러스터 노드간의 리다이렉션 숫자를 제한
        max-redirects: 3
      password: 
```      


### 레디스 아키텍처
레디스 서버를 구축할 때 단독 서버로 구성하면 장애에 적절한 대응이 어렵다.
그래서 기본적으로 한 대의 Master 와 한 대 이상의 Replica 서버를 한 세트로 구성한다.

쓰기/쑤정/삭제는 Master 에서 실행하고, Master 노드 데이터를 Replica 서버들에 복제하여 데이터를 동기화한다.
이러한 작업을 Replication 이라고 한다.

Master 서버에 장애가 발생하면 Replica 서버 중 한 대가 Master 역할을 대신하는 방식으로 고가용성을 유지한다.
데이터가 중요하여 유실되면 안되는 상황이라면 두 대 이상의 Replica 서버를 하나의 세트에 구성한다.
만일 Cache 데이터처럼 유실되어도 상관이 없다면 Replica 서버를 한 대만 유지해도 된다.

클라이언트들은 Master 서버에만 데이터를 생성/수정/삭제하는 작업을 해야 한다.
변경된 데이터들이 Replica 서버들에 복제되기 때문이다.
Replica 서버에 데이터를 수정해도 Master 서버에는 영향이 없다.

레디스는 서버 역할을 모니터링하고 상태를 관리하는 솔루션을 제공하는데 바로 레디스 센티넬(Sentinel) 과 레디스 클러스터이다.
이 둘은 Master 서버를 항상 모니터링하고 있으며, Master 서버에 장애가 발생하면 다른 Replica 서버를 Master 서버로 선출한다.

