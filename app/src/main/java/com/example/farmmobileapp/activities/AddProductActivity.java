package com.example.farmmobileapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.farmmobileapp.R;
import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.models.ApiResponse;
import com.example.farmmobileapp.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductActivity extends AppCompatActivity {

    private static final String TAG = "AddProductActivity";

    private EditText etProductName, etDescription, etPrice, etAvailableQuantity;
    private ImageView imgProductPreview;
    private Button btnSelectImage, btnAddProduct;
    private ProgressBar progressBar;

    private ApiService apiService;
    private SessionManager sessionManager;
    private Uri selectedImageUri;

    // Activity result launcher for image selection
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        try {
            initViews();
            setupToolbar();
            setupActivityResultLaunchers();
            setupClickListeners();

            sessionManager = SessionManager.getInstance(this);
            apiService = RetrofitClient.getClient().create(ApiService.class);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        etProductName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etAvailableQuantity = findViewById(R.id.etQuantity);
        imgProductPreview = findViewById(R.id.imageViewProduct);
        btnSelectImage = findViewById(R.id.buttonSelectImage);
        btnAddProduct = findViewById(R.id.buttonSave);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Add New Product");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupActivityResultLaunchers() {
        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        displaySelectedImage(uri);
                    }
                }
        );

        // Permission launcher - updated for Android 13+ compatibility
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(this, "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    // Update your setupClickListeners method
    private void setupClickListeners() {
        btnSelectImage.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                openImagePicker();
            } else {
                requestStoragePermission();
            }
        });

        btnAddProduct.setOnClickListener(v -> {
            if (validateInputs()) {
                addProduct();
            }
        });
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void displaySelectedImage(Uri imageUri) {
        try {
            Glide.with(this)
                    .load(imageUri)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(imgProductPreview);

            btnSelectImage.setText("Change Image");
        } catch (Exception e) {
            Log.e(TAG, "Error displaying selected image", e);
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs() {
        String name = etProductName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String quantityStr = etAvailableQuantity.getText().toString().trim();

        if (name.isEmpty()) {
            etProductName.setError("Product name is required");
            etProductName.requestFocus();
            return false;
        }

        if (name.length() < 3) {
            etProductName.setError("Product name must be at least 3 characters");
            etProductName.requestFocus();
            return false;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return false;
        }

        if (description.length() < 10) {
            etDescription.setError("Description must be at least 10 characters");
            etDescription.requestFocus();
            return false;
        }

        if (priceStr.isEmpty()) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return false;
        }

        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                etPrice.setError("Price must be greater than 0 FBU");
                etPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price format");
            etPrice.requestFocus();
            return false;
        }

        if (quantityStr.isEmpty()) {
            etAvailableQuantity.setError("Available quantity is required");
            etAvailableQuantity.requestFocus();
            return false;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                etAvailableQuantity.setError("Quantity must be greater than 0");
                etAvailableQuantity.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etAvailableQuantity.setError("Invalid quantity format");
            etAvailableQuantity.requestFocus();
            return false;
        }

        return true;
    }

    private void addProduct() {
        showLoading(true);

        Product product = new Product();
        product.setName(etProductName.getText().toString().trim());
        product.setDescription(etDescription.getText().toString().trim());
        product.setPrice(new BigDecimal(etPrice.getText().toString().trim()));
        product.setAvailableQuantity(Integer.parseInt(etAvailableQuantity.getText().toString().trim()));

        Call<Product> call = apiService.createProduct(sessionManager.getAuthHeaderValue(), product);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product createdProduct = response.body();
                    if (selectedImageUri != null) {
                        uploadProductImage(createdProduct.getId());
                    } else {
                        showLoading(false);
                        Toast.makeText(AddProductActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    showLoading(false);
                    String errorMessage = "Failed to add product";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(AddProductActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AddProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadProductImage(Long productId) {
        File imageFile = createFileFromUri(selectedImageUri);
        if (imageFile == null) {
            showLoading(false);
            Toast.makeText(this, "Error preparing image", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        Call<Product> call = apiService.uploadProductImage(sessionManager.getAuthHeaderValue(), productId, imagePart);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AddProductActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMessage = "Failed to upload product image";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(AddProductActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AddProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File createFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = new File(getCacheDir(), "temp_product_image.jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;
        } catch (IOException e) {
            Log.e(TAG, "Error creating file from URI", e);
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        // Check if there are unsaved changes
        if (hasUnsavedChanges()) {
            showDiscardChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        // Check if any field has content
        return !etProductName.getText().toString().trim().isEmpty() ||
                !etDescription.getText().toString().trim().isEmpty() ||
                !etPrice.getText().toString().trim().isEmpty() ||
                !etAvailableQuantity.getText().toString().trim().isEmpty() ||
                selectedImageUri != null;
    }

    private void showDiscardChangesDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Discard Changes?")
                .setMessage("You have unsaved changes. Are you sure you want to discard them?")
                .setPositiveButton("Discard", (dialog, which) -> {
                    setResult(RESULT_CANCELED);
                    finish();
                })
                .setNegativeButton("Keep Editing", null)
                .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnAddProduct.setEnabled(!show);
    }
}