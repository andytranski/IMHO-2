package server.endpoints;

import com.google.gson.Gson;
import server.controller.QuizController;
import server.controller.MainController;
import server.controller.TokenController;
import server.dbmanager.DbManager;
import server.models.Quiz;
import server.utility.Crypter;
import server.utility.CurrentUserContext;
import server.utility.Globals;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;

@Path("/quiz")
public class QuizEndpoint {
    //Instantiate controllers
    QuizController quizController = new QuizController();
    TokenController tokenController = new TokenController();
    //Instantiate crypter
    Crypter crypter = new Crypter();


    @GET
    @Path("/{CourseID}")
    public Response loadQuizzes(@HeaderParam("authorization") String token, @PathParam("CourseID") int courseId) throws SQLException {
        //Format the token
        token = new Gson().fromJson(token, String.class);
        //Find the current user with token
        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);

        //Check if user is validated
        if (currentUser.getCurrentUser() != null) {
            //Load array with quiz
            ArrayList<Quiz> quizzes = quizController.loadQuizzes(courseId);
            //Format to JSON
            String loadedQuizzes = new Gson().toJson(quizzes);

            //Verify that quiz isn't empty
            if (quizzes != null) {
                //Return array
                Globals.log.writeLog(this.getClass().getName(), this, "Quizzes loaded", 2);
                return Response.status(200).type("application/json").entity(crypter.encrypt(loadedQuizzes)).build();
            } else {
                //Empty array
                Globals.log.writeLog(this.getClass().getName(), this, "Empty quiz array loaded", 2);
                return Response.status(204).type("text/plain").entity("No quizzes").build();
            }
        } else {
            //User not validated
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - load quiz", 2);
            return Response.status(401).type("text/plain").entity("Unauthorized").build();
        }
    }

    @POST
    // Method for creating a quiz
    public Response createQuiz(@HeaderParam("authorization") String token, String quiz) throws SQLException {
        //Get current user
        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);

        //Verify that user is admin
        if (currentUser.getCurrentUser() != null && currentUser.isAdmin()) {
            //Format data
            quiz = new Gson().fromJson(quiz, String.class);
            //Decrypt data
            String decryptedQuiz = crypter.decrypt(quiz);
            //Create quiz
            Quiz quizCreated = quizController.createQuiz(new Gson().fromJson(decryptedQuiz, Quiz.class));
            //Format to JSON
            String newQuiz = new Gson().toJson(quizCreated);

            //Verify that quiz has been created
            if (quizCreated != null) {
                //Return quiz
                Globals.log.writeLog(this.getClass().getName(), this, "Quiz created", 2);
                return Response.status(200).type("application/json").entity(crypter.encrypt(newQuiz)).build();
            } else {
                //No quiz created
                Globals.log.writeLog(this.getClass().getName(), this, "No input to new quiz", 2);
                return Response.status(400).type("text/plain").entity("Failed creating quiz").build();
            }
        } else {
            //User not validated
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - create quiz", 2);
            return Response.status(401).type("text/plain").entity("Unauthorized").build();
        }
    }

    @POST
    @Path("/{QuizId}")
    public Response updateQuestionCount(@HeaderParam("authorization") String token, @PathParam("QuizId") int quizId, String questionCount) throws SQLException {
        //Get current user
        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);

        //Verify that user is admin
        if (currentUser.getCurrentUser() != null && currentUser.isAdmin()) {
            //Format data
            String newQuestionCount = new Gson().fromJson(questionCount, String.class);
            //Decrypt data
            String decryptedQuestionCount = crypter.decrypt(newQuestionCount);

            //True or false if count has been updated
            Boolean updatedCount = quizController.updateQuestionCount(quizId, new Gson().fromJson(decryptedQuestionCount, Integer.class));

            //If question count has been updated
            if (updatedCount == true) {
                //Return text
                Globals.log.writeLog(this.getClass().getName(), this, "Quiz updated", 2);
                return Response.status(200).type("text/plain").entity("Updated quiz").build();
            } else {
                //Failed updating
                Globals.log.writeLog(this.getClass().getName(), this, "Failed updating", 2);
                return Response.status(400).type("text/plain").entity("Failed updating quiz").build();
            }
        } else {
            //Not validated
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - update quiz", 2);
            return Response.status(401).type("text/plain").entity("Unauthorized").build();
        }
    }


    @DELETE
    @Path("{deleteId}")
    // Method for deleting a quiz and all it's sub-tables
    public Response deleteQuiz(@HeaderParam("authorization") String token, @PathParam("deleteId") int quizId) throws SQLException {
        //Format token
        token = new Gson().fromJson(token, String.class);
        //Find current user
        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);

        //Verfiy that user is admin
        if (currentUser.getCurrentUser() != null && currentUser.isAdmin()) {
            //Boolean to delete quiz
            Boolean quizDeleted = quizController.deleteQuiz(quizId);

            //If quiz is deleted
            if (quizDeleted == true) {
                //Return text
                Globals.log.writeLog(this.getClass().getName(), this, "Quiz deleted", 2);
                return Response.status(200).type("text/plain").entity(crypter.encrypt("Quiz deleted")).build();
            } else {
                //Quiz not deleted
                Globals.log.writeLog(this.getClass().getName(), this, "Delete quiz attempt failed", 2);
                return Response.status(400).type("text/plain").entity("Error deleting quiz").build();
            }
        } else {
            //User not validated
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - delete quiz", 2);
            return Response.status(401).type("text/plain").entity("Unauthorized").build();
        }
    }

}
