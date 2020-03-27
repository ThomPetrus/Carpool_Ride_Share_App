package com.project.carpool_ride_share_app.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ChatroomLocation implements Parcelable {

    private GeoPoint geoPoint;
    private @ServerTimestamp
    Date timestamp;
    private Chatroom chatroom;


    public ChatroomLocation() {

    }

    public ChatroomLocation(GeoPoint geoPoint, Date timestamp, Chatroom chatroom) {
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
        this.chatroom = chatroom;
    }

    protected ChatroomLocation(Parcel in) {
        chatroom = in.readParcelable(User.class.getClassLoader());
    }

    public static final Parcelable.Creator<ChatroomLocation> CREATOR = new Parcelable.Creator<ChatroomLocation>() {
        @Override
        public ChatroomLocation createFromParcel(Parcel in) {
            return new ChatroomLocation(in);
        }

        @Override
        public ChatroomLocation[] newArray(int size) {
            return new ChatroomLocation[size];
        }
    };

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Chatroom getChatroom() {
        return chatroom;
    }

    public void setChatroom(Chatroom chatroom) {
        this.chatroom = chatroom;
    }

    @Override
    public String toString() {
        return "ChatroomLocation{" +
                "geoPoint=" + geoPoint +
                ", timestamp=" + timestamp +
                ", chatroom=" + chatroom +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(chatroom, flags);
    }
}
