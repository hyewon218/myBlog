# NginX

## Web Server와 WAS의 차이
이번 게시글에서는 정적 페이지(static pages)와 동적 페이지(dynamic pages)를 먼저 이해하고, 그다음 Web Server와 WAS의 차이에 대해 살펴보겠습니다.

1. 정적 페이지(static pages)와 동적 페이지(dynamic pages)
2. `Web Server`와 `WAS`의 차이

### 정적 페이지(static pages)와 동적 페이지(dynamic pages)
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/638b0a99-82fe-40e4-85a3-ae8606ba95aa" width="90%"/><br>

우리가 보는 웹페이지는 위의 이미지처럼 웹 서버는 주소(url)를 가지고 통신 규칙(http)에 맞게 요청하면, 알맞은 내용(html)을 응답받습니다.<br> 
그러나 이처럼 단순한 클라이언트(웹 브라우저)와 웹 서버로는 정적(static)인 페이지밖에 처리하지 못한다는 한계를 가집니다.<br> 
이러한 html의 태생적인 한계를 극복하기 위해 application을 활용한 것이 Web Application입니다.<br> 
따라서 정적인 html의 한계를 극복하고 동적인 페이지를 제공하고자 하는 목적, 더 나아가 보안 강화와 장애 극복을 가능하게 만드는 것이 `WAS`입니다.<br>
정적 페이지와 동적 페이지는 말 그대로 페이지가 바뀌느냐 바뀌지 않느냐의 차이입니다.

<br>

1) 현재 상황
- ec2 : 1개
- Spring boot Application
  WAS만 있고 웹서버는 없는 상태

2) 목표
- 웹서버를 설치하여 한 개의 WAS를 여러개 WAS로 분산하자!

3) 과정
- nginx(웹서버) 설치
- nginx코드


### 1. EC2 생성

#### 🔐 보안그룹 설정
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/48ecec39-535a-4dbb-9650-e75ae045a86b" width="90%"/><br>

### 2. NginX 설치
```shell
# root 권한 변경
$ sudo su -
# 저장소 업데이트
$ apt update
# Nginx 설치
$ apt install nginx
```

```shell
# 설치된 nginx 버전 확인
nginx -v 
````

```shell
service nginx status
```
설치가 끝났다면, 위 명령어를 이용하여 nginx service가 active (running) 인지를 확인하여 서버가 잘 구동되고 있는지 확인한다.<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e85889ca-d9e3-440e-9982-67e8782e3429" width="90%"/><br>

### 3. Nginx 실행
```shell
service nginx start
```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/fd6c46a7-5053-4da0-9e78-7509dbb5f427" width="60%"/><br>


### 4. nginx.conf에서 설정 진행
```shell
vi /etc/nginx/nginx.conf
```

#### nginx.conf 파일의 구성과 디렉티브
```shell
user www-data;
worker_processes auto;
pid /run/nginx.pid;
include /etc/nginx/modules-enabled/*.conf;

events {
        worker_connections 768;
        # multi_accept on;
}

http {

        ##
        # Basic Settings
        ##

        sendfile on;
        tcp_nopush on;
        types_hash_max_size 2048;
        # server_tokens off;

        # server_names_hash_bucket_size 64;
        # server_name_in_redirect off;

        include /etc/nginx/mime.types;
        default_type application/octet-stream;

        ##
        # SSL Settings
        ##

        ssl_protocols TLSv1 TLSv1.1 TLSv1.2 TLSv1.3; # Dropping SSLv3, ref: POODLE
        ssl_prefer_server_ciphers on;

        ##
        # Logging Settings
        ##

        access_log /var/log/nginx/access.log;
        error_log /var/log/nginx/error.log;

        ##
        # Gzip Settings
        ##

        gzip on;

        # gzip_vary on;
        # gzip_proxied any;
        # gzip_comp_level 6;
        # gzip_buffers 16 8k;
        # gzip_http_version 1.1;
        # gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

        ##
        # Virtual Host Configs
        ##

        include /etc/nginx/conf.d/*.conf;
        include /etc/nginx/sites-enabled/*;
}
```
이런 디렉티브들은 블록과 컨텐츠로 구성되어 있는데 Directive = Block + Countext과 같이 구성된다.

**디렉티브를 끝내는 방법**
- 세미콜론
- 중괄호 블록

**디렉티브**
최초 디렉티브는 다음과 같이 구성된다.
- user
- worker_processes
- error_log
- pid

얘들은 어떤 특정 블록이나 컨텍스트에 포함되지 않는 메인 컨텍스트라고 부른다.<br>
user이게 root로 지정되어있으면 워커 프로세서가 root로 동작하게 하고 사용자가 워커 프로세스를 악의적으로 사용할 수 있어서 보안상 위험하다.

`worker_process`<br>
워커 프로세스를 몇 개를 생성할 것인지 지정한다.

`worker_connections`<br>
이벤트 안에서 사용하는 지시어인데 동시에 접속을 얼마나 처리할 것인지 지정하는 값으로<br> 
worker_process가 4고 worker_connections가 1024면 4 * 1024로 4096의 커넥션을 처리할 수 있다.


**블록**<br>
메인 컨텍스트와 별개로 추가적인 디렉티브로는 아래와 같은 블록이 있다.
- events {}
- http {}

`Http` 블록<br>
http 블록은 웹 트래픽을 처리하는 디렉티프블을 담고 있으면서 Universal블록이라고도 한다.<br>
그리고 http 블록에서 사용되는 모든 디렉티브들은 nginx 문서에서 볼 수 있다.

`Server` 블록<br>
하나의 웹 사이트를 선언하는데 사용되고, 가상 호스팅의 개념이고 하나의 서버로 두 개를 동시에 운영하고 싶을 때 사용한다.

`location` 블록<br>
server 블록 안에서 나오면서 특정 url을 처리하는 방법을 정의한다.<br>
예를 들어서 http://localhost:80/login 과 http://localhost:80/join으로 접근을 다르게 하고싶을 때 사용되는 데 이는 로드밸런싱에서도 나오니 주의깊게 봐두자.

`events` 블록
주로 네트워크의 동작 방법과 관련된 설정값들을 갖는다.
보는 것과 같이 http안에 직접 만들어도 되지만, 아래를 보면 etc/nginx/sites-enabled를 include하여 가져오는 것을 볼 수 있다.


#### 리버스 프록시
/etc/nginx/site-available/default 의 server 항목을 다시 수정
```shell
vi /etc/nginx/site-available/default
```

```shell

# Default server configuration
#
server {
        listen 80 default_server;
        listen [::]:80 default_server;

        # SSL configuration
        #
        # listen 443 ssl default_server;
        # listen [::]:443 ssl default_server;
      
        root /var/www/html;

        # Add index.php to the list if you are using PHP
         index index.html index.htm index.nginx-debian.html;

        server_name _;

        location / {
                # First attempt to serve request as file, then
                # as directory, then fall back to displaying a 404.
                try_files $uri $uri/ =404;
        }

```
- listen 80 default_server : 80 번 포트로 들어오는 통신에 대해서 응답 (default)
- listen : 해당 포트로 들어오는 요청을 해당 server {} 블록의 내용에 맞게 처리하겠다는 것을 뜻한다.
- server_name : 호스트 이름을 지정한다. 가상 호스트가 있는 경우 해당 호스트명을 써넣으면 된다.<br>
만약 로컬에서 작업하고 있는 내용을 nginx를 통해 띄우려고 하는 경우에는 localhost라고 적으면 된다.
- error_page : 요청결과의 http 상태코드가 지정된 http 상태코드와 일치할 경우, 해당 url로 이동한다.<br> 
보통 403, 404, 502 등의 에러처리를 위해 사용한다.<br>
url 결과에 따라 이후에 나오는 location = /50x.html와 일치하면 /usr/share/nginx/html 경로에 존재하는 50x.html 파일을 보여준다.
- location / : 처음 요청이 들어왔을 때 ( server_name이 127.0.0.1인 경우 -> 127.0.0.1로 요청이 들어왔을 때 )<br> 
보여줄 페이지들이 속해있는 경로와 초기 페이지인 index를 지정해준다.<br> 
/ url로 접속했을 경우 index.html, index.htm로 정의된 파일을 보여준다.

```shell
# 추가
upstream backend {
  server {instance_1번의_ip}:8081 weight=100 max_fails=3 fail_timeout=3s;
  server {instance_2번의_ip}:8081 weight=100 max_fails=3 fail_timeout=3s;
  server {instance_3번의_ip}:8081 weight=100 max_fails=3 fail_timeout=3s;
}

server {
        listen 80 default_server;
        listen [::]:80 default_server;

        # SSL configuration
        #
        # listen 443 ssl default_server;
        # listen [::]:443 ssl default_server;
      
        root /var/www/html;

        # Add index.php to the list if you are using PHP
         index index.html index.htm index.nginx-debian.html;

        server_name _;

        location / {
                # First attempt to serve request as file, then
                # as directory, then fall back to displaying a 404.
                try_files $uri $uri/ =404;
                
                # 추가
                proxy_pass http://backend;
                proxy_http_version 1.1;
                proxy_set_header Upgrade $http_upgrade;
                proxy_set_header Connection 'upgrade';
                proxy_set_header Host $host;
                proxy_cache_bypass $http_upgrade;
        }

```
- upstream은 강의 상류를 의미합니다. 즉, 위에서 아래로 뿌려주는 것을 의미합니다. (반대로는 downstream이 있습니다.)
  > 즉, 여러군데로 뿌려주는 것을 upstream이라고 합니다.
  > 위에서는 해당 nginx가 여러 서버에 배분해주므로 upstream 서버라고 부를 수도 있습니다.
- proxy_pass: Nginx로 요청이 오면 service_url에 등록된 곳으로 전달합니다.
  - NGINX는 역방향 프록시 역할을 하며 backend 호스트 그룹을 사용하여 이 그룹의 설정에 따라 요청을 분산합니다. 
- proxy_set_header_XXX: 실제 요청 데이터를 header의 각 항목에 할당합니다.

### nginx 서비스를 다시 시작