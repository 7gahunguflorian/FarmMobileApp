package com.example.farmmobileapp.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.farmmobileapp.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import retrofit2.Response;

public class ErrorHandler {
    private static final String TAG = "ErrorHandler";

    public static void handleApiError(Context context, Response<?> response) {
        String errorMessage = "An error occurred";
        
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                try {
                    JsonObject jsonObject = new Gson().fromJson(errorBody, JsonObject.class);
                    if (jsonObject.has("message")) {
                        errorMessage = jsonObject.get("message").getAsString();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing error response", e);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading error body", e);
        }

        switch (response.code()) {
            case Constants.HTTP_UNAUTHORIZED:
                errorMessage = "Session expired. Please login again.";
                break;
            case Constants.HTTP_FORBIDDEN:
                errorMessage = "You don't have permission to perform this action.";
                break;
            case Constants.HTTP_NOT_FOUND:
                errorMessage = "Resource not found.";
                break;
            case Constants.HTTP_INTERNAL_SERVER_ERROR:
                errorMessage = "Server error. Please try again later.";
                break;
        }

        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
    }

    public static void handleNetworkError(Context context, Throwable t) {
        Log.e(TAG, "Network error", t);
        String errorMessage = "Network error. Please check your connection.";
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
    }
} 