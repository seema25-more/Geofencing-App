package com.envigil.geofencingapp;

import com.google.firebase.firestore.GeoPoint;

public class User {
    String user_name;
    GeoPoint geoPoint;

    public User(String user_name, GeoPoint geoPoint) {
        this.user_name = user_name;
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

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }
}
