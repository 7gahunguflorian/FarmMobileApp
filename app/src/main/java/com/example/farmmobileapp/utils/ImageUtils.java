package com.example.farmmobileapp.utils;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.farmmobileapp.R;
import com.example.farmmobileapp.network.RetrofitClient;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;

public class ImageUtils {

    /**
     * Load image from URL into ImageView using Glide
     *
     * @param context Application context
     * @param imageUrl URL of the image to load
     * @param imageView ImageView to load the image into
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.placeholder_image);
            return;
        }

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        // Get the full URL for the image
        String fullImageUrl = getFullImageUrl(imageUrl, false);
        
        // Log the full URL for debugging
        Log.d("ImageUtils", "Loading image from URL: " + fullImageUrl);

        Glide.with(context)
                .asBitmap()
                .load(fullImageUrl)
                .apply(requestOptions)
                .into(imageView);
    }

    /**
     * Load profile image from URL into ImageView using Glide
     *
     * @param context Application context
     * @param imageUrl URL of the profile image to load
     * @param imageView ImageView to load the profile image into
     */
    public static void loadProfileImage(Context context, String imageUrl, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        // Handle relative URLs from backend
        String fullImageUrl = getFullImageUrl(imageUrl, true);

        Glide.with(context)
                .load(fullImageUrl)
                .apply(requestOptions)
                .into(imageView);
    }

    /**
     * Create a temporary image file
     *
     * @param context Application context
     * @return Created temporary file
     * @throws IOException If file cannot be created
     */
    @NonNull
    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(null);

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    /**
     * Get the full image URL for display
     *
     * @param imageUrl The relative or absolute image URL
     * @param isProfileImage Whether this is a profile image
     * @return Full URL for image display
     */
    public static String getFullImageUrl(String imageUrl, boolean isProfileImage) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        
        // If it's already a full URL, return it as is
        if (imageUrl.startsWith("http")) {
            return imageUrl;
        }
        
        // Get base URL and remove /api/ if present
        String baseUrl = RetrofitClient.getBaseUrl();
        if (baseUrl.endsWith("/api/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 5);
        }
        
        // Handle different URL formats
        if (imageUrl.startsWith("/")) {
            return baseUrl + imageUrl;
        } else {
            return baseUrl + "/images/" + imageUrl;
        }
    }
}