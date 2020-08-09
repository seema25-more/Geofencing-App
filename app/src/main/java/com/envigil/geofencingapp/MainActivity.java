package com.envigil.geofencingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.envigil.geofencingapp.service.LocationService;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import static com.envigil.geofencingapp.MainActivity2.user1;
import static com.envigil.geofencingapp.MainActivity2.userInfo;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private boolean mPermissionGranted;
    private static final String TAG = "MainActivity";
    public static final String CORSE_LOCATION = "Manifest.permission.ACCESS_COARSE_LOCATION";
    public static final String FINE_LOCATION = "Manifest.permission.ACCESS_FINE_LOCATION";
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private float DEFAULT_ZOOM = 20f;
    private float DEFAULT_RADIUS = 5;
    private String GEOFENCE_ID = "SELF_GEOFENCE";
    private GeofencingClient geofencingClient;
    GeofenceContextwrapper geofenceContextwrapper;
    GeoPoint currentLocation;
    PendingIntent pendingIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionCheck();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceContextwrapper = new GeofenceContextwrapper(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        userInfo.addSnapshotListener(MainActivity.this,new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error !=null){
                    return;
                }
                GeoPoint geoPoint=(GeoPoint) value.get("geoPoint");
                if(geoPoint!=null){
                    LatLng latLng=new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                    Toast.makeText(MainActivity.this,""+geoPoint, Toast.LENGTH_SHORT).show();
                    addGeofence(latLng,DEFAULT_RADIUS);
                }else {
                    Toast.makeText(MainActivity.this,"Null location", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    private void GPSAlertDialog(){
        AlertDialog dialog=new AlertDialog.Builder(getApplicationContext())
                .setMessage("Please enable GPS ")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent enableGps=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGps,1002);
                    }
                }).create();

    }

    private void addCircule(LatLng latLng){
        CircleOptions options=new CircleOptions();
        options.center(latLng);
        options.radius(DEFAULT_RADIUS);
        options.fillColor(Color.RED);
        mMap.addCircle(options);
    }
    private void saveUserLocation(){
        Intent locationService = new Intent(this, LocationService.class);
        this.startForegroundService(locationService);

    }
    /*private void markLocation(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng).title("current location"));
        addCircule(latLng);
        moveCamera(latLng, DEFAULT_ZOOM);
        addGeofence(latLng, DEFAULT_RADIUS);
    }*/

    private void addGeofence(final LatLng latLng, float radius) {
        if(pendingIntent!=null){
            geofencingClient.removeGeofences(pendingIntent);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        Geofence geofence = geofenceContextwrapper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER |
                Geofence.GEOFENCE_TRANSITION_DWELL);
        GeofencingRequest request = geofenceContextwrapper.getRequest(geofence);
        pendingIntent = geofenceContextwrapper.getPendingIntent();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        geofencingClient.addGeofences(request, pendingIntent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                System.out.println("Geo success");
                addCircule(latLng);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Geo fail");
            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            saveUserLocation();
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
                    mPermissionGranted = true;
                    initMap();
                    break;
                }
            }
        }
    }


}