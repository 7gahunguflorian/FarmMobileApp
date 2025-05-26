package com.example.farmmobileapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.farmmobileapp.R;
import com.example.farmmobileapp.adapters.ProductAdapter;
import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.models.User;
import com.example.farmmobileapp.utils.SessionManager;
import com.example.farmmobileapp.models.ApiResponse;
import com.example.farmmobileapp.utils.ErrorHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvailableProductsActivity extends AppCompatActivity implements ProductAdapter.OnProductActionListener {

    private static final String TAG = "AvailableProductsActivity";

    private RecyclerView recyclerViewProducts;
    private Button buttonRecentOrders, buttonCreateOrder, buttonLogout;
    private ImageView imageViewProfile;
    private ProductAdapter productAdapter;
    private List<Product> productsList = new ArrayList<>();
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Activity created successfully");
        setContentView(R.layout.activity_available_products);

        try {
            initViews();
            setupRecyclerView();
            setupButtons();
            loadUserProfile();
            loadProducts();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        buttonRecentOrders = findViewById(R.id.buttonRecentOrders);
        buttonCreateOrder = findViewById(R.id.buttonCreateOrder);
        buttonLogout = findViewById(R.id.buttonLogout);
        imageViewProfile = findViewById(R.id.imageViewProfile);

        sessionManager = SessionManager.getInstance(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this, productsList, true);
        productAdapter.setOnProductActionListener(this);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void setupButtons() {
        buttonRecentOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to main farmer activity
                finish();
            }
        });

        buttonCreateOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // For now, just show a message since CreateOrderActivity doesn't exist
                Toast.makeText(AvailableProductsActivity.this,
                        "Please select a product to place an order", Toast.LENGTH_SHORT).show();
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AvailableProductsActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
                // You can navigate to profile activity here
            }
        });
    }

    private void logoutUser() {
        try {
            sessionManager.logout();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(AvailableProductsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            Toast.makeText(this, "Error during logout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfile() {
        Log.d(TAG, "loadUserProfile: Loading user profile");
        try {
            User user = sessionManager.getUser();
            if (user != null) {
                loadProfileImage();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user profile", e);
        }
    }

    private void loadProfileImage() {
        Log.d(TAG, "loadProfileImage: Loading profile image");
        User user = sessionManager.getUser();
        if (user != null && user.getProfileImageUrl() != null) {
            String imageUrl = user.getProfileImageUrl();
            // Replace localhost with the actual server IP
            imageUrl = imageUrl.replace("localhost:8180", "192.168.88.247:8180");
            // Remove any double slashes and fix the path
            imageUrl = imageUrl.replace("//", "/");
            
            // Get auth token
            String authToken = sessionManager.getAuthToken();
            if (authToken != null) {
                // Create GlideUrl with auth header
                GlideUrl glideUrl = new GlideUrl(imageUrl, new LazyHeaders.Builder()
                        .addHeader("Authorization", "Bearer " + authToken)
                        .build());

                Glide.with(this)
                    .load(glideUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .into(imageViewProfile);
            } else {
                imageViewProfile.setImageResource(R.drawable.profile_placeholder);
            }
        } else {
            Log.d(TAG, "loadProfileImage: No profile image URL, using placeholder");
            imageViewProfile.setImageResource(R.drawable.profile_placeholder);
        }
    }

    private void loadProducts() {
        Log.d(TAG, "loadProducts: Starting to load products");
        String authHeader = sessionManager.getAuthHeaderValue();
        Log.d(TAG, "Loading all products with auth header: " + authHeader);
        
        if (authHeader == null) {
            Log.e(TAG, "loadProducts: No auth header available");
            showError("Authentication required");
            return;
        }

        apiService.getAllProducts(authHeader).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    Log.d(TAG, "API Response received: " + products.size() + " products");
                    productsList.clear();
                    productsList.addAll(products);
                    productAdapter.updateProducts(productsList);
                    Log.d(TAG, "Loaded " + products.size() + " products");
                    
                    // Show products list if we have products, otherwise show empty view
                    if (products.isEmpty()) {
                        showEmptyView();
                    } else {
                        showProductsList();
                    }
                } else {
                    Log.e(TAG, "loadProducts: API call failed with code " + response.code());
                    String errorMessage = "Failed to load products";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    showError(errorMessage);
                    showEmptyView();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(TAG, "loadProducts: Network error", t);
                showError("Network error: " + t.getMessage());
                showEmptyView();
            }
        });
    }

    private void showProductsList() {
        // Show products list
        if (recyclerViewProducts != null) {
            recyclerViewProducts.setVisibility(View.VISIBLE);
        }
        // Hide empty state message
        TextView emptyView = findViewById(R.id.emptyView);
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }

    // Implement ProductAdapter.OnProductActionListener methods

    public void onProductClick(Product product) {
        // Navigate to order form
        Intent intent = new Intent(this, OrderFormActivity.class);
        intent.putExtra("product_id", product.getId().toString());
        startActivity(intent);
    }

    @Override
    public void onEditClick(Product product) {
        // This won't be called in marketplace view
        Toast.makeText(this, "Edit not available in marketplace view", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(Product product) {
        // This won't be called in marketplace view
        Toast.makeText(this, "Delete not available in marketplace view", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void showProgress() {
        // Show progress indicator
        if (recyclerViewProducts != null) {
            recyclerViewProducts.setVisibility(View.GONE);
        }
        // You might want to add a ProgressBar to your layout and show it here
    }

    private void hideProgress() {
        // Hide progress indicator
        if (recyclerViewProducts != null) {
            recyclerViewProducts.setVisibility(View.VISIBLE);
        }
        // Hide the ProgressBar if you added one
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateEmptyView() {
        if (productsList.isEmpty()) {
            // Show empty state
            if (recyclerViewProducts != null) {
                recyclerViewProducts.setVisibility(View.GONE);
            }
            // Show empty state message
            TextView emptyView = findViewById(R.id.emptyView);
            if (emptyView != null) {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("No products available at the moment");
            }
        } else {
            // Show products
            if (recyclerViewProducts != null) {
                recyclerViewProducts.setVisibility(View.VISIBLE);
            }
            // Hide empty state message
            TextView emptyView = findViewById(R.id.emptyView);
            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    private void showEmptyView() {
        // Show empty state
        if (recyclerViewProducts != null) {
            recyclerViewProducts.setVisibility(View.GONE);
        }
        // Show empty state message
        TextView emptyView = findViewById(R.id.emptyView);
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("No products available at the moment");
        }
    }
}