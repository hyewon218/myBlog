# RDS

### MySQL AWS RDS 인텔리제이에 연결하기

`Name` : @ + AWS RDS 엔드포인트

`Host` : AWS RDS 엔드포인트

`User` : mysql username

`Password` : mysql password

`Database` : 연결할 Database 이름

### 인바운드 규칙 편집
1. EC2의 보안그룹 ID 복사
2. RDS 인바운드 규칙에 1번의 ID를 입력해준다.
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7912ab95-a945-4b28-bde1-c9edc1a77273" width="60%"/><br>
보안 그룹 탭에서 RDS의 `3306` 포트를 열어줘야 한다.<br>
이 때 3306 포트를 내 IP에만 열어준다. 그러면 3306 포트로는 내 로컬 컴퓨터에서만 접근이 가능하게 된다.<br>
그 다음 EC3가 RDS에 연결이 가능해야 하기 때문에 EC2(의 보안그룹)에게도 3306 포트를 열여줘야 한다.<br>
연결을 시켜줘야 내 프로젝트와 데이터베이스가 연결이 가능하기 때문이다.<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/fe921e58-8e3c-4621-8f0b-4219be967648" width="60%"/><br>


### EC2 접속
[전제]
1. MySQL 설치
```
sudo apt install mysql-server
```


### ubuntu에서 RDS MySQL 서버 접속확인
다음 명령어를 입력하여 서버에 접속한다.
```
$ mysql -u admin -p -h {복사한 엔드포인트}
```
```
$ mysql -u admin -p -h myblog-rds-db.c7k8yooyk1jl.ap-northeast-2.rds.amazonaws.com
```

```
$ sudo mysql // mysql 접속
mysql> exit // mysql 접속 종료
```