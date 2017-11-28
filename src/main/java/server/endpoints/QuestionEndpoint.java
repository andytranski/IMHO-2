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
    QuizController quizController = new QuizController();
    TokenController tokenController = new TokenController();
    Crypter crypter = new Crypter();

    //GET method for loading questions bc. SQL statements is SELECT.
    @GET
    @Path("/{QuizId}")
    public Response loadQuestion(@HeaderParam("authorization") String token, @PathParam("QuizId") int quizId) throws SQLException {
        token = new Gson().fromJson(token, String.class);

        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);

        if (currentUser.getCurrentUser() != null) {
            ArrayList<Question> questions = quizController.loadQuestions(quizId);
            String loadedQuestions = new Gson().toJson(questions);

            if (questions != null) {
                Globals.log.writeLog(this.getClass().getName(), this, "Questions loaded", 2);
                return Response.status(200).type("application/json").entity(crypter.encrypt(loadedQuestions)).build();
            } else {
                Globals.log.writeLog(this.getClass().getName(), this, "Empty question array loaded", 2);
                return Response.status(204).type("text/plain").entity("No questions").build();
            }
        } else {
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - load questions", 2);
            return Response.status(401).type("text/plain").entity("Unauthorized").build();
        }
    }

    @POST
    //Method for creating a question
    public Response createQuestion(@HeaderParam("authorization") String token, String question) throws SQLException {
        question = new Gson().fromJson(question, String.class);
        String decryptedQuestion = crypter.decrypt(question);

        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);

        if (currentUser.getCurrentUser() != null && currentUser.isAdmin()) {
            Question questionCreated = quizController.createQuestion(new Gson().fromJson(decryptedQuestion, Question.class));
            String newQuestion = new Gson().toJson(questionCreated);

            if (questionCreated != null) {
                Globals.log.writeLog(this.getClass().getName(), this, "Question created", 2);
                return Response.status(200).type("application/json").entity(crypter.encrypt(newQuestion)).build();
            } else {
                Globals.log.writeLog(this.getClass().getName(), this, "No input to new question", 2);
                return Response.status(400).type("text/plain").entity("Failed creating question").build();
            }
        } else {
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - create question", 2);
            return Response.status(401).type("application/json").entity("Unauthorized").build();

        }
    }


}
