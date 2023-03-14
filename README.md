# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답
* WebServer class에서 ServerSocket클래스를 생성하여 RequestHandler class에 인자로 넘겨줌.
* RequestHandler class에서는 소켓의 inputstream과 outputstream을 이용하여 클라이언트와 통신.
* 따라서 http 방식으로 전달된 요청을 inputstream을 통해 읽어서 index.html로 반환하는 코드가 필요.
* /index.html로 접속할 경우 자동으로 inputstream에 httpheader가 전달됨. 
* 사이트에 접속할 경우 스레드 하나에 모든 데이터가 클라이언트로 전송되는 것이 아니라 여러 번의 요청으로 스레드가 생성되어 데이터가 전송된다.

### 요구사항 2 - get 방식으로 회원가입
* RequestHandler class의 response200 method가 http 1.1 방식의 header를 생성해서 데이터를 전달함.
* http 1.1방식이므로 string의 형태로 아스키 코드로 전송하였지만 2.0이상의 방식으로 바뀔 경우 다른 함수로 헤더를 추가하여야 할듯함.
* get요청은 http 헤더의 첫 번째 라인에 요청 url이 전송됨.
* 가상의 db에 저장하기 위해서 db class를 싱글톤 방식으로 설정하였음.

### 요구사항 3 - post 방식으로 회원가입
* 

### 요구사항 4 - redirect 방식으로 이동
* 

### 요구사항 5 - cookie
* 

### 요구사항 6 - stylesheet 적용
* 

### heroku 서버에 배포 후
* 