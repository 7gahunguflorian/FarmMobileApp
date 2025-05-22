package com.example.farmmobileapp;

import android.app.Application;

import com.example.farmmobileapp.utils.SessionManager;
import com.example.farmmobileapp.utils.UserManager;

public class FarmApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize managers
        SessionManager.getInstance(this);
        UserManager userManager = UserManager.getInstance();
        userManager.init(this);
    }
}