package com.project.carpool_ride_share_app.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.project.carpool_ride_share_app.R;

public class ChooseRole extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);
    }

    public void driver(View view) {
        Intent main = new Intent(ChooseRole.this, MapViewActivity.class);
        main.putExtra("role", "Driver");
        startActivity(main);
    }

    public void passenger(View view) {
        Intent main = new Intent(ChooseRole.this, MapViewActivity.class);
        main.putExtra("role", "Passenger");
        startActivity(main);
    }

    /*  For getting the value
            Bundle extraValues = getIntent().getExtras();
        if (extraValues != null) {
            int maxQ = (int) extraValues.get("maxQ");
            int correctCount = (int) extraValues.get("correctCount");

            TextView score = findViewById(R.id.scoreCount);
            score.setText(correctCount + " / " + maxQ);
        }
     */

}
