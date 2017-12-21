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
        //Format the user
        user = new Gson().fromJson(user, String.class);
        //Decrypt user
        String decryptedUser = crypter.decrypt(user);

        //Authorize user
        String authorizedToken = mainController.authUser(new Gson().fromJson(decryptedUser, User.class));
        //Format the token created to user
        String myToken = new Gson().toJson(authorizedToken);

        //If token has been created
        if (myToken != null) {
            //Return the token to user
            Globals.log.writeLog(this.getClass().getName(), this, "User authorized", 2);
            return Response.status(200).type("application/json").entity(crypter.encrypt(myToken)).build();
        } else {
            //Not signed in
            Globals.log.writeLog(this.getClass().getName(), this, "User not authorized", 2);
            return Response.status(401).type("text/plain").entity("Error signing in - unauthorized").build();
        }
    }

    @POST
    @Path("/signup")
    //Creating a new user
    public Response signUp(String user) {
        //Format user
        user = new Gson().fromJson(user, String.class);
        //Decrypt the data
        String decryptedUser = crypter.decrypt(user);

        //Create the user
        User createdUser = mainController.createUser(new Gson().fromJson(decryptedUser, User.class));
        //Format to JSON
        String newUser = new Gson().toJson(createdUser);

        //If user has been created
        if (createdUser != null) {
            //Return crypted user object
            Globals.log.writeLog(this.getClass().getName(), this, "User created", 2);
            return Response.status(200).type("application/json").entity(crypter.encrypt(newUser)).build();
        } else {
            //Return error
            Globals.log.writeLog(this.getClass().getName(), this, "Failed creating user", 2);
            return Response.status(400).type("text/plain").entity("Error creating user").build();
        }
    }


    @Path("/myuser")
    @GET
    //Getting own profile by token
    public Response getMyUser(@HeaderParam("authorization") String token) throws SQLException {
        //Format token
        token = new Gson().fromJson(token, String.class);
        //Find current user
        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);
        //Format to JSON
        String myUser = new Gson().toJson(currentUser);

        //If user is to be found
        if (currentUser.getCurrentUser() != null) {
            //return user
            Globals.log.writeLog(this.getClass().getName(), this, "My user loaded", 2);
            return Response.status(200).type("application/json").entity(crypter.encrypt(myUser)).build();
        } else {
            //Return error
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - my user", 2);
            return Response.status(400).type("text/plain").entity("Error loading user").build();
        }
    }

    @POST
    @Path("/logout")
    public Response logOut(String userId) throws  SQLException {
        //Format user id
        String myUserId = new Gson().fromJson(userId, String.class);
        //decrypt user id
        String decryptedId = crypter.decrypt(myUserId);

        //Delete token from db
        Boolean deletedToken = tokenController.deleteToken(new Gson().fromJson(decryptedId, Integer.class));
        if (deletedToken == true) {
            //Return text
            Globals.log.writeLog(this.getClass().getName(), this, "User log out", 2);
            return Response.status(200).entity("Logged out").build();
        } else {
            //Error
            Globals.log.writeLog(this.getClass().getName(), this, "User failed log out", 2);
            return Response.status(400).type("text/plain").entity("Error logging out").build();
        }


    }
}