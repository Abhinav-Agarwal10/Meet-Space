package com.trinetra.meetspace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trinetra.meetspace.databinding.ActivityRewardBinding;

public class RewardActivity extends AppCompatActivity {

    ActivityRewardBinding binding;
    private RewardedAd rewardedAd;

    FirebaseDatabase database;
    String currentid;
    int coins = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRewardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        currentid = FirebaseAuth.getInstance().getUid();
        loadAd();

        database.getReference().child("Users")
                        .child(currentid)
                                .child("coins")
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                coins = snapshot.getValue(Integer.class);
                                                binding.coins.setText(String.valueOf(coins));
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

        binding.video1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showad(200);
            }
        });
        binding.video2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showad(300);
            }
        });
        binding.video3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showad(400);
            }
        });
        binding.video4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showad(500);
            }
        });
        binding.video5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showad(1000);
            }
        });
    }

    void loadAd(){
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d("TAG", loadAdError.toString());
                        rewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        Log.d("TAG", "Ad was loaded.");
                    }
                });
    }

    void showad(int num){
        if (rewardedAd != null) {
            Activity activityContext = RewardActivity.this;
            rewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    loadAd();
                    coins = coins + num;

                    database.getReference().child("Users")
                            .child(currentid)
                            .child("coins")
                            .setValue(coins);
                }
            });
        } else {
            Log.d("TAG", "The rewarded ad wasn't ready yet.");
        }
    }
}