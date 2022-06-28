package com.egrobots.grassanalysis2.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.egrobots.grassanalysis2.R;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_TIME_OUT = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivity(new Intent(SplashActivity.this, SignUpActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, RequestsActivity.class));
            }
        }, SPLASH_TIME_OUT);
    }
}