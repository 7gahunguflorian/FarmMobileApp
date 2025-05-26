package com.example.farmmobileapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.farmmobileapp.R;
import com.example.farmmobileapp.models.ApiResponse;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.utils.ErrorHandler;
import com.example.farmmobileapp.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProductActivity extends AppCompatActivity {
    private static final String TAG = "EditProductActivity";

    private EditText etName, etDescription, etPrice, etQuantity;
    private ImageView imageViewProduct;
    private Button buttonSave, buttonSelectImage;
    private ProgressBar progressBar;
    private Product currentProduct;
    private Uri selectedImageUri;
    private ApiService apiService;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imageViewProduct.setImageURI(selectedImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        initViews();
        setupClickListeners();
        loadProductData();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        imageViewProduct = findViewById(R.id.imageViewProduct);
        buttonSave = findViewById(R.id.buttonSave);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        progressBar = findViewById(R.id.progressBar);

        apiService = RetrofitClient.getClient().create(ApiService.class);
        sessionManager = SessionManager.getInstance(this);
    }

    private void setupClickListeners() {
        buttonSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        buttonSave.setOnClickListener(v -> updateProduct());
    }

    private void loadProductData() {
        Long productId = getIntent().getLongExtra("product_id", -1);
        if (productId == -1) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProductDetails(productId);
    }

    private void loadProductDetails(Long productId) {
        String authHeader = sessionManager.getAuthHeaderValue();
        if (authHeader == null) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getProductById(authHeader, productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body();
                    currentProduct = product;
                    populateFields(product);
                } else {
                    String errorMessage = "Failed to load product details";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(EditProductActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
                showLoading(false);
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EditProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields(Product product) {
        etName.setText(product.getName());
        etDescription.setText(product.getDescription());
        etPrice.setText(product.getPrice().toString());
        etQuantity.setText(product.getAvailableQuantity().toString());

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            String imageUrl = product.getImageUrl();
            // Handle different URL formats from backend
            if (imageUrl.startsWith("/")) {
                imageUrl = RetrofitClient.getBaseUrl() + imageUrl.substring(1);
            } else {
                imageUrl = RetrofitClient.getBaseUrl() + "images/" + imageUrl;
            }

            Log.d(TAG, "Loading product image from: " + imageUrl);

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(imageViewProduct);
        }
    }

    private void updateProduct() {
        if (currentProduct == null) {
            Toast.makeText(this, "No product to update", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        Call<Product> call = apiService.updateProduct(sessionManager.getAuthHeaderValue(), currentProduct.getId(), currentProduct);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product updatedProduct = response.body();
                    if (selectedImageUri != null) {
                        uploadProductImage(updatedProduct.getId());
                    } else {
                        showLoading(false);
                        Toast.makeText(EditProductActivity.this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                } else {
                    showLoading(false);
                    String errorMessage = "Failed to update product";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(EditProductActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EditProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadProductImage(Long productId) {
        if (selectedImageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        String authHeader = sessionManager.getAuthHeaderValue();
        if (authHeader == null) {
            hideLoading();
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File imageFile = new File(selectedImageUri.getPath());
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

            apiService.uploadProductImage(authHeader, productId, body).enqueue(new Callback<Product>() {
                @Override
                public void onResponse(Call<Product> call, Response<Product> response) {
                    hideLoading();
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(EditProductActivity.this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
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
                        Toast.makeText(EditProductActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Product> call, Throwable t) {
                    hideLoading();
                    Toast.makeText(EditProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            hideLoading();
            Toast.makeText(this, "Error preparing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonSave.setEnabled(!show);
        buttonSelectImage.setEnabled(!show);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        buttonSave.setEnabled(true);
        buttonSelectImage.setEnabled(true);
    }
} 