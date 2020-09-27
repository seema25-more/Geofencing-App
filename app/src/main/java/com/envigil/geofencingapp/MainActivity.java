package com.envigil.geofencingapp;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.SphericalUtil;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.envigil.geofencingapp.MainActivity2.Sname;
import static com.envigil.geofencingapp.MainActivity2.userRef;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private boolean mPermissionGranted;
    private static final String TAG = "MainActivity";
    public static final String CORSE_LOCATION = "Manifest.permission.ACCESS_COARSE_LOCATION";
    public static final String FINE_LOCATION = "Manifest.permission.ACCESS_FINE_LOCATION";
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private float DEFAULT_ZOOM = 20f;
    private float DEFAULT_RADIUS = 2;
    private String GEOFENCE_ID = "SELF_GEOFENCE";
    private LatLng latLng;
    private GeofencingClient geofencingClient;
    GeofenceContextwrapper geofenceContextwrapper;
    PendingIntent pendingIntent;
    CircleOptions circleOptions;
    ArrayList<User> users = new ArrayList<>();
    Intent locationService;
    LatLng loggedUsrlocation = null;
    static MarkerOptions markerOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionCheck();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceContextwrapper = new GeofenceContextwrapper(this);
        addGeofence();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //get logged user location
        userRef.whereEqualTo("user_name",Sname)
                .addSnapshotListener(MainActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                   if (error !=null) return;
                        for (QueryDocumentSnapshot snapshot : value) {
                            User user = snapshot.toObject(User.class);
                            GeoPoint geoPoint=user.getGeoPoint();
                            if(geoPoint==null) return;
                            Double latitude = new Double(geoPoint.getLatitude());
                            Double longtitude = new Double(geoPoint.getLongitude());
                            loggedUsrlocation=new LatLng(latitude,longtitude);
                        }
                    }
                });

        //get all user location
       userRef.addSnapshotListener(MainActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error !=null){
                    return;
                }
                users.clear();
                    if(mMap!=null){
                    for (QueryDocumentSnapshot snapshot : value) {
                        User user = snapshot.toObject(User.class);
                        users.add(user);
                    }
                    //addGeofence(users);
                    calculateDistance(users);
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
        /*userRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    User user = snapshot.toObject(User.class);
                    users.add(user);
                }
                addGeofence(users);
            }
        });*/
    }

    private void calculateDistance(ArrayList<User> users) {

        //Toast.makeText(getApplicationContext(), "Distance is:", Toast.LENGTH_SHORT).show();
        //mMap.clear();

        for(User user:users){
            addMarker(users);
            if(user.getUser_name().equals(Sname)) continue;
            LatLng userLocation;
            try{
                userLocation=new LatLng(user.getGeoPoint().getLatitude(), user.getGeoPoint().getLongitude());
                //addMarker(userLocation,user.getUser_name());
                // mMap.addMarker(getMarker().position(userLocation).title(user.getUser_name()));
                if(userLocation==null) continue;
                Double distance = SphericalUtil.computeDistanceBetween(loggedUsrlocation,userLocation);
                if(distance < 1.90){
                    alertUser();
                    Toast.makeText(getApplicationContext(), "Distance is:"+distance, Toast.LENGTH_SHORT).show();
                }
            }catch (NullPointerException e){
                //Toast.makeText(this, "No Location Found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void alertUser() {
        long[] pattern=new long[]{0,200,50,100,200};
        Vibrator vibrator= (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createWaveform(pattern,-1));
    }

    private void addMarker(ArrayList<User> users) {
        mMap.clear();
        for(User user:users) {
            try {
                LatLng marker = new LatLng(user.getGeoPoint().getLatitude(), user.getGeoPoint().getLongitude());
                mMap.addMarker(getMarker().position(marker).title(user.getUser_name()));
            } catch (NullPointerException e) {

            }
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void saveUserLocation() {
        locationService = new Intent(this, LocationService.class);
        this.startForegroundService(locationService);
    }

    private void addGeofence() {
        ArrayList<User> users=new ArrayList<>();
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
        if(users.size()>0) {
            for (final User user : users) {
                final GeoPoint geoPoint = user.getGeoPoint();
                if (geoPoint != null) {
                    final LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                    if (user.getUser_name().equals(Sname)) {
                        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                    }
                    mMap.addMarker(getMarker().position(latLng).title(user.getUser_name()));
                    //Geofence code
                    final Geofence geofence = geofenceContextwrapper.getGeofence(GEOFENCE_ID, latLng, DEFAULT_RADIUS, Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
                    GeofencingRequest request = geofenceContextwrapper.getRequest(geofence);
                    pendingIntent = geofenceContextwrapper.getPendingIntent();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    geofencingClient.addGeofences(request, pendingIntent).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            System.out.println("Geo success at:" + latLng.toString());
                            if (mMap != null) {
                                circleOptions = new CircleOptions();
                                circleOptions.center(latLng);
                                circleOptions.radius(DEFAULT_RADIUS);
                                circleOptions.fillColor(Color.RED);
                                mMap.addCircle(circleOptions);
                            }

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
        /*final LatLng latLng = new LatLng(17.681794,75.890877);
        Geofence geofence = geofenceContextwrapper.getGeofence(GEOFENCE_ID, latLng, DEFAULT_RADIUS, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceContextwrapper.getRequest(geofence);
        PendingIntent pendingIntent = geofenceContextwrapper.getPendingIntent();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence Added...");
                        mMap.addMarker(new MarkerOptions().position(latLng).title("test"));
                        circleOptions=new CircleOptions();
                        circleOptions.center(latLng);
                        circleOptions.radius(DEFAULT_RADIUS);
                        circleOptions.fillColor(Color.RED);
                        mMap.addCircle(circleOptions);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: ");
                    }
                });*/
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

    public static MarkerOptions getMarker(){
        if (markerOptions==null){
           return markerOptions=new MarkerOptions();
        }
        return markerOptions;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("Exit App");
        alertDialogBuilder.setMessage("Do you want to logout from app?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
           new MainActivity2().userLogout();
           finish();
        }
        });
        alertDialogBuilder.setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        AlertDialog alertDialog=alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.stopService(locationService);
        //this.unregisterReceiver(new GeofenceReceiver());
    }
}