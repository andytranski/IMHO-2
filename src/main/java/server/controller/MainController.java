package server.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import server.dbmanager.DbManager;
import server.models.User;
import server.utility.CurrentUserContext;
import server.utility.Digester;
import server.utility.Globals;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Date;


public class MainController {
    private Digester digester;


    //The constructor for instantiation
    public MainController() {
        digester = new Digester();

    }

    // Logic behind authorizing user
    public String authUser(User user) {
        DbManager dbManager = new DbManager();
        String token = null;
        //Use username to get the time created
        User foundUser = dbManager.getTimeCreatedByUsername(user.getUsername());

        if(foundUser != null) {
            //Add the time created to the password and hash
            user.setPassword(digester.hashWithSalt(user.getPassword() + foundUser.getTimeCreated()));
            //Authorize user
            User authorizedUser = dbManager.authorizeUser(user.getUsername(), user.getPassword());

            //Generate token at login
            try {
                Algorithm algorithm = Algorithm.HMAC256("Secret");
                long timeValue = (System.currentTimeMillis() * 1000) + 20000205238L;
                Date expDate = new Date(timeValue);

                token = JWT.create().withClaim("User", authorizedUser.getUsername()).withExpiresAt(expDate).withIssuer("IMHO").sign(algorithm);
                //Add token to database
                dbManager.createToken(token, authorizedUser.getUserId());

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (token != null) {
                return token;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    //Logic behind creating user.
    public User createUser(User user) {
        DbManager dbManager = new DbManager();
        //Add the time created to user
        long unixTime = (long) Math.floor(System.currentTimeMillis() / 10000);
        user.setTimeCreated(unixTime);
        //Add the time created to the password and hash
        user.setPassword(digester.hashWithSalt(user.getPassword()+user.getTimeCreated()));

        return dbManager.createUser(user);
    }



}



