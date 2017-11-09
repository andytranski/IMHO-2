package server.endpoints;

import com.google.gson.Gson;
import server.controller.MainController;
import server.controller.TokenController;
import server.controller.UserController;
import server.models.Result;
import server.utility.Crypter;
import server.utility.CurrentUserContext;
import server.utility.Globals;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

@Path("/result")
public class ResultEndpoint {
    UserController userController = new UserController();
    TokenController tokenController = new TokenController();

    Crypter crypter = new Crypter();


    @GET
    //send totale number of correct answer and questions back to the user.
    @Path("{QuizId}/{UserId}")
    public Response getUserScore(@HeaderParam("authorization") String token, @PathParam("QuizId") int QuizId, @PathParam("UserId") int UserId) throws SQLException {
        CurrentUserContext currentUser = tokenController.getUserFromTokens(token);

        if (currentUser.getCurrentUser() != null) {
                Result result = userController.getResult(QuizId, UserId);
                String loadedResult = new Gson().toJson(result);
               // loadedResult = crypter.encryptAndDecryptXor(loadedResult);

                if(result != null) {
                    Globals.log.writeLog(this.getClass().getName(), this, "Result loaded", 2);
                    return Response.status(200).type("application/json").entity(new Gson().toJson(loadedResult)).build();
                } else {
                    Globals.log.writeLog(this.getClass().getName(), this, "Empty result array loaded", 2);
                    return Response.status(204).type("text/plain").entity("No result").build();
                }
        } else {
            Globals.log.writeLog(this.getClass().getName(), this, "Unauthorized - get score", 2);
            return Response.status(401).type("text/plain").entity("Unauthorized").build();
        }
    }
}
