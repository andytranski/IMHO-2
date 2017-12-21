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
    QuizController quizController = new QuizController();
    TokenController tokenController = new TokenController();
    Crypter crypter = new Crypter();


    @GET
    //Specifies path
    @Path("/{QuestionId}")
    public Response loadOptions(@HeaderParam("authorization") String token, @PathParam("QuestionId") int questionId) throws SQLException {
        token = new Gson().fromJson(token, String.class);

        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);

        if (currentUser.getCurrentUser() != null) {
            //New arraylist of Option objects. Gives arraylist the value of the options loaded in loadOptions (dbmanager)
            ArrayList options = quizController.loadOptions(questionId);
            String loadedOptions = new Gson().toJson(options);

            if (options != null) {
                Globals.log.writeLog(this.getClass().getName(), this, "Options loaded", 2);
                return Response.status(200).type("application/json").entity(crypter.encrypt(loadedOptions)).build();
            } else {
                Globals.log.writeLog(this.getClass().getName(), this, "Empty options array loaded", 2);
                return Response.status(204).type("text/plain").entity("No options").build();
            }
        } else {
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - load options", 2);
            return Response.status(401).type("text/plain").entity("Unauthorized").build();
        }
    }

    @POST
    //Creating a new option for a quiz.
    public Response createOption(@HeaderParam("authorization") String token, String option) throws SQLException {
        option = new Gson().fromJson(option, String.class);
        String decryptedOption = crypter.decrypt(option);
        System.out.println(decryptedOption);

        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);

        if (currentUser.getCurrentUser() != null && currentUser.isAdmin()) {
            Option optionCreated = quizController.createOption(new Gson().fromJson(decryptedOption, Option.class));
            String newOption = new Gson().toJson(optionCreated);

            if (optionCreated != null) {
                Globals.log.writeLog(this.getClass().getName(), this, "Option created", 2);
                return Response.status(200).type("application/json").entity(crypter.encrypt(newOption)).build();
            } else {
                Globals.log.writeLog(this.getClass().getName(), this, "No input to new option", 2);
                return Response.status(500).type("text/plain").entity("Failed creating option").build();
            }
        } else {
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - create option", 2);
            return Response.status(500).type("text/plain").entity("Unauthorized").build();
        }
    }

}
