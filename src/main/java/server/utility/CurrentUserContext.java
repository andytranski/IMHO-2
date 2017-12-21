package server.utility;

import server.models.User;

import java.sql.SQLException;

public class CurrentUserContext {
    private User currentUser;

    //Return current user
    public User getCurrentUser() {
        return currentUser;
    }
    //Set the information on user as current
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    //Check whether the user is admin or not
    public Boolean isAdmin() {
        if(this.currentUser.getType() == 1) {
            return true;
        } else {
            return false;
        }
    }
}
