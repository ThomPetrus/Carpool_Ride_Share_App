package com.project.carpool_ride_share_app.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 *  Credit goes to CodingWithMitch. The Chat portion of his open source tutorial was used as a basis for the
 *  project.
 *
 *  Standard Java Object for each Chat Message
 *  Consisting of User details, message and the Firebase servertimestamp.
 */


public class Chatroom implements Parcelable {

    private String title;
    private String chatroom_id;
    private String avatar;
    private double latitude;
    private double longitude;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Chatroom(String title, String chatroom_id, String avatar, double latitude, double longitude) {
        this.title = title;
        this.chatroom_id = chatroom_id;
        this.avatar = avatar;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Chatroom() {

    }

    protected Chatroom(Parcel in) {
        title = in.readString();
        chatroom_id = in.readString();
        this.avatar = in.readString();
        this.latitude=in.readDouble();
        this.longitude=in.readDouble();
    }

    public static final Creator<Chatroom> CREATOR = new Creator<Chatroom>() {
        @Override
        public Chatroom createFromParcel(Parcel in) {
            return new Chatroom(in);
        }

        @Override
        public Chatroom[] newArray(int size) {
            return new Chatroom[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChatroom_id() {
        return chatroom_id;
    }

    public void setChatroom_id(String chatroom_id) {
        this.chatroom_id = chatroom_id;
    }

    @Override
    public String toString() {
        return "Chatroom{" +
                "title='" + title + '\'' +
                ", chatroom_id='" + chatroom_id + '\'' +
                ", avatar='" + avatar + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(chatroom_id);
    }
}
