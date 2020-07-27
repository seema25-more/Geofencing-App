package com.envigil.geofencingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private boolean mPermissionGranted;
    public static final String CORSE_LOCATION = "Manifest.permission.ACCESS_COARSE_LOCATION";
    public static final String FINE_LOCATION = "Manifest.permission.ACCESS_FINE_LOCATION";
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private float DEFAULT_ZOOM = 20f;
    private float DEFAULT_RADIUS = 5;
    private String GEOFENCE_ID = "SELF_GEOFENCE";
    private GeofencingClient geofencingClient;
    GeofenceContextwrapper geofenceContextwrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionCheck();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceContextwrapper = new GeofenceContextwrapper(this);
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getCurrentLocation() {
        if (mPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            //mark current location;
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    markLocation(latLng);
                }
            });

           /* Task task = fusedLocationClient.getLastLocation();
            task.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Location location = (Location) task.getResult();
                        LatLng latLng = new LatLng(location.getLongitude(), location.getLongitude());
                        moveCamera(latLng, DEFAULT_ZOOM);
                    } else {
                        Toast.makeText(getApplicationContext(), "unable to get location", Toast.LENGTH_SHORT).show();
                    }
                }
            });*/
        }
    }


    private void moveCamera(LatLng latLng, float default_zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, default_zoom));
    }
    private void addCircule(LatLng latLng){
        CircleOptions options=new CircleOptions();
        options.center(latLng);
        options.radius(DEFAULT_RADIUS);
        options.fillColor(Color.RED);
        mMap.addCircle(options);
    }

    private void markLocation(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng).title("current location"));
        addCircule(latLng);
        moveCamera(latLng, DEFAULT_ZOOM);
        addGeofence(latLng, DEFAULT_RADIUS);
    }

    private void addGeofence(LatLng latLng, float radius) {
        Geofence geofence = geofenceContextwrapper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER |
                Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_DWELL);
        GeofencingRequest request = geofenceContextwrapper.getRequest(geofence);
        PendingIntent pendingIntent = geofenceContextwrapper.getPendingIntent();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        geofencingClient.addGeofences(request, pendingIntent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                System.out.println("Geo success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Geo fail");
            }
        });
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mPermissionGranted) {
            getCurrentLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);

        }
    }

    private boolean permissionCheck() {
        String[] permission={Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
        if(ContextCompat.checkSelfPermission(getApplicationContext(),CORSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(getApplicationContext(), FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                mPermissionGranted=true;
               initMap();
                return true;
            }else {
                ActivityCompat.requestPermissions(this,permission,101);
                return false;
            }
        }else {
            ActivityCompat.requestPermissions(this,permission,101);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==101) {
            for (int i : grantResults) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission is granted", Toast.LENGTH_SHORT).show();
                    mPermissionGranted = true;
                    initMap();
                    break;
                }
            }
        }
    }


}