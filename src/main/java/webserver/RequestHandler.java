package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.sql.SQLOutput;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

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
            String line, url;
            if ((line = br.readLine()) == null) {
                return;
            }
            log.debug("request line : {}", line);
            url = HttpRequestUtils.getUrl(line);
            if(url.equals("/user/create")){

            }
            byte[] body = mkBody(url);

            DataOutputStream dos = new DataOutputStream(out);
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private byte[] mkBody(String url) throws IOException {
        if (url.equals("/")) {
            return "Hello World".getBytes();
        }
        int index;
        if ((index = url.indexOf("?")) != -1) {
            String requestPath = url.substring(0, index);
            String params = url.substring(index + 1);
            Map<String, String> map = HttpRequestUtils.parseQueryString(params);
            User user = new User(map.get("userId"), map.get("password"), map.get("name"), map.get("email"));
            DataBase db = DataBase.getInstance();
            db.addUser(user);
            System.out.println(db.findAll());
            return Files.readAllBytes(new File("./webapp" + url).toPath());
        }
        return Files.readAllBytes(new File("./webapp" + url).toPath());
    }
}
