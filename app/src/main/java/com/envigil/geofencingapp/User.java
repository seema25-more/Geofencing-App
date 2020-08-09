package com.envigil.geofencingapp;

import com.google.firebase.firestore.GeoPoint;

public class User {
    String user_name,password;
    GeoPoint geoPoint;

    public User(String user_name, String password, GeoPoint geoPoint) {
        this.user_name = user_name;
        this.password = password;
        this.geoPoint = geoPoint;
    }

    public User() {
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }
}
