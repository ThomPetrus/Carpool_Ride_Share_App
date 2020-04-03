package com.project.carpool_ride_share_app.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.core.app.ActivityCompat;

import com.project.carpool_ride_share_app.R;

import static com.project.carpool_ride_share_app.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

/**
 * COSC 341 - Carpool Ride Sharing Application
 * <p>
 * Cheeky little splash screen. Nothing else is styled yet.
 */

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e){
            System.out.println("Couldn't hide action bar.");
        }
        setContentView(R.layout.activity_splash_screen);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }

    public void openApp(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
