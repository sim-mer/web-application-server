package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.sql.SQLOutput;
import java.util.*;

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

            byte[] body = null;
            DataOutputStream dos = new DataOutputStream(out);
            if (url[0].equals("GET")) {
                log.debug(url[1]);
                body = requestGet(url[1], br);
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

            if (url[1].contains("css")) {
                response200cssHeader(dos, body.length);
            } else {
                response200Header(dos, body.length);
            }
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

    private void response200cssHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
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
            dos.writeBytes("Location: " + location);
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private void response302loginFail(DataOutputStream dos) {
        String location = "/index.html";
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=false \r\n");
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

    private byte[] requestGet(String url, BufferedReader br) throws IOException {
        if (url.equals("/")) {
            return "Hello World".getBytes();
        }
        if (url.equals("/user/list")) {
            String line;
            while (!(line = br.readLine()).equals("")) {
                log.debug(line);
                if (line.contains("Cookie:")) {
                    String[] tokens = line.split(" ");
                    Map<String, String> map = HttpRequestUtils.parseCookies(tokens[1]);
                    Boolean logined = Boolean.parseBoolean(map.get("logined"));
                    if (logined) {
                        DataBase db = DataBase.getInstance();
                        Collection<User> userList = db.findAll();
                        StringBuilder sb = new StringBuilder();
                        sb.append("<table border='1'>");
                        for (User user : userList) {
                            sb.append("<tr>");
                            sb.append("<td>" + user.getUserId() + "</td>");
                            sb.append("<td>" + user.getName() + "</td>");
                            sb.append("<td>" + user.getEmail() + "</td>");
                            sb.append("</tr>");
                        }
                        sb.append("</table>");
                        return sb.toString().getBytes();
                    } else {
                        Files.readAllBytes(new File("./webapp/user/login.html").toPath());
                    }
                }
            }
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
            } else {
                response302loginFail(dos);
            }
        }
    }

    private Map<String, String> createMap(BufferedReader br, int contentLength) throws IOException {
        String params = IOUtils.readData(br, contentLength);
        return HttpRequestUtils.parseQueryString(params);
    }

}
