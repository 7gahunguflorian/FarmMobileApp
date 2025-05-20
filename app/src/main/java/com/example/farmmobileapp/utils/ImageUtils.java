package com.example.farmmobileapp.utils;


import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.farmmobileapp.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

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
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(imageUrl)
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

        Glide.with(context)
                .load(imageUrl)
                .apply(requestOptions)
                .into(imageView);
    }

    /**
     * Upload image to Firebase Storage
     *
     * @param filePath Path to the image file
     * @param storagePath Firebase Storage path to upload to
     * @param callback Callback for upload result
     */
    public static void uploadImage(String filePath, String storagePath, final UploadCallback callback) {
        if (filePath == null || filePath.isEmpty()) {
            if (callback != null) {
                callback.onFailure("File path is empty");
            }
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            if (callback != null) {
                callback.onFailure("File does not exist");
            }
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = UUID.randomUUID().toString() + "_" + file.getName();
        StorageReference imageRef = storageRef.child(storagePath + "/" + fileName);

        UploadTask uploadTask = imageRef.putFile(android.net.Uri.fromFile(file));
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                if (callback != null) {
                    callback.onSuccess(downloadUrl);
                }
            });
        }).addOnFailureListener(e -> {
            if (callback != null) {
                callback.onFailure(e.getMessage());
            }
        });
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
     * Callback for image upload
     */
    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }
}