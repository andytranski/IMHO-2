package server.endpoints;

import com.google.gson.Gson;
import server.controller.QuizController;
import server.controller.TokenController;
import server.dbmanager.DbManager;
import server.models.Course;
import server.utility.CurrentUserContext;
import server.utility.Globals;
import server.utility.Crypter;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;

@Path("/course")
public class CourseEndpoint {
    //Instantiate controllers
    TokenController tokenController = new TokenController();
    QuizController quizController = new QuizController();
    //Instantiate crypter
    Crypter crypter = new Crypter();


    @GET
    public Response loadCourses(@HeaderParam("authorization") String token) throws SQLException {
        //Format the token
        token = new Gson().fromJson(token, String.class);
        //Find the current user with token
        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);

        //Check if a user is validated
        if (currentUser.getCurrentUser() != null) {
            //Load courses
            ArrayList<Course> courses = quizController.loadCourses();
            //Format to JSON
            String loadedCourses = new Gson().toJson(courses);

            //Verify that array isn't empty
            if (courses != null) {
                //Return the loaded courses
                Globals.log.writeLog(this.getClass().getName(), this, "Courses loaded", 2);
                return Response.status(200).type("application/json").entity(crypter.encrypt(loadedCourses)).build();
            } else {
                //Return error to client, if array is empty
                Globals.log.writeLog(this.getClass().getName(), this, "Empty course array loaded", 2);
                return Response.status(204).type("text/plain").entity("No courses").build();
            }
        } else {
            //If user isn't validated
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - load course", 2);
            return Response.status(401).type("text/plain").entity("Unauthorized").build();
        }
    }
}





