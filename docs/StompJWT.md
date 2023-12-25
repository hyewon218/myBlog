### 1.회원가입/ 로그인 후 중고상품 거래 게시글 생성
![8EEC2BF8-86A1-41D4-8C4D-E9316B1D432E_1_105_c](https://github.com/JihyeChu/PetNexus/assets/126750615/d9ffa101-764a-4e0a-b3a5-5449cceea48e)

![68194C84-C91B-4875-ABE2-AB1487113CDD_4_5005_c](https://github.com/JihyeChu/PetNexus/assets/126750615/fe6f7680-1edc-4f0f-a280-39d405d4d29e)


### 2. 다른 아이디로 로그인 후 @PathVariable 을 통해 중고거래 상품 id를 받아와 해당 중고거래 채팅방을 생성합니다. (프론트에서 거래 게시글 내 채팅하기 버튼 누르면 생성?)
![71AE1E3E-036B-4DB3-A5D0-CACAE6EC6734_1_105_c](https://github.com/JihyeChu/PetNexus/assets/126750615/a843da2a-271d-445f-a0a8-9af86c56d93f)

![0EF68A06-B8E1-4A15-978D-327325356449_4_5005_c](https://github.com/JihyeChu/PetNexus/assets/126750615/d070e34f-aa7f-4f78-8e95-d16107c99d00)


 
 
### 3. apic에서는 apic의 문제점은 STOMP connect 할 땐 header에 token 넣지만, message를 send 할 땐 header에 값을 추가할 수 없다는 문제점이 있어 https://jxy.me/websocket-debug-tool/ 을 통해서 테스트를 하였습니다.

- **URL : endpoint 입력, STOMP connect header : 로그인 후 얻은 토큰값 입력 후 Connet 버튼 클릭**
  ![AF7433DC-3E2B-4300-AC7F-D2633EAC8E1D_4_5005_c](https://github.com/JihyeChu/PetNexus/assets/126750615/efbfb845-4f44-43bd-9ec1-5fc810dbb4ba)
- **StompHandler 클래스 - preSend 메소드에서 클라이언트가 CONNECT할 때 헤더로 보낸 Authorization에 담긴 jwt Token을 검증**
  
   StompHeaderAccessor.wrap으로 message를 감싸면 STOMP의 헤더에 직접 접근할 수 있습니다. 위에서 작성한 클라이언트에서 보낸 JWT가 들어있는 헤더 Authorization을StompHeaderAccessor.getNativeHeader("Authorization") 메서드를 통해 받아올 수 있고
  받아온 헤더의 값은 JWT가 됩니다.

- **StompWebSocketConfig 클래스 내 StompHandler를 인터셉터 등록(JWT 인증 기반 웹 소켓 사용이 가능)**
  ```@Override
    public void configureClientInboundChannel(ChannelRegistration registration) { // 핸들러 등록
        // connect / disconnect 인터셉터
        registration.interceptors(stompHandler);
    }
- **받은 JWT를 검증해 정상적으로 소켓을 사용할 수 있도록 동작합니다.**
  ![C90B3EAF-1B0D-454F-992F-FAEA32B8E118_1_105_c](https://github.com/JihyeChu/PetNexus/assets/126750615/c573d114-4673-4637-aefb-cd3e21106ce5)

- **STOMP subscribe destination / 판매자  그로인 후 토근값 입력 (받는쪽) : /sub/tradechat/{roomId} - @DestinationVariable 로 roomId 받아와  roomId 설정 → 입력 후 subscribe 클릭 → subscribe destination /sub/tradechat/1 success**
  ![C1F46BED-BFBE-4F8A-ABD1-F4575FF0A425_1_105_c](https://github.com/JihyeChu/PetNexus/assets/126750615/5fbfeac0-41fc-458c-b8dc-f9d14a0300da)

- **창 하나 더 띄워서 구매자 아이디로 로그인 후 토큰 값 입력 (보내는쪽) : STOMP send header → STOMP connect header 와 동일하게 입력, STOMP send destination → 어디로 보낼지? /pub/tradechat/message/{roomId} , Message Content → { "message":"아직 물건 안 팔렸나요????" } 입력 후 send 버튼 클릭**
  ![10C7B7B2-EF24-4059-B5A3-B1AD0B38AD30_1_105_c](https://github.com/JihyeChu/PetNexus/assets/126750615/4f6895c5-f4d9-41af-babd-524ff3c4bf9d)

- **roomId = 1 구독한 사람들(구매자, 판매자)에게 메세지가 간다.**
  ![77BB8E4F-A84A-4996-80B8-073B6E50E0BE_1_105_c](https://github.com/JihyeChu/PetNexus/assets/126750615/edfe19b6-56d9-4deb-b711-e5f0fdcad304)

- **판매자 답변(양방향 소통)**
  ![867A48FB-F433-4D99-84D0-5F4F4E461596_1_105_c](https://github.com/JihyeChu/PetNexus/assets/126750615/ecad259f-05e9-456b-ac9a-a701f64d467f)

  ![773CE77A-7E1B-4CA9-9C14-96087A6D5FD4](https://github.com/JihyeChu/PetNexus/assets/126750615/eb84983d-a6ad-47f2-902c-c285121bb19d)

---
## 📍오픈채팅방과 중고거래 채팅 테이블을 나눴습니다. 받는 정보가 달라서 헷갈림!!! (확인하시면 제가 erd 수정하겠습니다)
![28EB7A11-13E4-46DB-8468-FFD4749331D4](https://github.com/JihyeChu/PetNexus/assets/126750615/ba2417b5-69a3-4d23-88f0-8c38269c152d)


![06754008-47DA-45DB-8BDB-12F88B396920_1_105_c](https://github.com/JihyeChu/PetNexus/assets/126750615/3317c816-f57c-465b-b125-90a731ca4ca6)

