# myBlog
1. 수정, 삭제 API의 request를 어떤 방식으로 사용하셨나요? 

  • 수정 API의 request는  @RequestBody를 달아주어 HTTP Body 부분에 JSON 형식으로 넘어온 데이터(수정할 테이터, 비밀번호)를 
    글수정 메서드에 파라미터로 넣어 RequestDto로 변환해 처리하였습니다.
   
  • 삭제 API의 request는 @RequestBody를 달아주어 HTTP Body 부분에 JSON 형식으로 넘어온 비밀번호를
    글삭제 메서드의 파라미터로 넣어 RequestDto로 변환해 처리하였습니다. 
   
2. 어떤 상황에 어떤 방식의 request를 써야하나요? 

   • 서버에서 자원을 가져올 때 : Get
   
   • 서버에 자원을 새로 등록할 때 : Post
   
   • 서버의 자원을 요청에 들어 있는 자원으로 치환하고자 할 때 : Put
   
   • 서버의 데이터를 삭제하는 작업을 요청할 때 : Delete
   
3. RESTful한 API를 설계했나요? 어떤 부분이 그런가요? 어떤 부분이 그렇지 않나요? 

   API를 설계하는 부분이 어려워서 노션 참고하였습니다...
   
4. 적절한 관심사 분리를 적용하였나요? 네 (**Controller, Repository, Service**)

5. API 명세서 작성 가이드라인을 검색하여 직접 작성한 API 명세서와 비교해보세요! 
