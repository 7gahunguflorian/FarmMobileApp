package com.example.farmmobileapp.activities;

import androidx.appcompat.app.AlertDialog;
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

public class MyProductsActivity extends AppCompatActivity implements ProductAdapter.OnProductActionListener {

    private static final String TAG = "MyProductsActivity";
    private static final int REQUEST_ADD_PRODUCT = 1001;

    private RecyclerView recyclerViewProducts;
    private Button buttonRecentOrders, buttonAddProduct, buttonLogout;
    private ImageView imageViewProfile;
    private ProductAdapter productAdapter;
    private List<Product> productsList = new ArrayList<>();
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_my_products);
            Log.d(TAG, "Layout set successfully");

            initViews();
            Log.d(TAG, "Views initialized successfully");

            setupRecyclerView();
            Log.d(TAG, "RecyclerView setup successfully");

            setupButtons();
            Log.d(TAG, "Buttons setup successfully");

            loadUserProfile();
            Log.d(TAG, "Loading user profile...");

            loadMyProducts();
            Log.d(TAG, "Loading products...");

        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate", e);
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_LONG).show();

            // Safe fallback - go back to main activity
            try {
                Intent fallbackIntent = new Intent(this, MainFarmerActivity.class);
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(fallbackIntent);
            } catch (Exception fallbackError) {
                Log.e(TAG, "Fallback failed", fallbackError);
            }
            finish();
        }
    }

    private void initViews() {
        // Initialize SessionManager and ApiService first - these are critical
        sessionManager = SessionManager.getInstance(this);
        if (sessionManager == null) {
            throw new RuntimeException("SessionManager is null - cannot proceed");
        }

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            Log.e(TAG, "User not logged in - redirecting to login");
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
            return;
        }

        apiService = RetrofitClient.getClient().create(ApiService.class);
        if (apiService == null) {
            throw new RuntimeException("ApiService is null - cannot proceed");
        }

        Log.d(TAG, "SessionManager and ApiService initialized");

        // Initialize views with null checks
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        if (recyclerViewProducts == null) {
            throw new RuntimeException("recyclerViewProducts view not found in layout");
        }

        buttonRecentOrders = findViewById(R.id.buttonRecentOrders);
        if (buttonRecentOrders == null) {
            Log.w(TAG, "buttonRecentOrders not found - this might be expected");
        }

        buttonAddProduct = findViewById(R.id.buttonAddProduct);
        if (buttonAddProduct == null) {
            throw new RuntimeException("buttonAddProduct view not found in layout");
        }

        buttonLogout = findViewById(R.id.buttonLogout);
        if (buttonLogout == null) {
            Log.w(TAG, "buttonLogout not found - this might be expected");
        }

        imageViewProfile = findViewById(R.id.imageViewProfile);
        if (imageViewProfile == null) {
            Log.w(TAG, "imageViewProfile not found - this might be expected");
        }

        Log.d(TAG, "All views found and initialized");
    }

    private void setupRecyclerView() {
        try {
            productAdapter = new ProductAdapter(this, productsList, true); // true for farmer's own products
            productAdapter.setOnProductActionListener(this); // Set the listener
            recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewProducts.setAdapter(productAdapter);
            Log.d(TAG, "RecyclerView configured successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
            throw e;
        }
    }

    private void setupButtons() {
        try {
            if (buttonRecentOrders != null) {
                buttonRecentOrders.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Log.d(TAG, "Recent Orders button clicked");
                            finish();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in Recent Orders click", e);
                        }
                    }
                });
            }

            if (buttonAddProduct != null) {
                buttonAddProduct.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Log.d(TAG, "Add Product button clicked");
                            navigateToAddProduct();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in Add Product click", e);
                            Toast.makeText(MyProductsActivity.this, "Error opening add product screen", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            if (buttonLogout != null) {
                buttonLogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Log.d(TAG, "Logout button clicked");
                            logoutUser();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in logout", e);
                        }
                    }
                });
            }

            if (imageViewProfile != null) {
                imageViewProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Log.d(TAG, "Profile image clicked");
                            Toast.makeText(MyProductsActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in profile click", e);
                        }
                    }
                });
            }

            Log.d(TAG, "Button listeners set successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up buttons", e);
            throw e;
        }
    }

    private void navigateToAddProduct() {
        try {
            Intent intent = new Intent(MyProductsActivity.this, AddProductActivity.class);
            startActivityForResult(intent, REQUEST_ADD_PRODUCT);
            Log.d(TAG, "Navigated to AddProductActivity");
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to AddProductActivity", e);
            Toast.makeText(this, "Error opening add product screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void logoutUser() {
        try {
            if (sessionManager != null) {
                sessionManager.logout();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MyProductsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            Toast.makeText(this, "Error during logout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfile() {
        try {
            if (sessionManager == null || apiService == null) {
                Log.e(TAG, "SessionManager or ApiService is null");
                return;
            }

            String authHeader = sessionManager.getAuthHeaderValue();
            if (authHeader == null || authHeader.isEmpty()) {
                Log.e(TAG, "Auth header is null or empty");
                return;
            }

            Log.d(TAG, "Loading user profile with auth header: " + authHeader.substring(0, Math.min(20, authHeader.length())) + "...");

            Call<User> call = apiService.getCurrentUser(authHeader);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    try {
                        Log.d(TAG, "Profile response code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            User user = response.body();
                            sessionManager.saveUser(user);
                            Log.d(TAG, "User profile loaded successfully: " + user.getName());

                            if (imageViewProfile != null && user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                                loadProfileImage(user.getProfileImageUrl());
                            }
                        } else {
                            Log.w(TAG, "Failed to load profile: " + response.code());
                            if (response.code() == 401) {
                                // Token might be expired
                                Log.w(TAG, "Unauthorized - token might be expired");
                                handleUnauthorized();
                            }
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

    private void loadProfileImage(String imageUrl) {
        try {
            if (imageViewProfile == null) return;

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

            Glide.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(imageViewProfile);
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile image", e);
        }
    }

    private void loadMyProducts() {
        Log.d(TAG, "Starting to load farmer's products...");

        try {
            if (sessionManager == null || apiService == null) {
                Log.e(TAG, "SessionManager or ApiService is null - cannot load products");
                return;
            }

            String authHeader = sessionManager.getAuthHeaderValue();
            if (authHeader == null || authHeader.isEmpty()) {
                Log.e(TAG, "Auth header is null or empty - cannot load products");
                handleUnauthorized();
                return;
            }

            Log.d(TAG, "Making API call to load products with auth header: " + authHeader.substring(0, Math.min(20, authHeader.length())) + "...");

            Call<List<Product>> call = apiService.getFarmerProducts(authHeader);
            call.enqueue(new Callback<List<Product>>() {
                @Override
                public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                    Log.d(TAG, "Products response received - code: " + response.code());

                    try {
                        if (response.isSuccessful()) {
                            List<Product> products = response.body();
                            if (products != null) {
                                Log.d(TAG, "Received " + products.size() + " products");

                                productsList.clear();
                                productsList.addAll(products);

                                if (productAdapter != null) {
                                    productAdapter.notifyDataSetChanged();
                                    Log.d(TAG, "Adapter notified of data change");
                                }
                            } else {
                                Log.w(TAG, "Response body is null");
                                productsList.clear();
                                if (productAdapter != null) {
                                    productAdapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            String errorMessage = "Failed to load products - Response code: " + response.code();

                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    Log.e(TAG, "Error body: " + errorBody);
                                    errorMessage += " - " + errorBody;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading error body", e);
                            }

                            Log.e(TAG, errorMessage);

                            if (response.code() == 401) {
                                handleUnauthorized();
                            } else {
                                Toast.makeText(MyProductsActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing products response", e);
                        Toast.makeText(MyProductsActivity.this, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Product>> call, Throwable t) {
                    Log.e(TAG, "Network error loading products", t);
                    Toast.makeText(MyProductsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initiating products load", e);
            Toast.makeText(this, "Error loading products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleUnauthorized() {
        Log.w(TAG, "Handling unauthorized access - logging out user");
        try {
            if (sessionManager != null) {
                sessionManager.logout();
            }

            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error handling unauthorized", e);
        }
    }

    // Implement ProductAdapter.OnProductActionListener methods
    @Override
    public void onEditProduct(Product product) {
        try {
            Log.d(TAG, "Edit product requested: " + product.getName());
            // TODO: Navigate to EditProductActivity when it's created
            Toast.makeText(this, "Edit product: " + product.getName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error in onEditProduct", e);
        }
    }

    @Override
    public void onDeleteProduct(Product product) {
        try {
            Log.d(TAG, "Delete product requested: " + product.getName());
            // Show confirmation dialog before deleting
            new AlertDialog.Builder(this)
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to delete " + product.getName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteProduct(product);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error in onDeleteProduct", e);
        }
    }

    @Override
    public void onOrderProduct(Product product) {
        // This won't be called in owner view
        Log.d(TAG, "onOrderProduct called (should not happen in owner view)");
    }

    private void deleteProduct(Product product) {
        try {
            if (sessionManager == null || apiService == null) {
                Log.e(TAG, "Cannot delete product - SessionManager or ApiService is null");
                return;
            }

            Log.d(TAG, "Deleting product: " + product.getName());

            Call<Void> call = apiService.deleteProduct(sessionManager.getAuthHeaderValue(), product.getId());
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    try {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Product deleted successfully");
                            Toast.makeText(MyProductsActivity.this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                            loadMyProducts(); // Refresh the list
                        } else {
                            Log.e(TAG, "Failed to delete product - code: " + response.code());
                            Toast.makeText(MyProductsActivity.this, "Failed to delete product", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing delete response", e);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Error deleting product", t);
                    Toast.makeText(MyProductsActivity.this, "Network error deleting product", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initiating product deletion", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == REQUEST_ADD_PRODUCT) {
                if (resultCode == RESULT_OK) {
                    // Product was added successfully, refresh the list
                    Log.d(TAG, "Product added successfully, refreshing list");
                    Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show();
                    loadMyProducts();
                } else if (resultCode == RESULT_CANCELED) {
                    // User cancelled adding product
                    Log.d(TAG, "User cancelled adding product");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onActivityResult", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "onResume called - refreshing products");
            // Refresh products when returning to this activity
            loadMyProducts();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            Log.d(TAG, "onDestroy called");
            // Clean up resources if needed
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
    }
}