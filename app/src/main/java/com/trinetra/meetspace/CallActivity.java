package com.trinetra.meetspace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trinetra.meetspace.databinding.ActivityCallBinding;
import com.trinetra.meetspace.models.Interfaces;
import com.trinetra.meetspace.models.UserModel;

import java.util.UUID;

public class CallActivity extends AppCompatActivity {

    ActivityCallBinding binding;

    String uniqueid = "";
    FirebaseAuth auth;
    String username = "";
    String friendusername = "";
    boolean isPeerconnected = false;
    DatabaseReference databaseReference;
    boolean isAudio = true;
    boolean iscamera = true;

    String createdby;
    boolean pageexit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("connect");
        username = getIntent().getStringExtra("username");
        String incoming = getIntent().getStringExtra("incoming");
        createdby = getIntent().getStringExtra("createdby");

//        friendusername = "";
//
//        if(incoming.equalsIgnoreCase(friendusername))
//            friendusername = incoming;

        friendusername = incoming;
        setupweb();

        binding.micbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAudio = !isAudio;
                callJavaScript("javascipt:toggleAudio(\"" + isAudio + "\")");
                if(isAudio)
                    binding.micbtn.setImageResource(R.drawable.btn_unmute_normal);
                else
                    binding.micbtn.setImageResource(R.drawable.btn_mute_normal);
            }
        });

        binding.videobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iscamera = !iscamera;
                callJavaScript("javascript:toggleVideo(\"" + iscamera + "\")");
                if(iscamera)
                    binding.videobtn.setImageResource(R.drawable.btn_video_normal);
                else
                    binding.videobtn.setImageResource(R.drawable.btn_video_muted);
            }
        });

        binding.endbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    void setupweb()
    {
        binding.webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        binding.webview.getSettings().setJavaScriptEnabled(true);
        binding.webview.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.webview.addJavascriptInterface(new Interfaces(this), "AndroidDeveloper");

        loadVideo();
    }

    public void loadVideo(){
        String filepath = "file:android_asset/call.html";
        binding.webview.loadUrl(filepath);

        binding.webview.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();
            }
        });
    }

    void initializePeer(){
        uniqueid = getUniqueid();

        callJavaScript("javascript:init(\"" + uniqueid + "\")");

        if(createdby.equalsIgnoreCase(username)){
            if(pageexit)
                return;
            databaseReference.child(username).child("connId").setValue(uniqueid);
            databaseReference.child(username).child("isAvailable").setValue(true);

            binding.loadinggroup.setVisibility(View.GONE);
            binding.controls.setVisibility(View.VISIBLE);

            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(friendusername)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel userModel = snapshot.getValue(UserModel.class);

                            Glide.with(CallActivity.this)
                                    .load(userModel.getProfile())
                                    .into(binding.profileimg);

                            binding.profilename.setText(userModel.getName());
                            binding.profilelocation.setText(userModel.getCity());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
        else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendusername = createdby;
                    FirebaseDatabase.getInstance().getReference().child("Users")
                            .child(friendusername)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    UserModel userModel = snapshot.getValue(UserModel.class);

                                    Glide.with(CallActivity.this)
                                            .load(userModel.getProfile())
                                            .into(binding.profileimg);

                                    binding.profilename.setText(userModel.getName());
                                    binding.profilelocation.setText(userModel.getCity());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                    FirebaseDatabase.getInstance().getReference().child("connect")
                            .child(friendusername)
                            .child("connId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.getValue() != null){
                                        sendCallReq();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }
            }, 3000);
        }
    }

    public void onPeerConnected(){
        isPeerconnected = true;
    }

    void sendCallReq(){
        if(!isPeerconnected) {
            Toast.makeText(this, "Not Connected. Please check your Connection", Toast.LENGTH_SHORT).show();
            return;
        }

        listenconn();
    }

    void listenconn(){
        databaseReference.child(friendusername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null)
                    return;

                binding.loadinggroup.setVisibility(View.GONE);
                binding.controls.setVisibility(View.VISIBLE);
                String connId = snapshot.getValue(String.class);

                callJavaScript("javascript:startCall(\"" + connId + "\")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void callJavaScript(String function){
        binding.webview.post(new Runnable() {
            @Override
            public void run() {
                binding.webview.evaluateJavascript(function, null);
            }
        });
    }
    String getUniqueid(){
        return UUID.randomUUID().toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageexit = true;
        databaseReference.child(createdby).setValue(null);
        finish();
    }
}