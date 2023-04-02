package com.trinetra.meetspace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trinetra.meetspace.databinding.ActivityConnectBinding;

import java.util.HashMap;

public class ConnectActivity extends AppCompatActivity {

    ActivityConnectBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;

    boolean isokay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        String profile = getIntent().getStringExtra("profile");
        Glide.with(this).load(profile).into(binding.profileimg);

        String username = auth.getUid();

        database.getReference().child("connect")
                .orderByChild("status")
                .equalTo(0).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getChildrenCount() > 0){
                            isokay = true;
                            for(DataSnapshot childsnap: snapshot.getChildren()){
                                database.getReference().child("connect")
                                        .child(childsnap.getKey())
                                        .child("incoming")
                                        .setValue(username);

                                database.getReference().child("connect")
                                        .child(childsnap.getKey())
                                        .child("status")
                                        .setValue(1);

                                Intent intent = new Intent(ConnectActivity.this, CallActivity.class);
                                intent.putExtra("username", username);
                                intent.putExtra("incoming", childsnap.child("incoming").getValue(String.class));
                                intent.putExtra("createdby", childsnap.child("createdby").getValue(String.class));
                                intent.putExtra("isAvailable", childsnap.child("isAvailable").getValue(boolean.class));
                                startActivity(intent);
                                finish();
                            }
                        }
                        else {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("incoming", username);
                            map.put("createdby", username);
                            map.put("isAvailable", true);
                            map.put("status", 0);

                            database.getReference().child("connect").child(username)
                                    .setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            database.getReference().child("connect").child(username).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.child("status").exists()){
                                                        if(snapshot.child("status").getValue(Integer.class) == 1){
                                                            if(isokay)
                                                                return;
                                                            isokay = true;
                                                            Intent intent = new Intent(ConnectActivity.this, CallActivity.class);
                                                            intent.putExtra("username", username);
                                                            intent.putExtra("incoming", snapshot.child("incoming").getValue(String.class));
                                                            intent.putExtra("createdby", snapshot.child("createdby").getValue(String.class));
                                                            intent.putExtra("isAvailable", snapshot.child("isAvailable").getValue(boolean.class));
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}