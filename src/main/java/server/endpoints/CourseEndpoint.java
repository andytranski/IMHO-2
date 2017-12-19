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
    TokenController tokenController = new TokenController();
    QuizController quizController = new QuizController();
    Crypter crypter = new Crypter();


    @GET
    public Response loadCourses(@HeaderParam("authorization") String token) throws SQLException {
        System.out.println(token);
        token = new Gson().fromJson(token, String.class);
        String decryptedToken = crypter.decrypt(token);
        System.out.println(decryptedToken);

        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);

        if (currentUser.getCurrentUser() != null) {
            ArrayList<Course> courses = quizController.loadCourses();
            String loadedCourses = new Gson().toJson(courses);

            if (courses != null) {
                Globals.log.writeLog(this.getClass().getName(), this, "Courses loaded", 2);
                return Response.status(200).type("application/json").entity(crypter.encrypt(loadedCourses)).build();
            } else {
                Globals.log.writeLog(this.getClass().getName(), this, "Empty course array loaded", 2);
                return Response.status(204).type("text/plain").entity("No courses").build();
            }
        } else {
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - load course", 2);
            return Response.status(401).type("text/plain").entity("Unauthorized").build();
        }
    }
}





