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

import com.bumptech.glide.Glide;
import com.example.farmmobileapp.R;
import com.example.farmmobileapp.adapters.ProductAdapter;
import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.models.User;
import com.example.farmmobileapp.utils.SessionManager;

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
        setContentView(R.layout.activity_available_products);

        try {
            initViews();
            setupRecyclerView();
            setupButtons();
            loadUserProfile();
            loadAvailableProducts();
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
        productAdapter = new ProductAdapter(this, productsList, false); // false for available products
        productAdapter.setOnProductActionListener(this); // Set the listener
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
        try {
            Call<User> call = apiService.getCurrentUser(sessionManager.getAuthHeaderValue());
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            User user = response.body();
                            sessionManager.saveUser(user);

                            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                                String imageUrl = user.getProfileImageUrl();

                                // Handle different URL formats from backend
                                if (!imageUrl.startsWith("http")) {
                                    // If it's a relative path, prepend base URL
                                    if (imageUrl.startsWith("/")) {
                                        imageUrl = RetrofitClient.BASE_URL + imageUrl;
                                    } else {
                                        imageUrl = RetrofitClient.BASE_URL + "/uploads/" + imageUrl;
                                    }
                                }

                                Log.d(TAG, "Loading profile image from: " + imageUrl);

                                Glide.with(AvailableProductsActivity.this)
                                        .load(imageUrl)
                                        .circleCrop()
                                        .placeholder(R.drawable.ic_profile)
                                        .error(R.drawable.ic_profile)
                                        .into(imageViewProfile);
                            }
                        } else {
                            Log.w(TAG, "Failed to load profile: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing profile response", e);
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e(TAG, "Failed to load profile", t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading user profile", e);
        }
    }

    private void loadAvailableProducts() {
        Log.d(TAG, "Loading available products...");

        try {
            // Fixed: Remove auth header since getAllProducts() doesn't require it
            Call<List<Product>> call = apiService.getAllProducts();
            call.enqueue(new Callback<List<Product>>() {
                @Override
                public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                    Log.d(TAG, "Products response code: " + response.code());

                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Product> products = response.body();
                            Log.d(TAG, "Received " + products.size() + " products");

                            productsList.clear();
                            productsList.addAll(products);
                            productAdapter.notifyDataSetChanged();
                        } else {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading error body", e);
                            }

                            Log.e(TAG, "Failed to load products: " + response.code() + " - " + errorBody);
                            Toast.makeText(AvailableProductsActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing products response", e);
                    }
                }

                @Override
                public void onFailure(Call<List<Product>> call, Throwable t) {
                    Log.e(TAG, "Network error loading products", t);
                    Toast.makeText(AvailableProductsActivity.this, "Network error loading products", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initiating products load", e);
        }
    }

    // Implement ProductAdapter.OnProductActionListener methods
    @Override
    public void onEditProduct(Product product) {
        // This won't be called in marketplace view
    }

    @Override
    public void onDeleteProduct(Product product) {
        // This won't be called in marketplace view
    }

    @Override
    public void onOrderProduct(Product product) {
        // Navigate to order form
        Intent intent = new Intent(this, OrderFormActivity.class);
        intent.putExtra("product_id", product.getId().toString());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            loadAvailableProducts();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume", e);
        }
    }
}