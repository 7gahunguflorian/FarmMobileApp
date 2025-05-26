package com.example.farmmobileapp.network;

import android.util.Log;

import com.example.farmmobileapp.models.ApiResponse;
import com.example.farmmobileapp.models.AuthResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.reflect.TypeToken;
import com.google.gson.TypeAdapter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    
    // Base URL for your local server
    private static final String BASE_URL = "http://192.168.137.204:8180/api/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            try {
                // Add logging interceptor for debugging
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                    Log.e(TAG, "OkHttp: " + message);
                });
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .addInterceptor(loggingInterceptor)
                        .build();

                Log.d(TAG, "Initializing Retrofit with base URL: " + BASE_URL);

                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                Log.d(TAG, "Retrofit client initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error initializing Retrofit client", e);
                throw e;
            }
        }
        return retrofit;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }
}
