package com.example.farmmobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.farmmobileapp.activities.LoginActivity;
import com.example.farmmobileapp.activities.MainClientActivity;
import com.example.farmmobileapp.activities.MainFarmerActivity;
import com.example.farmmobileapp.utils.SessionManager;
import com.example.farmmobileapp.utils.UserManager;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private SessionManager sessionManager;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize managers
        sessionManager = SessionManager.getInstance(this);
        userManager = UserManager.getInstance();
        userManager.init(this);

        // Delay and then check login status
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkLoginStatus();
            }
        }, SPLASH_DELAY);
    }

    private void checkLoginStatus() {
        if (sessionManager.isLoggedIn() && sessionManager.getUser() != null) {
            // User is logged in, navigate to appropriate main activity
            navigateBasedOnRole();
        } else {
            // User is not logged in, go to login activity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        finish();
    }

    private void navigateBasedOnRole() {
        String role = sessionManager.getUser().getRole();

        if ("FARMER".equals(role)) {
            startActivity(new Intent(MainActivity.this, MainFarmerActivity.class));
        } else if ("CLIENT".equals(role)) {
            startActivity(new Intent(MainActivity.this, MainClientActivity.class));
        } else {
            // Unknown role, logout and go to login
            sessionManager.logout();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }
}