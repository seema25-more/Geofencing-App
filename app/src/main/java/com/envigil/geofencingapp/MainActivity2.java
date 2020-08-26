package com.envigil.geofencingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener {
    EditText name,pass;
    TextView info;
    Button save;
    public static String Sname,Spass;
    private static final String TAG = "MainActivity2";
    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static CollectionReference userRef=db.collection("UserList");
    public static DocumentReference userInfo;
    public static User user1=new User();
    private boolean mPermissionGranted;
    public static final String CORSE_LOCATION = "Manifest.permission.ACCESS_COARSE_LOCATION";
    public static final String FINE_LOCATION = "Manifest.permission.ACCESS_FINE_LOCATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        permissionCheck();
        name=findViewById(R.id.name);
        pass=findViewById(R.id.pass);
        info=findViewById(R.id.info);
        save=findViewById(R.id.btn_save);
        save.setOnClickListener(this);
        /*if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},101);
        }*/
    }



    @Override
    public void onClick(View v) {
        Sname=name.getText().toString();
        Spass=pass.getText().toString();
        user1.setUser_name(Sname);
        user1.setPassword(Spass);

        // Add a new document with a generated ID
        userInfo=userRef.document(Sname);
        userInfo.set(user1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity2.this, "Success", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity2.this,MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity2.this, "Fail", Toast.LENGTH_SHORT).show();
                        Log.d(TAG,e.toString());
                    }
                });

    }

    public void saveLocation(View view) {
      user1.setGeoPoint(new GeoPoint(24.2,34.2));
        userInfo.set(user1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity2.this, "Success", Toast.LENGTH_SHORT).show();
                        /*startActivity(new Intent(MainActivity2.this,MainActivity.class));
                        finish();*/
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity2.this, "Fail", Toast.LENGTH_SHORT).show();
                        Log.d(TAG,e.toString());
                    }
                });
    }

    public void loadUser(View view) {
        userRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                String Sinfo="";
                for(QueryDocumentSnapshot snapshot:queryDocumentSnapshots){
                    User user=snapshot.toObject(User.class);
                    Sinfo +=user.getUser_name()+"*"+user.getPassword()+"*"+user.getGeoPoint()+"*";
                    info.setText(Sinfo);
                }

            }
        });
    }

    private boolean permissionCheck() {
        String[] permission={Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET};
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET)==PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(getApplicationContext(),CORSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(getApplicationContext(), FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                    return true;
                }else {
                    ActivityCompat.requestPermissions(this,permission,101);
                    return false;
                }
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

                    break;
                }
            }
        }
    }

}