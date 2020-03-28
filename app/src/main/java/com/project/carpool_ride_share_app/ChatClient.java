package com.project.carpool_ride_share_app;

import com.project.carpool_ride_share_app.models.Chatroom;
import com.project.carpool_ride_share_app.models.User;

/**
 *
 * Trying things ...
 *
 *  UserClient - Used to store the information of the particular user.
 *  It extends Application, this way it is stored more or less as a session variable.
 *  Persistent as long as the application is. We don't have to retrieve user data everytime,
 *  this would be highly inefficient as the user data is stored on the database.
 *
 *  Note: UserClient must be defined in the manifest.
 */

public class ChatClient extends android.app.Application {

    private Chatroom cr = null;

    public Chatroom getChatroom() {
        return cr;
    }

    public void setChatroom(Chatroom cr) {
        this.cr = cr;
    }

}
