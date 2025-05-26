package com.example.farmmobileapp;

import android.app.Application;
import android.os.StrictMode;
import android.util.Log;

import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.utils.SessionManager;
import com.example.farmmobileapp.utils.UserManager;

public class FarmApplication extends Application {
    private static final String TAG = "FarmApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Log.d(TAG, "Initializing application");

            // Enable StrictMode for debugging
            if (BuildConfig.DEBUG) {
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectNetwork()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .penaltyLog()
                    .build());
                Log.d(TAG, "StrictMode enabled for debugging");
            }

            // Initialize network configuration
            RetrofitClient.getClient(); // This will use the default BASE_URL

            // Initialize managers
            SessionManager.getInstance(this);
            UserManager userManager = UserManager.getInstance();
            userManager.init(this);

            Log.d(TAG, "Application initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing application", e);
        }
    }
}