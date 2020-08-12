package com.envigil.geofencingapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

public class GeofenceContextwrapper extends ContextWrapper {
    PendingIntent pendingIntent;

    public GeofenceContextwrapper(Context base) {
        super(base);
    }

    public GeofencingRequest getRequest(Geofence geofence){
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    public Geofence getGeofence(String id, LatLng latLng,float radius,int transition){
        return new Geofence.Builder()
                .setCircularRegion(latLng.latitude,latLng.longitude,radius)
                .setRequestId(id)
                .setTransitionTypes(transition)
                .setLoiteringDelay(5000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    public PendingIntent getPendingIntent(){
        if(pendingIntent !=null){
            return pendingIntent;
        }
        Intent intent=new Intent(this,GeofenceReceiver.class);
        pendingIntent=PendingIntent.getBroadcast(this,101,intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return  pendingIntent;
    }
}



















