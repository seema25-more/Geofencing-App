package com.envigil.geofencingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class GeofenceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"Entered in Geofence",Toast.LENGTH_SHORT).show();
        System.out.println("Entered in Geofence");
    }
}
