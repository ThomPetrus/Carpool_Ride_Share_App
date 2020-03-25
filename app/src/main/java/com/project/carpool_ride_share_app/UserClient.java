package com.project.carpool_ride_share_app;

import com.project.carpool_ride_share_app.models.User;


public class UserClient extends android.app.Application {

    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
