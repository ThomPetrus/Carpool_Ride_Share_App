package com.project.carpool_ride_share_app;

import com.project.carpool_ride_share_app.models.User;

/**
 *  UserClient - Used to store the information of the particular user.
 *  It extends Application, this way it is stored more or less as a session variable.
 *  Persistent as long as the application is. We don't have to retrieve user data everytime,
 *  this would be highly inefficient as the user data is stored on the database.
 *
 *  Note: UserClient must be defined in the manifest.
 */

public class UserClient extends android.app.Application {

    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
