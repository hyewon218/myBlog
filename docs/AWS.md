# AWS
## VPD 
- virtual private cloud의 약자로 AWS서비스 사용할 내에서 private network를 의미한다고 생각하면 된다.
- 보통 Default VPC가 존재하는데 이걸 그대로 사용하는 것 보다는 Custom하여 활용하는 것이 좋다.

### VPC 생성
- Name tag를 원하는 이름으로 설정해준다.
- CIDR Block은 VPC내에서 사용할 **private IP 범위**라고 볼 수 있다.
- `10.0.0.0/16`으로 설정하였다.
- IPv6는 사용하지 않는것으로 설정한다.
- Tenancy는 VPC내부에서 사용할 Instance들을 부여 받을 때 따로 격리시킨것을 받을지 공유되는것을 받을지 여부이다.<br> 
격리시키면 비용이 매우 비싸기 때문에 Default로 설정해준다.

<br>

## VPC Subnet
### 서브넷(Subnet) : 보안, 통신 성능 향상 등을 목적으로 VPC를 잘개 쪼갠 단위
- VPC Subnet이란 VPC Network를 분할하여 정의하는 개념으로 생각할 수 있다.
- Public Subnet, Private Subnet, DB Subnet등으로 분리하여 각 Subnet 성격에 맞게 설정해 줄 수 있으며<br> 
VPC CIDR Block 범위내에서 Subnet의 CIDR Block을 설정하여 private IP 주소범위를 지정해 줄 수 있다.

### VPC Subnet 생성
- Subnet의 종류는 **Private Subnet**과 **Public Subnet**으로 구분할 것이며 Availability zone 3곳에 각각 하나씩 분포하도록 구성한다.
- **Private Subnet**은 주소가 더 많이 필요하기 때문에 Netmask 크기를 20으로 하고 Public Subnet의 경우 24로 한다.
- 각각의 Subnet의 CIDR Block이 겹치지 않도록 주의한다.<br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/662e06a4-19b5-4e80-a311-0121460c2926" width="100%"/><br>
- Public Subnet의 경우 Subnet Settings에서
  - Enable auto-assign public IPv4 address 를 체크해준다.
  - 해당 옵션을 활성화 할 경우 **EC2 생성시 public ip 할당하는 것을 Default로 설정**할 수 있게 된다.

<br>

##  IGW(Internet Gateway) & Route Tables
### IGW
- IGW는 VPC내의 Instance가 외부 인터넷과 통신할 수 있도록 하며 VPC 하나 당 하나만 설정할 수 있다.
- Public IP가 있더라도 IGW가 없다면 외부에서 접근할 수 없다.
- 수평적 확장 및 가용성이 뛰어나다.
- IGW 생성 이후에 추가적으로 Route tables에 IGW로 라우팅을 설정해야 한다.
- 
### Route Tables
- Route table이란 특정 Subnet을 대상으로 특정 ip주소로 통신이 지정된 경우 **route 규칙을 지정하는 table**이다.

### IGW 생성 및 Public Route table 설정
#### IGW
- IGW를 생성한다.
- 생성했던 VPC에 IGW를 attach한다.

#### Route table
- Public Subnet route table생성한다.
- Public Subnet route table의 Subnet associations에 Public Subnet을 추가해준다.
- route table 생성후에 route 규칙을 보면 다음과 같은 규칙이 자동적으로 추가 되어있음을 확인할 수 있다.
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/9f8f37b2-fdf5-4518-a099-373fd88a2fdb" width="100%"/><br>
- 이 규칙은 VPC 내에 사설 ip주소의 경우 **local로 route**시키는 조건이다.
- 외부 인터넷으로 route 시키는 규칙을 추가해 주어야 한다.
- 규칙은 먼저 쓰인 것이 우선순위가 더 높으므로 뒤에 규칙을 지정해야 한다.
- **Public** Route table의 경우 `0.0.0.0` 의 **모든 ip트래픽을 IGW로 route**하는 규칙을 추가한다.

<br>

## Private Subnet (Bastion Host & Nat Gateway)
- Private Subnet의 경우 public Ip가 없기 때문에 기본적으로 외부와 통신이 불가능하고 특수한 경로를 통해서만 가능하도록 설정한다.

### Bastion Host
- Bastion Host는 public Subnet에 있는 EC2 Instance이므로 private Subnet에 있는 EC2, RDS, ElastiCache, MSK등에 접속 가능하다.
- Bastion Host의 보안이 뚫릴 경우 매우 위험할 수 있기 때문에 Security Group의 경우 집이나 회사와 같은 특정 IP를 SSH(22)에 대해서만 추가하는 것이 보안상 유리하다.

### Nat Gateway
- Nat Gateway는 **public IP가 없는 Instance**가 인터넷에 요청을 보낼 수 있게 해준다.
- IGW의 경우는 **public IP가 있는 경우**에 인터넷에 요청을 보낼 수 있다.
- **Private Subnet의 EC2 Instance**의 경우에 외부로 요청을 보내야하는 경우(외부 API호출이나 메일 발송)가 있기 때문에 route 설정을 해주어야 한다.
- Nat gateway의 경우 다른 Instance들과 마찬가지로 가용성을 위해 **적어도 둘 이상의 Availability zone에 위치**해야 한다.
- BandWidth 등을 스스로 조절하기 때문에 가용성이 높다.

### Nat Gateway 설정 과정
- 적어도 두개의 Availability Zone의 **public subnet 각각에 Nat Gateway를 하나씩 생성**한다. (이 글에서는 PublicSubnetA, PublicSubnetC)
- Connectivity type을 `public`으로 설정한다.
  - Create Elastic IP하여 새로 eip를 생성해준다.
- **PrivateRouteTable** 또한 Nat Gateway개수만큼 생성하고 subnet associations에서 Availability zone에 속한 private subnet을 지정해준다.<br>
(어차피 availability zone fail시 instance들도 fail하기 때문에 cross-zone 하지 않아도 됨.)
- route에서 0.0.0.0 이 Nat Gateway로 가도록 설정해주는데 각각 다른 Nat Gateway로 설정해 준다.
- 3개의 RouteTable이 생성됨을 확인할 수 있다.<br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2f74af9a-a2b4-4c14-8c29-d1b3d30550bc" width="30%"/><br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3940fab0-1d44-4d1f-97b7-15b72e84fe7e" width="60%"/><br>


<br>

## Elastic Load Balancer
- Local balancer란 Backend System의 전면에 위치하면서 사용자 요청을 받아들이고 여러 instance에게 요청을 분배해주는 역할을 하는 컴포넌트이다.
- DNS를 지정하여 **domain name을 지정**할 수 있고 **HTTPS SSL Termination**을 대리수행할 수 있다.
- public에 위치하면서 Private Subnet에 위치한 Instance에게 요청을 전달 할 수 있다.
- AWS에는 CLB, NLB, ALB, GWLB등의 종류가 있으며 이글에서는 **ALB(Application Load Balancer)** 를 사용한다.

### Load Balancing Algorithm
- Round Robin: Instance 들에게 돌아가면서 요청을 할당한다.
  - 가중 Round Robin: 가중치에 따라 Instance에게 요청을 할당한다.
- Hash: 요청 내의 IP, Request Uri의 Hash 값을 구해서 Instance를 대응해 준다.
- Least Connection: 현재 연결이 가장 적은 Instance에 대응해 준다.
- Least Response Time: 응답 시간이 가장 적은것으로 통계적으로 판단되는 Instance에 대응해 준다.

### Health Check
- 특정 uri, port를 지정하여 Health Check를 하도록 설정하면 연결된 Instance들이 정상적으로 동작하는지를<br> 
설정된 요청지에서 200(OK)응답이 오는지 보고 확인할 수 있다.
- Health Check간격이나 몇번 연속해서 잘못된 응답이 올 경우에 문제가 있는 EC2로 판단할 것인지를 정할 수 있고<br> 
문제가 있는것으로 판단 된 경우 load balancer가 요청을 제공해주지 않는다.

### L4 vs L7 Load balancer
- L4 즉 Transport layer 혹은 L7 Application layer까지의 정보를 바탕으로 balancing을 수행한다.
- 여기서 생기는 가장 큰 차이점은 L7의 경우 Https 프로토콜에 의한 복호화 과정을 수행할 수 있고 또 해야만 한다는 점이다.
  - 이에 따른 장점은 SSLTermination을 수행할 수 있고 이후의 흐름에서 Http만으로 통신할 수 있도록 할 수 있다.
  - 또한, Http 요청 내용을 활용할 수 있기 때문에 이를 바탕으로 request uri, cookie등의 정보도 활용하여 Load Balancing을 진행할 수 있다.
  - 단점은 복호화라는 것 자체에서 overhead가 발생한다는 점이다.
  - L4의 경우 패킷을 복호화 하지않고 IP, Port, Mac Address 정보만으로도 충분히 활용 가능하기 때문에 복호화에 의한 overhead가 발생하지 않는다.
  - AWS에서 NLB(L4)의 Latency 는 ~100ms정도인 반면에 ALB(L7)의 Latency는 ~400ms로 알려져있다.

### AWS ELB 설정 과정
#### 설정 시 주요 결정 사항들
- L4 vs L7
  - L7인 Application Load Balancer를 활용하는 것이 적합하다고 판단하였다.
    - 이유는 NLB를 선택하여 Https 복호화를 수행하지 않아 성능 향상이 부분적으로 이루어 질 수 있다 하더라도 어차피<br> 
    이 이후의 서버단 어딘가에서 SSL Termination이 이루어져야 하기 때문이다.
    - 기존 배포의 경우 EC2내의 Reverse Proxy인 Nginx에서 이 역할을 수행하고 있었는데 ALB를 선택할 경우<br>
    Nginx가 이 역할을 수행하지 않아도 되기 때문에 여러 Instance에서 중복된 로직을 Application Load Balancer에서 응집해서 수행할 수 있다는 장점이 있다.

#### 설정 과정
- ALB 생성 선택
  - Internet Facing
  - Security Group 생성
    - Inbound Rule에서 프론트엔드 서버에 대해서만 허용한다.
    - SSH 접속은 Bastion Host를 통해 이루어지기 때문에 허용할 필요가 없다.
  - subnet의 경우 Public subnet을 둘 이상을 선택한다.
    - Subnet은 ALB가 위치할 곳을 의미하므로 꼭 **IGW가 Routing이 되어있는 Public Subnet으로 설정**해야한다.
    - Target Group의 Instance가 위치해 있는 Availability 존에 적어도 하나 존재해야 해당 Instance에 접근할 수 있다.

  - Listener-target group 생성
    - Listner의 프로토콜과 포트는 Load Balancer로 들어오는 요청을 의미하며 이 요청을 target group으로 어떻게 전달해줄지를 의미한다.
      - HTTPS 프로토콜을 사용할 것이기 때문에 HTTPS 443으로 선택한다.
      - Target Group을 선택하고 Action에서 Forward를 선택한다. 해당 Instance의 프로토콜은 SSL Termination을 이미진행했으므로 Http를 선택한다.
      - 추후에 ASG를 생성하여 target group으로 설정할 것이다.

#### DNS + HTTPS (AWS Certificate Manager + Route 53)
- Route 53을 통해 도메인을 발급받는다.(1년에 13달러 요금 발생)
- 생성된 Hosted Zone에서 Record를 생성하고 A record, Alias를 선택한 후 Load balancer를 선택하여 도메인 주소를 발급해준다.
- ACM에서 해당 도메인 주소에 SSL 인증서를 request하고 인증 과정을 거치면 HTTPS 구축까지 완료된다.


---
## Route53
# 도메인 연결하기 (Route 53)

## ✅ Route 53이란?
> 💡한 줄 요약 : 도메인을 발급하고 관리해주는 서비스이다.

혹시나 도메인(Domain)의 의미를 모르는 분들을 위해 설명드리자면,<br>
www.naver.com, daum.net, youtube.com과 같은 문자로 표현된 인터넷 주소를 의미한다.

Route 53을 조금 더 전문적인 용어로 표현하자면 **DNS(Domain Name System) 서비스**이다.

> DNS가 뭔지 알아보자.

## ✅ DNS(Domain Name System)란?
도메인(Domain)이 없던 시절에는 특정 컴퓨터와 통신하기 위해서 IP 주소(ex. 12.134.122.11)를 사용했다.<br>
이 **IP**는 **특정 컴퓨터를 가리키는 주소의 역할**을 한다.

하지만 IP 주소는 많은 숫자들로 이루어져 있어서 일일이 외우기가 너무 불편했다.<br>
이 때문에 사람들이 기억하기 쉬운 문자로 컴퓨터의 주소를 나타낼 수는 없을까에 대해 고민하게 됐다.<br>
숫자로 이루어져 있는 IP 주소를 문자로 구성하기에는 한계가 있었다.<br>
왜냐하면 컴퓨터가 처리하기 쉬운 값의 형태는 문자가 아니라 **숫자**이기 때문이다.

이를 해결하기 위해 **문자를 IP 주소로 변환해주는 하나의 시스템(서버)** 을 만들게 됐다. 이게 바로 **DNS(Domain Name System)** 이다.<br>
DNS가 생기고나서부터 사람들은 특정 컴퓨터와 통신하기 위해 복잡한 IP 주소를 일일이 외울 필요가 없게 됐다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2c85f97e-8715-40e8-80ed-3c388eb8546e" width="60%"/><br>

## ✅ 현업에서의 Route53 활용 여부
프론트 웹 페이지든 백엔드 서버든 일반적으로 IP를 기반으로 통신하지 않고 도메인을 기반으로 통신한다.<br>
이유는 여러가지지만 그 이유 중 하나는 **HTTPS 적용** 때문이다. **IP 주소에는 HTTPS 적용을 할 수가 없다**.<br>
도메인 주소가 있어야만 HTTPS 적용을 할 수 있다. 이 때문에 특정 서비스를 운영할 때 도메인은 필수적으로 사용하게된다.

하지만 DNS의 역할을 하는 서비스는 AWS Route 53 뿐만 아니라 다양하게 존재한다.<br>
가비아(gabia), 후이즈(whois) 등에서도 도메인을 구매하고 관리할 수 있다. 즉, DNS의 역할을 서비스를 하고 있다는 뜻이다.

현업에서는 무조건적으로 AWS Route 53을 고집하진 않는다. 왜냐면 각 서비스마다 구매할 수 있는 도메인의 종류가 다르기 때문이다.<br>
쉽게 예를 들어, 가비아(gabia)에는 내가 원하는 형태의 도메인이 있는데 AWS Route 53에는 없을 수도 있다.<br>
따라서 내가 원하는 도메인이 존재하는 곳의 DNS 서비스를 활용하자.

<br>

# [실습] 1. Route53에서 도메인 구매
## ✅ Route 53에서 도메인 구매
1.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/41ecdc06-cdaf-4ac0-86b8-d3613a9bf553" width="60%"/><br>
2.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3a9563ce-5e12-4480-b7a6-62d740e92b9d" width="60%"/><br>
3.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2bf2ca47-59f9-409b-b0fa-ac7c67cf6054" width="60%"/><br>
4.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/780afe11-a476-4223-86f2-072d7667a9e8" width="60%"/><br>

주의) 다른 건 몰라도 이메일은 정확히 입력해야 한다. 10~20분 있다가 등록한 이메일로 메일이 날라온다. 이 메일을 확인해야만 정상적으로 도메인이 등록된다.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ef63e230-f17a-4099-a4e8-80b700ea9a25" width="60%"/><br>

5.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/5826a8cb-6e2d-46f4-8785-4e767d0b60bb" width="60%"/><br>

## ✅ 도메인 구매 잘 됐는지 확인하기

[호스팅 영역]<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/540854da-6796-44b6-8c10-6d8fa8a77bca" width="60%"/><br>
[등록된 도메인]<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/f373115d-d596-4c9c-84f2-f9cefee56df7" width="60%"/><br>
위와 같이 등록된 도메인은 도메인을 구매한 시점으로부터 10~20분 뒤에 등록된다.

<br>

# [실습] 2. Route53의 도메인을 EC2에 연결하기
## ✅ Route53의 도메인을 EC2에 연결하기
1. Route 53의 `호스팅 영역` 메뉴에 들어가서 `레코드 생성` 버튼 누르기<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b98bb6e2-2f49-42a8-87d0-b6540c631942" width="60%"/><br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c8e3a2ad-af3c-4a16-bfb1-6c69500b4eab" width="60%"/><br>

2. 레코드 생성하기<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/603aec61-7f4b-42db-a6eb-03dcdf2b2fb5" width="60%"/><br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e6e92380-63cd-470d-8454-ed0966f478e1" width="60%"/><br>

위와 같이 설정할 경우 jscode-edu.link의 도메인으로 접속했을 때 52.79.34.240의 IP 주소로 연결시켜준다는 뜻이다.<br>
레코드 유형은 아래에서 자세히 설명하겠다.

만약 api.jscode-edu.link의 도메인으로 접속했을 때 52.79.34.240의 IP 주소로 연결시켜주고 싶다면 레코드 이름의 빈칸에 api를 적으면 된다.<br>
이와 같이 하나의 도메인만 구매하더라도 여러 개의 서브 도메인을 사용할 수 있다. (jscode-edu.link가 주 도메인이고, ___.jscode-edu.link 형태의 도메인이 서브 도메인이다.)

3. 잘 접속되는지 확인하기<br>
> A 레코드로 추가하고나서 도메인이 적용되는데 5~10분 정도 걸린다. 그러고 도메인에 접속해서 테스트해보자

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/93554917-cd2c-4644-9afe-ed1ee863fc28" width="60%"/><br>

## ✅ 레코드 유형
DNS에 공통적으로 있는 설정 중 하나가 레코드 유형이다. 많은 레코드 유형이 있지만 2가지(A 레코드, CNAME 레코드)만 확실히 알고 있으면 충분하다.<br>
[파레토의 법칙]<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/567703ec-c508-4644-90f5-621cc51bb044" width="60%"/><br>

1. **A 레코드**<br>
   도메인을 **특정 IPv4 주소에 연결시키고 싶을 때 사용**하는 레코드 유형이다.

2. **CNAME 레코드**<br>
   도메인을 **특정 도메인 주소에 연결시키고 싶을 때 사용**하는 레코드 유형이다.
   만약 CNAME 레코드의 값으로 `www.naver.com`을 적었다고 가정하자. 그러면 해당 도메인으로 접속했을 때, `www.naver.com`으로 연결되어 이동한다.

---
## ELB

# HTTPS 연결하기 (ELB)
## ELB란? / TLS, SSL과 HTTPS
### ✅ ELB(Elastic Load Balancer)란?
> 한 줄 요약 : 트래픽(부하)을 적절하게 분배해주는 장치이다.

트래픽(부하)를 적절하게 분배해주는 장치를 보고 전문적인 용어로 로드밸런서(Load Balancer)라고 부른다.<br>
서버를 2대 이상 가용할 때 ELB를 필수적으로 도입하게 된다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b2044505-6f5b-4f91-8f0b-eb949f3ef7b0" width="30%"/><br>

> 하지만 지금은 ELB의 로드밸런서 기능을 사용하지 않고, ELB의 부가 기능인 **SSL/TLS(HTTPS)를 적용시키는 방법**에 대해 배울 것이다.

### ✅ SSL/TLS란 ?
**SSL/TLS** 쉽게 표현하자면 **HTTP를 HTTPS로 바꿔주는 인증서**이다.
위에서 말했다시피 **ELB**는 **SSL/TLS 기능**을 제공한다고 했다.<br>
**SSL/TLS 인증서를 활용해 HTTP가 아닌 HTTPS로 통신할 수 있게 만들어준다.**

### ✅ HTTPS ?
> HTTPS를 적용시켜야 하는 이유는 무엇일까?
1. **보안적인 이유**<br>
   데이터를 서버와 주고 받을 때 **암호화**를 시켜서 통신을 한다. 암호화를 하지 않으면 누군가 중간에서 데이터를 가로채서 해킹할 수도 있다. 보안에 좋지 않다.
2. **사용자 이탈**<br>
   어떤 사이트에 들어갔는데 아래와 같이 보인다면 왠지 믿음직스럽지 못한 사이트라고 생각할 것이다.<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/635cd71b-c5ed-4880-b0eb-225107c1633c" width="40%"/><br>

### ✅ 현업에서는 ?
대부분의 웹 사이트에서 HTTPS를 적용시킨다.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ce72aa66-eaf6-46ab-a3a6-cf931dbbad09" width="40%"/><br>
HTTPS 인증을 받은 웹 사이트가 백엔드 서버와 통신하려면, 백엔드 서버의 주소도 HTTPS 인증을 받아야 한다.<br>
따라서 백엔드 서버와 통신할 때도 IP 주소로 통신하는 게 아니라, **HTTPS 인증을 받은 도메인 주소로 통신**을 한다.

주로 도메인을 구성할 때 아래와 같이 많이 구성한다.
- 웹 사이트 주소 : `**https**://jscode-edu.co.kr`
- 백엔드 API 서버 주소 : `**https**://api.jscode-edu.co.kr`

<br>

## ELB를 활용한 아키텍처 구성
### ✅ ELB를 활용한 아키텍처 구성
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/f4466d76-639d-4dbc-be70-2cc0fcf4b810" width="20%"/><br>
ELB 도입 전 아키텍처<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/77ffc65f-83d2-4090-b0c9-4e4adfb08f97" width="30%"/><img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/470f0ee1-e531-4c9a-9f9a-ba4157baacf3" width="30%"/><br>
ELB 도입 후 아키텍처<br>

> ELB를 사용하기 전의 아키텍처는 사용자들이 EC2의 IP 주소 또는 도메인 주소에 직접 요청을 보내는 구조였다.<br>
> 하지만 ELB를 추가적으로 도입함으로써 사용자들이 EC2에 직접적으로 요청을 보내지 않고 `ELB`를 향해 요청을 보내도록 구성할 것이다.<br>
> 그래서 EC2 달았던 **도메인도 ELB에** 달 것이고(ELB->EC2), **HTTPS도 ELB의 도메인에** 적용시킬 예정이다.

<br>

## [실습] 1. ELB 셋팅하기 - 기본 구성
### ✅ 1. 리전 선택하기
`AWS EC2` `로드밸런서` 서비스로 들어가서 리전(Region)을 선택해야 한다.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/9faa5eb7-7662-4af3-af19-2c40fbc3dd92" width="20%"/><img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/32ee4237-41fc-4d84-9623-184c36a84c4e" width="40%"/><br>

### ✅ 2. 로드 밸런서 유형 선택하기
**2.1. 로드 밸런서 생성하기**<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/bff145d7-7d90-469e-ae72-be5eddc66efc" width="60%"/><br>

**2.2. 로드 밸런서 유형 선택하기**
3가지 로드 밸런서 유형 중 Application Load Balancer(ALB)를 선택하면 된다.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ea91ef51-0b3d-43d7-b301-fe72a367b312" width="60%"/><br>
**참고)** **Application Load Balancer를 선택한 이유**를 간단하게 들자면 **HTTP, HTTPS에 대한 특징을 활용하기 위함**이다.<br>
Application Load Balancer, Network Load Balancer, Gateway Load Balancer의 차이를 아는 건 AWS 입문자 입장에서 크게 중요한 부분이 아니다.<br>
그러니 Application Load Balancer를 선택한 이유가 이해되지 않아도 넘어가도 괜찮다.

### ✅ 3. 기본 구성
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ef0441f7-d2a1-4405-898a-501928c9b511" width="60%"/><br>
- **인터넷 경계**와 **내부**라는 옵션이 있다. **내부** 옵션은 Private IP를 활용할 때 사용한다.<br>
  입문 강의에서는 VPC, Private IP에 대한 개념을 활용하지 않을 예정이라 **인터넷 경계** 옵션을 선택하면 된다.
- **IPv4**와 **듀얼 스택**이라는 옵션이 있다. IPv6을 사용하는 EC2 인스턴스가 없다면 **IPv4**를 선택하면 된다.<br>
  우리가 만든 EC2 인스턴스는 전부 IPv4로 이루어져 있다.
    - **참고) IPv4와 IPv6의 차이**
      IPv4 주소는 `121.13.0.5`와 같은 IP 주소를 의미한다. <br>
      그런데 IPv4 주소가 고갈될 것으로 예측하고 IPv6을 추가로 만들어낸다.<br>
      IPv6은 IPv4보다 훨씬 더 많은 주소값을 만들어낼 수 있게 구성했다. IPv6의 형태는 `2dfc:0:0:0:0217:cbff:fe8c:0`와 같다.

### ✅ 4. 네트워크 매핑
로드 밸런서가 어떤 **가용 영역**으로만 트래픽을 보낼 건지 제한하는 기능이다.<br>
아직 가용 영역에 대한 개념을 배우지 않았다. AWS 입문자한테는 별로 중요한 개념이 아니다.<br>
가용 영역에 제한을 두지 않고 모든 영역에 트래픽을 보내게 설정하자. 즉, **모든 가용 영역에 다 체크하자.**<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/67e0945c-82bc-4297-a512-d441d79528a8" width="60%"/><br>

<br>

## [실습] 2. ELB 셋팅하기 - 보안그룹
### ✅ 보안 그룹
#### `AWS EC2 보안 그룹`에서 보안 그룹 생성하기
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/dca1cb85-0ba1-4ea3-8561-8808594b8a0c" width="60%"/><br>
- ELB의 특성상 인바운드 규칙에 `80`(HTTP), `443`(HTTPS) 포트로 모든 IP에 대해 요청을 받을 수 있게 설정해야 한다.

#### ELB 만드는 창으로 돌아와서 보안 그룹 등록하기
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/9d6ba7f9-e4f1-470c-89e7-3ca534a72f4f" width="60%"/><br>


<br>

## [실습] 3. ELB 셋팅하기 - 리스너 및 라우팅 / 헬스 체크
### ✅ 1. 대상 그룹(Target Group) 설정하기
리스너 및 라우팅 설정은 ELB로 들어온 요청을 **어떤 EC2 인스턴스에 전달할 건지**를 설정하는 부분이다.

1. .<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/eda2299c-c446-4ca8-adb6-85fca9fab278" width="60%"/><br>
   ELB로 들어온 요청을 ‘어떤 곳’으로 전달해야 하는데, 여기서 **‘어떤 곳’** 을 대상 그룹(Target Group)이라고 표현한다.<br>
   즉, ELB로 들어온 요청을 어디로 보낼지 대상 그룹을 만들어야 한다.

2. 대상 유형 선택하기<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a5ff594f-430e-4611-9b90-387e063de4b3" width="60%"/><br>
   EC2에서 만든 특정 인스턴스로 트래픽을 전달할 것이기 때문에 인스턴스 옵션을 선택한다.

3. 프토토콜, IP 주소 유형, 프로토콜 버전 설정<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/62a054e6-86f9-48b1-9c62-6869b8c5be77" width="60%"/><br>

ELB가 사용자로부터 트래픽을 받아 대상 그룹에게 어떤 방식으로 전달할지 설정하는 부분이다. <br>
위 그림은 HTTP(HTTP1), 80번 포트, IPv4 주소로 통신을 한다는 걸 뜻한다. 이 방식이 흔하게 현업에서 많이 쓰이는 셋팅 방법이다.

4. 상태 검사 설정하기<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1e9cb141-b265-4323-bbe5-a05fb3fa7d68" width="60%"/><br>
> ELB의 부가 기능으로 상태 검사(= Health Check, 헬스 체크) 기능이 있다. 이 기능은 굉장히 중요한 기능 중 하나이므로 확실하게 짚고 넘어가자.<br>
>
> 실제 ELB로 들어온 요청을 대상 그룹에 있는 여러 EC2 인스턴스로 전달하는 역할을 가진다.<br>
> (@ELB를 활용한 아키텍처 구성) 그런데 만약 특정 EC2 인스턴스 내에 있는 서버가 예상치 못한 에러로 고장났다고 가정해보자.<br>
> 그럼 ELB 입장에서 고장난 서버한테 요청(트래픽)을 전달하는 게 비효율적인 행동이다.
>
> 이런 상황을 방지하기 위해 ELB는 주기적으로(기본 30초 간격) 대상 그룹에 속해있는 각각의 EC2 인스턴스에 요청을 보내본다.<br>
> 그 요청에 대한 200번대(HTTP Status Code) 응답이 잘 날라온다면 서버가 정상적으로 잘 작동되고 있다고 판단한다.<br>
> 만약 요청을 보냈는데 200번대의 응답이 날라오지 않는다면 서버가 고장났다고 판단해서, ELB가 고장났다고 판단한 EC2 인스턴스로는 요청(트래픽)을 보내지 않는다.
>
> 이러한 작동 과정을 통해 조금 더 효율적인 요청(트래픽)의 분배가 가능해진다.
>
> 위에서 설정한 값을 해석해보자면, 대상 그룹의 각각의 EC2 인스턴스에 `GET /health`(HTTP 프로토콜 활용)으로 요청을 보내게끔 설정한 것이다.<br>
> 정상적인 헬스 체크 기능을 위해 EC2 인스턴스에서 작동하고 있는 백엔드 서버에 Health Check용 API를 만들어야 한다. 뒤에서 곧 만들 예정이다.

5. 대상 등록하기<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/89f5bc08-8890-49a0-8b6a-08715f9db1c0" width="60%"/><br>

6. ELB 만드는 창으로 돌아와서 대상 그룹(Target Group) 등록하기<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/758161e0-a948-41a0-bfc9-1589eaeec461" width="60%"/><br>
   위 설정을 해석하자면 ELB에 HTTP를 활용해 80번 포트로 들어온 요청(트래픽)을 설정한 대상 그룹으로 전달하겠다는 의미이다.

7. 로드 밸런서 생성하기<br>
   나머지 옵션들은 그대로 두고 로드 밸런서를 생성하면 된다.<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/883f29ff-2f74-4cee-a9ee-760711f337a8" width="60%"/><br>


### ✅ 2. Health Check API 추가하기
위의 샘플 프로젝트처럼 ELB의 상태 검사(= Health Check, 헬스 체크)에 응답할 수 있는 API를 추가하자.<br>
그런 뒤에 EC2 인스턴스의 서버를 업데이트 시켜주자.

### ✅ 3. 로드밸런서 주소를 통해 서버 접속해보기
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/11cd78c0-4f10-41fe-aa98-54c3d6d286de" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/f9d597f0-0676-41d6-bd72-159e8eaa3490" width="60%"/><br>

<br>

## [실습] 4. ELB에 도메인 연결하기
### ✅ 1. Route 53에서 EC2에 연결되어 있던 레코드 삭제
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4c1b07dc-d32c-4a63-917b-72470437afbf" width="60%"/><br>
### ✅ 2. Route 53에서 ELB에 도메인 연결하기
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0defceff-2bc6-429e-9ed1-415c98bbc8c8" width="60%"/><br>

<br>

## [실습] 5. HTTPS 적용을 위해 인증서 발급받기
> HTTPS를 적용하기 위해서는 인증서를 발급받아야 한다.
### ✅ 1. AWS Certificate Manager 서비스로 들어가서 인증서 요청 버튼 누르기
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/551116d7-da26-4d82-8483-202e2e2d424c" width="60%"/><br>
### ✅ 2. 인증서 요청하기
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/128499cd-c06f-4841-84fd-3098ed7e0848" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/394b0560-3f7c-481b-9cee-2c862d8abdb9" width="60%"/><br>
### ✅ 3. 인증서 검증하기
내가 소유한 도메인이 맞는 지 검증하는 과정이다.<br>
**1.**<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/92411194-0adb-4979-9caf-105c8226ac90" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/8dd30cd8-2720-4e86-a7de-7fb89731a12f" width="60%"/><br>

**2. 검증 완료**<br>
3분 정도 기다렸다가 AWS Certificate Manager 창을 새로고침하면 아래와 같이 검증이 완료된다. (길게는 10분 정도 소요될 때도 있다.)<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2ec3e67f-8691-4710-8fe0-4209795d0919" width="60%"/><br>

<br>

## [실습] 6. ELB에 HTTPS 설정하기
### ✅ 1. ELB의 리스너 및 규칙 수정하기
1. **HTTPS에 대한 리스너 추가하기**<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/8e9a61f9-1abf-4b5d-91cf-3663e1e9e82f" width="60%"/><br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a45fb182-723d-4bf7-ad58-7ad78185d6d0" width="60%"/><br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/dbb479e1-e0e4-463d-83b6-768abf5eff58" width="60%"/><br>
> 위와 같이 설정하면 HTTPS가 한 5초 정도 있다가 바로 적용된다.

### ✅2. HTTPS가 잘 적용됐는 지 확인하기
구매한 도메인에 https를 붙여서 접속해보자.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/78a22afe-1b0f-4bbb-893b-7b68995c94e0" width="60%"/><br>

### ✅3. HTTP로 접속할 경우 HTTPS로 전환되도록 설정하기
아직까지 아쉬운 점은 http를 붙여서 접속할 경우 HTTPS를 사용하지 않고 접속이 가능하다는 점이다.<br>
http를 붙여서 접속하더라도 자동으로 HTTPS로 전환(Redirect)되도록 만들어보자.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c4e1718e-362a-4a6b-a988-c912df00b029" width="60%"/><br>

1. 기존 HTTP:80 리스너를 삭제하기<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/044024c0-a0eb-40e3-a04a-8feaa44f7216" width="60%"/><br>
2. 리스너 추가하기<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/bfc2062c-be3b-469d-b070-36989418836f" width="60%"/><br>

### ✅4. HTTP로 접속해도 HTTPS로 잘 전환되는 지 확인하기
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4160101c-941f-4172-a3fe-3c063f3b7436" width="60%"/><br>
