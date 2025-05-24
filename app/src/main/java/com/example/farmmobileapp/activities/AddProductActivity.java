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
import com.example.farmmobileapp.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

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
        etProductName = findViewById(R.id.editTextProductName);
        etDescription = findViewById(R.id.editTextDescription);
        etPrice = findViewById(R.id.editTextPrice);
        etAvailableQuantity = findViewById(R.id.editTextAvailableQuantity);
        imgProductPreview = findViewById(R.id.imageViewProductPreview);
        btnSelectImage = findViewById(R.id.buttonSelectImage);
        btnAddProduct = findViewById(R.id.buttonAddProduct);
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

        // Permission launcher
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

    private void setupClickListeners() {
        btnSelectImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
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
                etPrice.setError("Price must be greater than 0");
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
        progressBar.setVisibility(View.VISIBLE);
        btnAddProduct.setEnabled(false);

        try {
            String name = etProductName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            double price = Double.parseDouble(etPrice.getText().toString().trim());
            int availableQuantity = Integer.parseInt(etAvailableQuantity.getText().toString().trim());

            // Create product object
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setAvailableQuantity(availableQuantity);

            // First create the product
            Call<Product> call = apiService.createProduct(sessionManager.getAuthHeaderValue(), product);
            call.enqueue(new Callback<Product>() {
                @Override
                public void onResponse(Call<Product> call, Response<Product> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Product createdProduct = response.body();

                        // If image is selected, upload it
                        if (selectedImageUri != null) {
                            uploadProductImage(createdProduct.getId());
                        } else {
                            // Product created successfully without image
                            progressBar.setVisibility(View.GONE);
                            btnAddProduct.setEnabled(true);
                            Toast.makeText(AddProductActivity.this, "Product added successfully!", Toast.LENGTH_LONG).show();

                            // Set result and finish
                            setResult(RESULT_OK);
                            finish();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnAddProduct.setEnabled(true);

                        String errorMessage = "Failed to add product";
                        if (response.code() == 400) {
                            errorMessage = "Invalid product data";
                        } else if (response.code() == 401) {
                            errorMessage = "Please login again";
                            sessionManager.logout();
                            // Navigate back to login
                            Intent loginIntent = new Intent(AddProductActivity.this, LoginActivity.class);
                            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(loginIntent);
                            finish();
                            return;
                        }
                        Toast.makeText(AddProductActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Product> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    btnAddProduct.setEnabled(true);
                    Log.e(TAG, "Network error adding product", t);
                    Toast.makeText(AddProductActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            btnAddProduct.setEnabled(true);
            Log.e(TAG, "Error adding product", e);
            Toast.makeText(this, "Error adding product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProductImage(Long productId) {
        try {
            // Convert URI to File
            File imageFile = createFileFromUri(selectedImageUri);
            if (imageFile == null) {
                progressBar.setVisibility(View.GONE);
                btnAddProduct.setEnabled(true);
                Toast.makeText(this, "Product added but failed to upload image", Toast.LENGTH_SHORT).show();

                // Still set result as OK since product was created
                setResult(RESULT_OK);
                finish();
                return;
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

            Call<Void> call = apiService.uploadProductImage(sessionManager.getAuthHeaderValue(), productId, imagePart);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    progressBar.setVisibility(View.GONE);
                    btnAddProduct.setEnabled(true);

                    if (response.isSuccessful()) {
                        Toast.makeText(AddProductActivity.this, "Product added successfully with image!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(AddProductActivity.this, "Product added but image upload failed", Toast.LENGTH_SHORT).show();
                    }

                    // Set result as OK and finish
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    btnAddProduct.setEnabled(true);
                    Log.e(TAG, "Error uploading image", t);
                    Toast.makeText(AddProductActivity.this, "Product added but image upload failed", Toast.LENGTH_SHORT).show();

                    // Still set result as OK since product was created
                    setResult(RESULT_OK);
                    finish();
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            btnAddProduct.setEnabled(true);
            Log.e(TAG, "Error preparing image upload", e);
            Toast.makeText(this, "Product added but failed to upload image", Toast.LENGTH_SHORT).show();

            // Still set result as OK since product was created
            setResult(RESULT_OK);
            finish();
        }
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
        } catch (Exception e) {
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
}