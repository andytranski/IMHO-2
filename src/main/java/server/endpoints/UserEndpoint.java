package server.endpoints;


import com.google.gson.Gson;
import server.controller.MainController;
import server.controller.TokenController;
import server.models.User;
import server.utility.Crypter;
import server.utility.CurrentUserContext;
import server.utility.Globals;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

@Path("/user")
public class UserEndpoint {
    MainController mainController = new MainController();
    TokenController tokenController = new TokenController();

    Crypter crypter = new Crypter();

    @POST
    @Path("/login")
    //Endpoint for authorizing a user
    public Response logIn(String user) {
        user = new Gson().fromJson(user, String.class);
        String decryptedUser = crypter.decrypt(user);
        System.out.println(decryptedUser);

        String authorizedToken = mainController.authUser(new Gson().fromJson(decryptedUser, User.class));
        String myToken = new Gson().toJson(authorizedToken);
        System.out.println(myToken);

        if (myToken != null) {
            Globals.log.writeLog(this.getClass().getName(), this, "User authorized", 2);
            return Response.status(200).type("application/json").entity(crypter.encrypt(myToken)).build();
        } else {
            Globals.log.writeLog(this.getClass().getName(), this, "User not authorized", 2);
            return Response.status(401).type("text/plain").entity("Error signing in - unauthorized").build();
        }
    }

    @POST
    @Path("/signup")
    //Creating a new user
    public Response signUp(String user) {
        user = new Gson().fromJson(user, String.class);
        String decryptedUser = crypter.decrypt(user);

        User createdUser = mainController.createUser(new Gson().fromJson(decryptedUser, User.class));
        String newUser = new Gson().toJson(createdUser);

        if (createdUser != null) {
            Globals.log.writeLog(this.getClass().getName(), this, "User created", 2);
            return Response.status(200).type("application/json").entity(crypter.encrypt(newUser)).build();
        } else {
            Globals.log.writeLog(this.getClass().getName(), this, "Failed creating user", 2);
            return Response.status(400).type("text/plain").entity("Error creating user").build();
        }
    }


    @Path("/myuser")
    @GET
    //Getting own profile by token
    public Response getMyUser(@HeaderParam("authorization") String token) throws SQLException {
        token = new Gson().fromJson(token, String.class);
        System.out.println(token);

        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);
        String myUser = new Gson().toJson(currentUser);

        if (currentUser.getCurrentUser() != null) {
            Globals.log.writeLog(this.getClass().getName(), this, "My user loaded", 2);
            return Response.status(200).type("application/json").entity(crypter.encrypt(myUser)).build();
        } else {
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - my user", 2);
            return Response.status(400).type("text/plain").entity("Error loading user").build();
        }
    }

    @POST
    @Path("/logout")
    public Response logOut(String userId) throws SQLException {
        int myUserId = new Gson().fromJson(userId, Integer.class);
        Boolean deletedToken = tokenController.deleteToken(myUserId);

        if (deletedToken == true) {
            Globals.log.writeLog(this.getClass().getName(), this, "User log out", 2);
            return Response.status(200).entity("Logged out").build();
        } else {
            Globals.log.writeLog(this.getClass().getName(), this, "User failed log out", 2);
            return Response.status(400).type("text/plain").entity("Error logging out").build();
        }


    }
}