package server.endpoints;

import com.google.gson.Gson;
import server.controller.QuizController;
import server.controller.MainController;
import server.controller.TokenController;
import server.models.Question;
import server.utility.CurrentUserContext;
import server.utility.Crypter;
import server.utility.Globals;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;


@Path("/question")
public class QuestionEndpoint {
    //Instantiate controllers
    QuizController quizController = new QuizController();
    TokenController tokenController = new TokenController();
    //Instantiate crypter
    Crypter crypter = new Crypter();

    //GET method for loading questions bc. SQL statements is SELECT.
    @GET
    @Path("/{QuizId}")
    public Response loadQuestion(@HeaderParam("authorization") String token, @PathParam("QuizId") int quizId) throws SQLException {
       //Format the token
        token = new Gson().fromJson(token, String.class);
        //Find the current user with token
        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);

        //Check if user is validated
        if (currentUser.getCurrentUser() != null) {
            //Load questions
            ArrayList<Question> questions = quizController.loadQuestions(quizId);
            //Format to JSON
            String loadedQuestions = new Gson().toJson(questions);

            //Verify that array isn't empty
            if (questions != null) {
                //return loaded questions
                Globals.log.writeLog(this.getClass().getName(), this, "Questions loaded", 2);
                return Response.status(200).type("application/json").entity(crypter.encrypt(loadedQuestions)).build();
            } else {
                //Empty array
                Globals.log.writeLog(this.getClass().getName(), this, "Empty question array loaded", 2);
                return Response.status(204).type("text/plain").entity("No questions").build();
            }
        } else {
            //If user isn't validated
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - load questions", 2);
            return Response.status(401).type("text/plain").entity("Unauthorized").build();
        }
    }

    @POST
    //Method for creating a question
    public Response createQuestion(@HeaderParam("authorization") String token, String question) throws SQLException {
       //Get the current user
        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);

        //Check if user is admin
        if (currentUser.getCurrentUser() != null && currentUser.isAdmin()) {
           //Format the data from client
            question = new Gson().fromJson(question, String.class);
            //Decrypt data
            String decryptedQuestion = crypter.decrypt(question);
            //create question
            Question questionCreated = quizController.createQuestion(new Gson().fromJson(decryptedQuestion, Question.class));
            //Format to JSON
            String newQuestion = new Gson().toJson(questionCreated);

            //Verfi that question has been created
            if (questionCreated != null) {
                //Return the question
                Globals.log.writeLog(this.getClass().getName(), this, "Question created", 2);
                return Response.status(200).type("application/json").entity(crypter.encrypt(newQuestion)).build();
            } else {
                //Question nod created
                Globals.log.writeLog(this.getClass().getName(), this, "No input to new question", 2);
                return Response.status(400).type("text/plain").entity("Failed creating question").build();
            }
        } else {
            //User not validated
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - create question", 2);
            return Response.status(401).type("application/json").entity("Unauthorized").build();

        }
    }


}
