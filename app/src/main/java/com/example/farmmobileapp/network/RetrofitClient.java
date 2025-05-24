package com.example.farmmobileapp.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonDeserializationContext;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static final String BASE_URL = "http://192.168.88.221:8180/";

    // http://10.0.2.2:8180/ For Android emulator
    // Use "http://localhost:8180/" if testing on physical device with backend on same network
    // Use your actual IP address like "http://192.168.1.100:8180/" for physical device

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Add logging interceptor for debugging
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .build();

            // Custom date deserializer to handle different date formats
            JsonDeserializer<Date> dateDeserializer = new JsonDeserializer<Date>() {
                private final SimpleDateFormat[] dateFormats = {
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                        new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                };

                @Override
                public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    String dateString = json.getAsString();

                    for (SimpleDateFormat format : dateFormats) {
                        try {
                            return format.parse(dateString);
                        } catch (ParseException e) {
                            // Try next format
                        }
                    }

                    // If all formats fail, try to parse as timestamp
                    try {
                        long timestamp = json.getAsLong();
                        return new Date(timestamp);
                    } catch (Exception e) {
                        throw new JsonParseException("Unable to parse date: " + dateString);
                    }
                }
            };

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, dateDeserializer)
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
