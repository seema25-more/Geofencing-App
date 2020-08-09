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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener {
    EditText name,pass;
    Button save;
    String Sname,Spass;
    private static final String TAG = "MainActivity2";
    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static CollectionReference userRef=db.collection("UserList");
    public static DocumentReference userInfo;
    public static User user1=new User();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        name=findViewById(R.id.name);
        pass=findViewById(R.id.pass);
        save=findViewById(R.id.btn_save);
        save.setOnClickListener(this);
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View v) {
        Sname=name.getText().toString();
        Spass=pass.getText().toString();

//        Map<User, Object> user = new HashMap<>();
//        user.put(KEY_NAME, Sname);
//        user.put(KEY_PASS, Spass);


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
}