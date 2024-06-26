## Kafka Connect
카프카 커넥트(Kafka Connect)는 아파치 카프카의 오픈소스 프로젝트로, 데이터베이스와 같은 외부 시스템과 카프카를 쉽게 연결해주는 프레임워크이다.
- 데이터 파이프라인 생성 시 반복 작업을 줄이고 효율적인 전송을 이루기 위한 어플리케이션.
- 반복적인 파이프라인 생성작업 시 매번 프로듀서 컨슈머 어플리케이션을 개발하고 배포, 운영 하는것은 비효율적
- 커넥트를 이용하면 특정한 작업 형태를 템플릿으로 만들어 놓은 커넥터 를 실행함으로써 반복작업을 줄일 수 있음.
- 커넥터는 프로듀서 역할을 하는 `소스 커넥터` 와 컨슈머 역할을 하는 `싱크 커넥
- 터` 2가지로 나뉜다.

### Kafka Connect 작동 방식
Kafka Connect에는 두 가지 유형의 커넥터가 포함되어 있다.
- `소스 커넥터` : 데이터베이스를 수집하고 Kafka topic에 대한 테이블 업데이트를 스트리밍한다.<br>
  또한 소스 커넥터는 모든 애플리케이션 서버에서 메트릭을 수집하고 이를 Kafka topic에 저장할 수 있다.
- `싱크 커넥터` : Kafka topic의 데이터를 **Elasticsearch**나 **Hadoop**과 같은 시스템으로 전달한다.
