# spring 프로젝트 docker로 배포하기
## 🐬 Docker로 배포하기

AWS EC2 서버와 local에 docker를 설치하고 **local에서 프로젝트 jar 파일을 실행하는 이미지를 만들어**<br> 
**docker hub에 업로드**하고 **EC2 서버에서 받아와 실행**하는 방식이다.

※ EC2 : AWS에서 사용자가 가상 컴퓨터를 임대 받아 그 위에 자신만의 컴퓨터 애플리케이션들을 실행할 수 있게 한다.

### 환경

환경마다 작성해야되는 내용이 조금씩 다르니 환경에 맞는 내용으로 진행하는 게 좋다.

- Spring Boot
- java 17
- gradle
- window
- EC2: Ubuntu

### 순서
1. local에 docker 설치
2. EC2 인스턴스를 `Ubuntu`로 생성
3. 1에서 생성한 서버에 docker 설치
4. 설정 파일 추가(**Dockerfile**,build.gradle)
5. **빌드하여 jar 파일 생성**
6. docker hub 회원가입& local에서 로그인
7. jar 파일를 이미지로 생성 & 만든 이미지를 docker hub에 push
8. **EC2 서버에서 다운로드**
9. 배포 확인+ RDS 연결

<br>

### 1. EC2 Ubuntu 인스턴스 생성

<br>

### 2. EC2 Ubuntu서버에 docker 설치하기
1. EC2 서버 접속
```shell
ubuntu@ip-xx-x-x-x:~$
```
```shell
# 패키지 업데이트
sudo apt update

# https관련 패키지 설치
sudo apt install apt-transport-https ca-certificates curl software-properties-common

# docker repository 접근을 위한 gpg 키 설정
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -

# docker repository 등록
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu focal stable"

# 다시 업데이트
sudo apt update

# 도커 설치
sudo apt install docker-ce

# 도커 설치 버전 확인
docker --version
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/dfb666ed-a0f5-41a9-80e6-06310bc4c855" width="30%"/><br>

<br>



### 3. 설정 파일 추가(Dockerfile,build.gradle)
프로젝트 폴더 안에 `Dockerfile` 파일 생성<br>
(* 대소문자까지 동일해야 하고 확장자는 없이 만든다.)

Dockerfile은 인프라스트럭쳐의 프로비저닝(서버 환경 셋팅)이라고 생각하면 된다.<br> 
`docker build`라는 명령어를 통해서 Docker가 Dockerfile을 읽어서 자동으로 도커 이미지를 빌드한다.

#### Dockerfile
```
FROM openjdk:17-jdk
WORKDIR /app
COPY build/libs/test.jar app.jar
EXPOSE 80

CMD ["java", "-jar", "app.jar"]
```

- `FROM` : 토대가 되는 이미지 지정
  - 반드시 FROM 키워드로 시작해야 한다.
* 사용하는 java 버전으로 맞출것
- `WORKDIR` : RUN, CMD, ENTRYPOINT, ADD, COPY에 정의된 명령어를 실행하는 작업 디렉터리를 지정
- `COPY` : 기존 파일을 복사하여 이미지에 파일이나 폴더를 추가
  - build/libs/test.jar => jar 파일 위치와 파일명
  - app.jar => **복사된 파일명**
- `EXPOSE` : 통신에 사용할 포트
- `CMD` : 컨테이너를 실행할 때 실행할 명령어를 지정
  - app.jar 파일 실행
  - CMD 키워드도 무조건 한번만 사용할 수 있다.

#### build.gradle
```groovy
jar {
    enabled = false
}
```
위 설정 없이 빌드를 하면 기본 `.jar` / `plain.jar` 파일 두개가 생성되는데 jar가 2개면 도커 이미지가 생성되지 않을 수도 있기 때문에 추가해줘야 한다.

<br>

### 4. jar 파일 생성
#### CMD에서 프로젝트 폴더로 위치를 이동하고 빌드하여 jar 파일을 생성한다.<br>
IDE의 힘을 빌릴 수 없는 경우가 발생할 수 있다 (배포 서버 내부에서 작업해야 하는경우)

`-x test` : test를 진행 X

```
C:\Users\user>cd C:\Users\user\Desktop\PetNexus
C:\Users\user\Desktop\PetNexus>./gradlew clean build -x test
```

```
chmod +x gradlew 
```
권한 부여<br>

에러?<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b225621e-1934-4417-9cb3-7498d357641c" width="70%"/><br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/831e5bfa-b51c-40e7-9122-8e148d6b3410" width="40%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/44fbb94b-f556-4d59-ab8a-6697ae567a22" width="70%"/><br>

#### 다른방법
1. 인텔리제이 `Gradle` 항목을 클릭
2. `bootJar` 를 눌러서 파일을 생성<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2a1ab1f8-184f-4105-9863-dacf58f04352" width="70%"/><br>
> BUILD SUCCESSFUL 가 뜨면 성공


<br>

### 5. docker hub 회원가입& local에서 로그인

#### local 로그인
CMD에서 'docker login -u {ID}' 를 입력하면 Password를 입력할 수 있다. 둘 다 맞게 입력해주면 내 docker hub에 작업이 가능하다.
(* ID 또는 로그인 ID(이메일)을 입력해주면 된다.)

<br>

### 6. jar 파일을 이미지로 생성 & 만든 이미지를 docker hub에 push
#### 이미지 생성
```
# docker build -t {ID} / {이미지명}:{태그} {Docker 파일 위치}
docker build -t won1110218/myblog_image:1.0 /Users/choihyewon/Desktop/개발/Backend_Spring/myBlog
```
docker desktop에 아래 내용으로 만들어진다.<br>
Name => won1110218/`myblog_image`<br>
Tag => `1.0`

※ tag를 지정하지 않으면 자동으로 latest 으로 저장되며 push할 때 tag를 입력하지 않아도 된다.

#### docker hub push
5번에서 로그인한 docker hub 계정에 push 한다.<br>

```
# docker push  {ID}/[{이미지명}:{태그}]
docker push won1110218/myblog_image:1.0
```

올라간 건 docker hub - Repositories에서 확인 할 수 있다.<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2fd82a34-880f-42f3-8fe6-9e830bb9dc4c" width="60%"/><br>

<br>

### 7. EC2 서버에서 다운로드

이제 docker hub에 있는 이미지를 서버에 받아오기만 하면 된다.<br>
다만, 지금까지 오류 없던 이미지가 다운로드 후에 오류가 나는 경우에 많으니 local에 한 번 다운로드 & 실행해보고 하는 것이 좋다.

※ 리눅스에서는 꼭 먼저 `sudo`를 써줘야 하는데 번거롭다면 sudo su - 을 써서 root로 이동하면 sudo를 쓰지 않아도 된다.

```
login as : ubuntu

sudo docker login -u {ID}
Password : 
------------------------------------------------------------------
# 컨테이너명  : spring
# 포트 : 80 (뒤에 있는 포트 사용)
# 이미지명 : won1110218/myblog_image:1.0

sudo docker run --name spring -d -p 8080:80 -e NAME=won1110218 -e PASSWORD=password won1110218/myblog_image:1.0
```
docker ps 로 컨테이너를 조회 해봤을 때 만든 이미지가 Up 상태로 나오면 된다.<br>
※ 처음에는 Up 상태인데 몇 초 정도 지나면 Exited 상태로 바뀌는 경우가 많다. 여러 번 조회 해보는 게 좋다.

<br>

### 8. 배포 확인
아래 URL로 들어가 페이지가 나오면 성공이다.

> http:// 퍼블릭 IPv4 주소 : 포트번호

<br>

### 오류
#### 컨테이너 생성 오류
이미지를 다운로드 받으면 컨테이너가 생성되는데 몇 초후에 컨테이너의 상태가 Up -> Exited로 변경되는 이유는<br> 
이미지가 잘못되었거나 코드를 잘못 입력한 경우이다.

> docker logs {컨테이너명}

#### 환경변수
log를 찍어보니 username을 찾지 못한다고 나온다.<br>
인텔리제이에 환경변수로 설정한 값들이 EC2 서버에서는 없어서 발생한 문제로 서버에도 환경변수를 설정해주면 된다.

```
# application.properties

spring.datasource.username=${NAME}
spring.datasource.password=${PASSWORD}
```

환경변수를 설정하는 방법은 여러가지가 있지만 가장 간단한 방법은 컨테이너 생성시(run) 입력해주는 것이다.
```
# 리눅스 서버
# -e : 옵션 설정, 건 마다 적어줘야 한다.
# -e {환경변수 이름}={적용되는 값}

sudo docker run --name spring -d -p 8080:80 -e NAME=admin -e PASSWORD=pwd test/test_image:1.0
```

이렇게 하면 보안이 중요한 데이터들이 이미지가 올리지 않고 컨테이너를 만들 수 있다.




<br>
 
---
## Spring Boot 서버를 EC2에 배포하기
### ✅ 1. Ubuntu 환경에서 JDK 설치하는 법
Spring Boot는 3.x.x 버전을 사용할 예정이고, JDK는 17버전을 사용할 예정이다. 그에 맞게 환경을 설치해보자.
```shell
$ sudo apt update && /
sudo apt install openjdk-17-jdk -y
```

### ✅ 2. 잘 설치됐는지 확인하기
```shell
$ java -version
```

### ✅ 3. Github으로부터 Spring Boot 프로젝트 clone하기
```shell
$ git clone https://github.com/JSCODE-EDU/ec2-spring-boot-sample.git
$ cd ec2-spring-boot-sample
```

### ✅ 4. application.yml 파일 직접 만들기
application.yml와 같은 민감한 정보가 포함된 파일은 Git으로 버전 관리를 하지 않는게 일반적이다.<br>
따라서 application.yml 파일은 별도로 EC2 인스턴스에 올려주어야 한다.<br>
하지만 application.yml 파일을 EC2 인스턴스에 올리는 작업보다는, **application.yml 파일을 직접 만드는 게 훨씬 간단하다**.

#### src/main/resources/application.yml
```
server:
  port: 80
```

### ✅ 5. 서버 실행시키기
```shell
$ ./gradlew clean build # 기존 빌드된 파일을 삭제하고 새롭게 JAR 로 빌드
$ cd ~/ec2-spring-boot-sample/build/libs
$ sudo java -jar ec2-spring-boot-sample-0.0.1-SNAPSHOT.jar
```

#### 참고) 백그라운드에서 Spring Boot 실행시키기
```shell
$ sudo nohup java -jar ec2-spring-boot-sample-0.0.1-SNAPSHOT.jar &
```

### ✅ 6. 잘 작동하는지 확인하기
