package com.envigil.geofencingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import com.envigil.geofencingapp.service.LocationService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import static com.envigil.geofencingapp.MainActivity2.userRef;

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
    PendingIntent pendingIntent;
    Circle circle;
    CircleOptions circleOptions;
    ArrayList<User> users = new ArrayList<>();
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
        userRef.addSnapshotListener(MainActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error !=null){
                    return;
                }
                //Toast.makeText(MainActivity.this, "userRef Toast", Toast.LENGTH_SHORT).show();

                users.clear();
                if(mMap!=null) {
                    for (QueryDocumentSnapshot snapshot : value) {
                        User user = snapshot.toObject(User.class);
                        users.add(user);
                        System.out.println("UserLocation:" + user.getGeoPoint());
                    }
                    addGeofence(users);
                }
            }
        });
        /*userInfo.addSnapshotListener(MainActivity.this,new EventListener<DocumentSnapshot>() {
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
        });*/
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void addCircule(LatLng latLng){
        circleOptions=new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(DEFAULT_RADIUS);
        circleOptions.fillColor(Color.RED);
        circle=mMap.addCircle(circleOptions);
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

    private void addGeofence(ArrayList<User> users) {
        if(pendingIntent!=null){
            geofencingClient.removeGeofences(pendingIntent).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    System.out.println("Geo remove success");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println("Geo remove fail");
                }
            });
        }
        mMap.clear();
        for(User user:users) {

            final GeoPoint geoPoint = user.getGeoPoint();
            if (geoPoint != null) {
                final LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                mMap.addMarker(new MarkerOptions().position(latLng).title(user.getUser_name()));
                final Geofence geofence = geofenceContextwrapper.getGeofence(GEOFENCE_ID, latLng, DEFAULT_RADIUS, Geofence.GEOFENCE_TRANSITION_ENTER |
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
                        //addCircule(latLng);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Geo fail");
                    }
                });
            }
        }
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