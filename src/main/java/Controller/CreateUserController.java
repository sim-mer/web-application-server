package Controller;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class CreateUserController extends AbstractController{
    @Override
    public void doPost(HttpRequest request, HttpResponse response){
        User user = new User(request.getParam("userId"), request.getParam("password"),
                request.getParam("name"), request.getParam("email"));
        DataBase db = DataBase.getInstance();
        db.addUser(user);
        response.sendRedirect("/index.html");
    }
}
