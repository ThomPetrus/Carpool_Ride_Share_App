package com.project.carpool_ride_share_app.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MarkerCluster implements ClusterItem {

    private LatLng position;
    private String title;
    private String snippet;
    private int picture;
    private User user;
    private Chatroom chatroom;

    public MarkerCluster() {

    }

    public MarkerCluster(LatLng pos, String title, String text, int picture, User user) {
        this.position = pos;
        this.title = title;
        this.snippet = text;
        this.picture = picture;
        this.user = user;
    }

    public MarkerCluster(LatLng pos, String title, String text, int picture, Chatroom chatroom) {
        this.position = pos;
        this.title = title;
        this.snippet = text;
        this.picture = picture;
        this.chatroom = chatroom;
    }

    public Chatroom getChatroom() {
        return chatroom;
    }

    public void setChatroom(Chatroom chatroom) {
        this.chatroom = chatroom;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng pos) {
        this.position = pos;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.snippet = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String text) {
        this.snippet = text;
    }

    public int getPicture() {
        return picture;
    }

    public void setPicture(int picture) {
        this.picture = picture;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
