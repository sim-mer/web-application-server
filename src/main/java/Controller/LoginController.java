package Controller;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class LoginController extends AbstractController{
    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        DataBase db = DataBase.getInstance();
        User user = db.findUserById(request.getParam("userId"));
        if (user != null) {
            if (request.getParam("password").equals(user.getPassword())) {
                response.addHeader("Set-Cookie", "logined=true");
                response.sendRedirect("/index.html");
            } else {
                response.sendRedirect("/user/login_failed.html");
            }
        } else {
            response.sendRedirect("/user/login_failed.html");
        }
    }
}
