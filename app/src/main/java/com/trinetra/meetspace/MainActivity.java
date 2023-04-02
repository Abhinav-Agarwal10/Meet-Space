package com.trinetra.meetspace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trinetra.meetspace.databinding.ActivityMainBinding;
import com.trinetra.meetspace.models.UserModel;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    long coins = 0;

    String[] permissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private int requestCode = 21;
    UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        FirebaseUser currentuser = auth.getCurrentUser();



        database.getReference().child("Users").child(currentuser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userModel = snapshot.getValue(UserModel.class);
                        Glide.with(MainActivity.this).load(userModel.getProfile())
                                .into(binding.profilepic);
                        coins = userModel.getCoins();
                        binding.coins.setText("You have: " + coins);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.rewardbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RewardActivity.class);
                startActivity(intent);
            }
        });

        binding.findbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPermissionGranted()) {
                    if (coins >= 5) {
                        coins = coins - 5;
                        database.getReference().child("Users")
                                .child(currentuser.getUid())
                                .child("coins")
                                .setValue(coins);
                        Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
                        intent.putExtra("profile", userModel.getProfile());
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Insufficient Coins.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    askPermission();
                }
            }
        });

    }

    void askPermission() {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private boolean isPermissionGranted() {
        for (String permission: permissions)
        {
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }

        return true;

    }
}