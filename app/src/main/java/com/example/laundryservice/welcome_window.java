package com.example.laundryservice;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class welcome_window extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_window);

        Toast.makeText(this, "Join our facebook Page 'Object Canvas Technology'", Toast.LENGTH_LONG).show();



        View splashView = findViewById(R.id.splash_view);


        Animation zoomInAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom_in);


        splashView.startAnimation(zoomInAnimation);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(welcome_window.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DURATION);
    }
}
