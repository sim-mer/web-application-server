package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            String[] url;
            if ((line = br.readLine()) == null) {
                return;
            }
            log.debug("request line : {}", line);
            url = HttpRequestUtils.parseLine(line);
            //방식을 바꿔서 첫 라인만 parse를 하고 나머지는 헤더를 다 읽는걸 기본으로 하는 코드로?

            byte[] body = null;
            DataOutputStream dos = new DataOutputStream(out);
            if (url[0].equals("GET")) {
                body = requestGet(url[1]);
                if (url[1].equals("/index.html")) {
                    while (!(line = br.readLine()).equals("")) {
                        log.debug(line);
                    }
                }
            }
            if (url[0].equals("POST")) {
                int contentlength = 0;
                while (!(line = br.readLine()).equals("")) {
                    log.debug(line);
                    if (line.contains("Content-Length")) {
                        String[] a = line.split(" ");
                        contentlength = Integer.parseInt(a[1]);
                    }
                }
                requestPost(url[1], br, contentlength, dos);
                return;
            }

            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos) {
        String location = "/index.html";
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + location);
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302loginSuccess(DataOutputStream dos) {
        String location = "/index.html";
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            log.debug("set cookie");
            dos.writeBytes("Location: " + location);
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private byte[] requestGet(String url) throws IOException {
        if (url.equals("/")) {
            return "Hello World".getBytes();
        }
        return Files.readAllBytes(new File("./webapp" + url).toPath());
    }

    private void requestPost(String url, BufferedReader br, int contentLength, DataOutputStream dos) throws IOException {
        DataBase db = DataBase.getInstance();
        if (url.equals("/user/create")) {
            Map<String, String> map = createMap(br, contentLength);
            User user = new User(map.get("userId"), map.get("password"), map.get("name"), map.get("email"));
            db.addUser(user);
            log.debug("User : " + user);
            response302Header(dos);
            return;
        }
        if (url.equals("/user/login")) {
            Map<String, String> map = createMap(br, contentLength);
            User user = db.findUserById(map.get("userId"));
            if (user.getPassword().equals(map.get("password"))) {
                response302loginSuccess(dos);
            }
        }
    }

    private Map<String, String> createMap(BufferedReader br, int contentLength) throws IOException {
        String params = IOUtils.readData(br, contentLength);
        return HttpRequestUtils.parseQueryString(params);
    }

}
