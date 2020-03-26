package com.project.carpool_ride_share_app.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.project.carpool_ride_share_app.R;

/**
 *  COSC 341 - Carpool Ride Sharing Application
 *
 *  Cheeky little splash screen. Nothing else is styled yet.
 */

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
    }

    public void openApp(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
