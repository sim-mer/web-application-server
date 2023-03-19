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
* get요청으로 데이터를 전달하면 http 헤더의 첫 번째 라인에 요청 url과 그 내용이 전송됨.
* 가상의 db에 저장하기 위해서 db class를 싱글톤 방식으로 설정하였음.

### 요구사항 3 - post 방식으로 회원가입
* post요청의 데이터 전달은 http 헤더의 첫 번째 라인에는 url만 전송되고 본문데이터는 http 헤더 이후 빈 공백을 가지는 한 줄 다음부터 시작됨.
* 본문데이터의 길이는 http헤더의 content-length에서 확인할 수 있음.

### 요구사항 4 - redirect 방식으로 이동
* http 302 리디렉션 응답코드는 요청한 리소스가 location에 저장된 url로 일시적으로 이동했음을 알리는 코드임.
* 그렇기 때문에 http 302코드는 header만 있으면 되기 때문에 body의 내용이 필요없음.

### 요구사항 5 - cookie
* http는 요청을 보내고 응답을 받으면 클라이언트와 서버간의 연결을 유지하지만 서로의 상태를 공유하지 않기때문에 무상태 프로토콜이라 함.
* 따라서 서버는 클라이언트가 한 행위를 기억하지 못함, 그렇기에 클라이언트의 행위를 기억하기 위한 목적으로 쿠키가 존재함.
* 클라이언트는 응답헤더에 set-cookie가 존재할 경우 값을 읽어 서버에 보내는 요청헤더의 쿠키에 저장해 다시 전송함.
* 쿠키는 path 속성이 존재하며 지정한 path에만 쿠키를 가지게 할 수 있고 쿠키를 생성할 때 path를 지정하지 않으면 쿠키를 생성했던 url범위, 즉 /user/login에서 생성했다면 /user의 path에 쿠키가 생성됨을 확인할 수 있음.
* 브라우저 특성상 서버를 재시작해도 쿠키가 계속 저장되어 있는 것으로 보아 실제 서비스에서는 쿠키의 유효시간을 정하는 등의 방법이 필요해보임.

### 요구사항 6 - stylesheet 적용
* http 요청에서 여러 형식의 파일을 전송하기 위해서는 반드시 http헤더의 Content-type을 설정해 주어야함.

### heroku 서버에 배포 후
* 