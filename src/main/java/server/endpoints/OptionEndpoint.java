package server.endpoints;

import com.google.gson.Gson;
import server.controller.QuizController;
import server.controller.TokenController;
import server.models.Option;
import server.utility.CurrentUserContext;
import server.utility.Crypter;
import server.utility.Globals;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;

//Specifies path
@Path("/option")
public class OptionEndpoint {
    //Instantiate controllers
    QuizController quizController = new QuizController();
    TokenController tokenController = new TokenController();
    //Instantiate crypter
    Crypter crypter = new Crypter();


    @GET
    //Specifies path
    @Path("/{QuestionId}")
    public Response loadOptions(@HeaderParam("authorization") String token, @PathParam("QuestionId") int questionId) throws SQLException {
        //Format the token
        token = new Gson().fromJson(token, String.class);
        //Find the current user with token
        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);

        //Check if user is validated
        if (currentUser.getCurrentUser() != null) {
            //New arraylist of Option objects. Gives arraylist the value of the options loaded in loadOptions (dbmanager)
            ArrayList options = quizController.loadOptions(questionId);
            //Format to JSON
            String loadedOptions = new Gson().toJson(options);

            //Verify that array isn't empty
            if (options != null) {
                //Return loaded options
                Globals.log.writeLog(this.getClass().getName(), this, "Options loaded", 2);
                return Response.status(200).type("application/json").entity(crypter.encrypt(loadedOptions)).build();
            } else {
                //Empty array
                Globals.log.writeLog(this.getClass().getName(), this, "Empty options array loaded", 2);
                return Response.status(204).type("text/plain").entity("No options").build();
            }
        } else {
            //If user isn't validated
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - load options", 2);
            return Response.status(401).type("text/plain").entity("Unauthorized").build();
        }
    }

    @POST
    //Creating a new option for a quiz.
    public Response createOption(@HeaderParam("authorization") String token, String option) throws SQLException {
        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);

        if (currentUser.getCurrentUser() != null && currentUser.isAdmin()) {
            //Format data from client
            option = new Gson().fromJson(option, String.class);
            //Decrypt data
            String decryptedOption = crypter.decrypt(option);
            //Create option
            Option optionCreated = quizController.createOption(new Gson().fromJson(decryptedOption, Option.class));
           //Format to JSON
            String newOption = new Gson().toJson(optionCreated);

            //Verify that option is created
            if (optionCreated != null) {
                //Return the new option
                Globals.log.writeLog(this.getClass().getName(), this, "Option created", 2);
                return Response.status(200).type("application/json").entity(crypter.encrypt(newOption)).build();
            } else {
                //Option not created
                Globals.log.writeLog(this.getClass().getName(), this, "No input to new option", 2);
                return Response.status(500).type("text/plain").entity("Failed creating option").build();
            }
        } else {
            //User not validated
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - create option", 2);
            return Response.status(500).type("text/plain").entity("Unauthorized").build();
        }
    }

}
