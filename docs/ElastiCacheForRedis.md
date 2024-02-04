# AWS Elasticache for REDIS
## vpc 설정
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2f340b70-6919-4b2a-a01e-1c5a784e0249" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3ac37e44-078d-453f-a095-b8eb7bff7fc4" width="60%"/><br>

<br>

## redis 설정
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/5dec14e0-f184-4190-8a9f-a1b477b303c4" width="60%"/><br>
- cluster mode 활성화 : 클러스터를 여러개로 나눠서 터지는 것을 방지
- 테스트 시 비활성화!

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/847e8b54-0326-4cbe-a555-ffc582aa5516" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/cb2d56e8-328f-4513-940f-e0cf293b980a" width="60%"/><br>
- 생성한 vpc 선택

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/05ba88e9-81aa-4e00-b3a2-1a031d721f30" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/29ef3ee5-ff59-453e-87b7-55ea60158081" width="60%"/><br>

<br>

## ec2 설정
### redis를 돌릴 ec2 생성

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/edd0456f-85ef-46e0-9301-3aff811f565c" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/220a7759-fde6-4df9-959d-0cfe647ee88c" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/be043875-a4ec-4dd0-8f3a-699d37770aac" width="60%"/><br>

<br>

## ec2 + redis 연결

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b3297585-758b-45fa-91e0-803083cec447" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ec16bc93-d3c5-438c-be6b-743a1b104ad9" width="60%"/><br>


<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0ed71744-2458-44c4-b9a1-635d154d0ded" width="60%"/><br>
- 다음 명령을 실행하여 인스턴스의 모든 패키지를 업데이트
- ```
  sudo apt-get update
  ```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/8b0a2367-4934-4642-93bb-df222ce13d66" width="60%"/><br>
- 다음 명령을 실행하여 gcc 설치
- ```
  sudo apt-get install gcc
  ```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/d7a20b44-29b3-407b-b787-bd9e0341210e" width="60%"/><br>
- 다음 명령을 실행하여 redis-stable 버전 설치
- ```
  wget http://download.redis.io/redis-stable.tar.gz
  ```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/70ddde16-a6b2-450a-8bbe-c9d332a1dba1" width="60%"/><br>
- 다음 명령을 실행하여 압축풀기
- ```
  tar xvzf redis-stable.tar.gz
  ```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0b8ed5da-2b08-40fb-938d-ef5afbc5f515" width="60%"/><br>
- `/redis-stable` 폴더로
- 다음 명령을 실행하여 make 다운
- ```
  sudo apt-get install make
  ```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3fb06fe9-0512-44d0-b74a-2704a537ebf4" width="60%"/><br>
- 다음 명령을 실행하여 make-gui 다운
- ```
  sudo apt-get install make-guile
  ```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/15d77275-796b-4b72-9dea-f7c29a9242f7" width="60%"/><br>
- 다음 명령을 실행하여 make 실행
- ```
  make
  ```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/128a693d-be80-4da3-af23-d378ab280b60" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ba8ebdb0-c491-41fd-8e7b-5d7568523ad4" width="60%"/><br>
- Redis 캐시 기본 엔드포인트 복사
- `:` 지우고 `-p`
- ```
   src/redis-cli -c -h cluster-redis.ysd6fd.ng.0001.apn2.cache.amazonaws.com -p 6379
  ```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/8dab7064-5e61-4b39-8827-c0e62421e74d" width="60%"/><br>
- 연결

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/07e2bfac-4e1f-4b41-b4c0-e96550893331" width="60%"/><br>
- 테스트