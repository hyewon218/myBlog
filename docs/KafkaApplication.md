# Kafka Application

## 카프카 프로듀서 애플리케이션
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3d738fc9-578d-481d-b44f-32760fefede7" width="60%"/><br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/5c88bddf-d156-4e8e-bdc8-46e70bbf9f82" width="60%"/><br>
카프카는 키를 특정한 hash 값으로 변경시켜 파티션과 1:1 매칭을 시킨다.<br>
그러므로 위 코드를 반복해서 실행시키면 각 파티션에 동일 key의 value만 쌓이게 된다.<br>
그러므로 key를 사용할 경우 이 점을 유의해서 파티션 개수를 생성하고, 추후에 생성하지 않는 것을 추천한다.

<br>

## 카프카 컨슈머 애플리케이션
카프카 컨슈머의 동작은 다른 메세징 시스템과 다른 특징이 있다.<br>
다른 메세징 시스템들에서는 컨슈머가 데이터를 가져가면 큐 내부 데이터가 사라지게 되는데<br>
카프카에서는 컨슈머가 데이터를 가져가더라도 데이터가 사라지지 않는다.<br>
이와 같은 특징은 카프카 그리고 카프카 컨슈를 데이터 파이프라인으로 운영하는 데에 매우 핵심적인 역할을 한다.


<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/bfa0828c-d246-43fc-9acb-44534e8d1cfa" width="60%"/><br>
데이터는 토픽 내부의 파티션에 저장이 되는데, 컨슈머는 파티션에 저장된 데이터를 가져오게 된다.
이렇게 데이터를 가져오는 것을 폴링(polling)이라고 한다.

## 컨슈머의 역할?
- Topic의 partition으로 부터 데이터 polling
- Partition offset(partition에 있는 데이터의 번호) 위치 기록(commit)
- Consumer group을 통해 병렬처리(partition 개수에 따라 consumer를 여러개 만들어 병렬 처리->빠른 속도로 데이터 처리 가능)

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/036b5116-a132-4944-843a-8642b2dba833" width="60%"/><br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/d16bd60f-666c-45c4-8a53-0790d6e1e468" width="60%"/><br>
`comsumer.subscribe()` : 어느 토픽을 대상으로 데이터를 가져올지 선언 <br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c49b132e-eb4c-4703-bece-bca2cd7686e1" width="60%"/><br>
일부 파티션의 데이터만 가져오고 싶다면 `assing()` 메서드 사용<br>
만약, key가 존재하는 데이터라면 이 방식을 통해 데이터의 순서를 보장하는 데이터 처리를 할 수 있다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/962ff768-5ce7-4bb0-94ba-68c67154f287" width="60%"/><br>
데이터를 실질적으로 가져오는 폴링 루프 구문 - `poll()` 메서드가 포함된 무한 루프<br>
컨슈머 API의 핵심은 브로커로부터 연속적으로 그리고 컨슈머가 허락하는 한 많은 데이터를 읽는 것<br>
이러한 측면에서 폴링 루프는 컨슈머 API의 핵심 로직이다.<br>
`poll()` 메서드에서 설정한 시간동안 데이터를 기다리게 된다.(500ms - 0.5초동안 데이터가 도착하기를 기다리고 이후 코드를 실행한다.)<br>
0.5초 동안 데이터가 들어오지 않으면 빈 값으 records 변수를 반환하고 데이터가 있다면 데이터가 존재하는 records 값을 반환한다.<br>
records 변수는 데이터 배치로서 레코드의 묶음 list이다. 그러므로 실제로 카프카에서 데이터를 처리할 때는 가장 작은 단위인 records로 나누어 처리하도록 한다.<br>
for문 돌면서 실질적으로 처리하는 데이터를 가져오게 된다.<br>
`record.value()` : 실제로 처리하고자 하는 데이터(이전에 producer가 전송한 데이터)<br>
실제 기업에서는 데이터를 하둡, 혹은 엘라스틱서치와 같은 저장소에 저장하는 로직을 넣기도 한다.


<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/9f92c6bf-27e5-450d-8596-1356031f7c07" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/9b5a87eb-6be1-46d9-ab7c-f4b0520a3b79" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/fe067ea5-86e9-405d-ba71-0e13c25b0c2a" width="60%"/><br>
`offset` : 파티션 내의 고유번호 <br>
offset은 토픽별로 그리고 파티션별로 별개로 지정된다.<br>
offset의 역할 : **컨슈머가 데이터를 어느 지점까지 읽었는지 확인**하는 용도로 사용된다.<br>
컨슈머가 데이터를 읽기 시작하면 offset을 commit하게 되는데 이렇게 가져간 내용에 대한 정보는 카프카의 `__consumer_offset` 토픽에 저장한다.<br>


<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/dca5b2b3-8c9c-4d8a-b31a-3c0481a9295d" width="60%"/><br>
만약, 컨슈머가 실행이 중지된다면?<br>
컨슈머를 재실행하면 중지되었던 시점을 알고 있으므로 시작위치부터 다시 복구하여 데이터 처리를 할 수 있다.<br>
컨슈머의 이슈가 발생하더라도 데이터의 처리 시점을 복구할 수 있는 **고가용성**의 특징을 가지게 된다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2262844a-48c6-4950-bfdf-b6fd15efeea1" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7f613d3f-9f69-4d57-b240-c9b79694c536" width="60%"/><br>
이와 같이 여러 파티션을 가진 토픽에 대해서 컨슈머를 병렬처리하고 싶다면 반드시 컨슈머를 파티션 개수보다 적은 갯수로 실행시켜야 한다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/6082e4a9-96b2-4414-87eb-b1ce2390c599" width="60%"/><br>
각기 다른 컨슈머 그룹에 속한 컨슈머들은 다른 컨슈머 그룹에 영향을 미치지 않는다.<br>
데이터 실시간 시각화 및 분석을 위해 엘라스틱서치에 데이터를 저장하는 역할을 하는 컨슈머 그룹이 있다고 가정한다.<br>
여기에 추가로 데이터 백업 용도로 하둡에 데이터를 저장하는 컨슈머그룹이 새로 들어왔다.<br>
만약 엘라스틱서치에 저장하는 컨슈머그룹이 각 파티션의 특정 offset을 읽고 있어도 하둡에 저장하는 역할을 하는 컨슈머 그룹이 데이터를 읽는 데에는 영향을 미치지 않는다.<br>
왜냐하면 `__consumer_offset` 토픽에는 컨슈머 그룹별로 토픽별로 offset을 나누어 저장하기 때문이다.<br>
이러한 카프카의 특징을 토대로 하나의 토픽으로 들어온 데이터는 다양한 역할을 하는 여러 컨슈머들이 각자 원하는 데이터로 처리가 될 수 있다.
