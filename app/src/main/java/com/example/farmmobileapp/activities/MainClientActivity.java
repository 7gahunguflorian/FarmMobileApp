package com.example.farmmobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.farmmobileapp.R;
import com.example.farmmobileapp.adapters.ProductAdapter;
import com.example.farmmobileapp.decorations.GridSpacingItemDecoration;
import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.models.ApiResponse;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.models.User;
import com.example.farmmobileapp.utils.Constants;
import com.example.farmmobileapp.utils.ErrorHandler;
import com.example.farmmobileapp.utils.SessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainClientActivity extends AppCompatActivity implements ProductAdapter.OnProductActionListener {
    private static final String TAG = "MainClientActivity";
    private RecyclerView recyclerViewProducts;
    private Button buttonRecentOrders, buttonLogout;
    private ImageView imageViewProfile;
    private ProgressBar progressBar;
    private TextView tvEmptyView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private ProductAdapter productAdapter;
    private List<Product> productsList = new ArrayList<>();
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Activity created successfully");
        setContentView(R.layout.activity_main_client);

        try {
            initViews();
            setupRecyclerView();
            setupButtons();
            setupSwipeRefresh();
            loadUserProfile();
            loadAvailableProducts();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        recyclerViewProducts = findViewById(R.id.gridProducts);
        buttonRecentOrders = findViewById(R.id.buttonRecentOrders);
        buttonLogout = findViewById(R.id.buttonLogout);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyView = findViewById(R.id.tvEmptyView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        searchView = findViewById(R.id.searchView);

        sessionManager = SessionManager.getInstance(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this, productsList, true);
        productAdapter.setOnProductActionListener(this);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewProducts.addItemDecoration(new GridSpacingItemDecoration(2, 16, true));
        recyclerViewProducts.setAdapter(productAdapter);
        
        // Set up order click listener
        productAdapter.setOnOrderClickListener(product -> {
            Intent intent = new Intent(MainClientActivity.this, CreateOrderActivity.class);
            intent.putExtra("product_id", product.getId());
            intent.putExtra("product_name", product.getName());
            intent.putExtra("product_price", product.getPrice());
            startActivity(intent);
        });
    }

    private void setupButtons() {
        buttonRecentOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecentOrdersActivity.class);
            startActivity(intent);
        });

        buttonLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        imageViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadAvailableProducts();
        });
    }

    private void loadUserProfile() {
        String token = sessionManager.getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getCurrentUser("Bearer " + token).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        User user = apiResponse.getData();
                        sessionManager.saveUser(user);
                        loadUserProfileImage(user);
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? 
                            apiResponse.getMessage() : "Failed to load profile";
                        Toast.makeText(MainClientActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ErrorHandler.handleApiError(MainClientActivity.this, response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e(TAG, "Error loading profile", t);
                Toast.makeText(MainClientActivity.this, 
                    "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserProfileImage(User user) {
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            String imageUrl = user.getProfileImageUrl();
            // Remove leading slash and 'profile-images/' prefix if present
            if (imageUrl.startsWith("/")) {
                imageUrl = imageUrl.substring(1);
            }
            if (imageUrl.startsWith("profile-images/")) {
                imageUrl = imageUrl.substring(15); // Remove "profile-images/" prefix
            }
            // Construct full URL for profile images without /api/
            imageUrl = RetrofitClient.getBaseUrl().replace("/api", "") + "profile-images/" + imageUrl;

            Log.d(TAG, "Loading profile image from: " + imageUrl);

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .circleCrop()
                    .into(imageViewProfile);
        } else {
            imageViewProfile.setImageResource(R.drawable.default_profile);
        }
    }

    private void loadAvailableProducts() {
        showProgress();
        
        String token = sessionManager.getAuthToken();
        if (token == null) {
            hideProgress();
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = "Bearer " + token;
        Log.d(TAG, "Loading all products with auth header: " + authHeader);
        
        apiService.getAllProducts(authHeader).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                hideProgress();
                swipeRefreshLayout.setRefreshing(false);
                
                Log.d(TAG, "Response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    Log.d(TAG, "Received " + products.size() + " products");
                    
                    productsList.clear();
                    productsList.addAll(products);
                    
                    if (products.isEmpty()) {
                        showEmptyView();
                    } else {
                        hideEmptyView();
                        productAdapter.updateProducts(products);
                    }
                } else {
                    String errorMessage = "Error: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error response body: " + errorBody);
                            errorMessage = errorBody;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(MainClientActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    showEmptyView();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                hideProgress();
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "Error loading products", t);
                String errorMessage = t.getMessage();
                if (errorMessage != null && errorMessage.contains("Expected BEGIN_OBJECT")) {
                    errorMessage = "Server response format error. Please try again later.";
                }
                Toast.makeText(MainClientActivity.this, 
                    "Network error: " + errorMessage, Toast.LENGTH_SHORT).show();
                showEmptyView();
            }
        });
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewProducts.setVisibility(View.GONE);
        tvEmptyView.setVisibility(View.GONE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
        recyclerViewProducts.setVisibility(View.VISIBLE);
    }

    private void showEmptyView() {
        progressBar.setVisibility(View.GONE);
        recyclerViewProducts.setVisibility(View.GONE);
        tvEmptyView.setVisibility(View.VISIBLE);
    }

    private void hideEmptyView() {
        tvEmptyView.setVisibility(View.GONE);
        recyclerViewProducts.setVisibility(View.VISIBLE);
    }

    public void searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            productAdapter.updateProducts(productsList);
            return;
        }

        List<Product> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();
        
        for (Product product : productsList) {
            boolean matches = false;
            
            // Check name
            if (product.getName() != null && 
                product.getName().toLowerCase().contains(lowerQuery)) {
                matches = true;
            }
            
            // Check description
            if (!matches && product.getDescription() != null && 
                product.getDescription().toLowerCase().contains(lowerQuery)) {
                matches = true;
            }
            
            // Check farmer name
            if (!matches && product.getFarmerName() != null && 
                product.getFarmerName().toLowerCase().contains(lowerQuery)) {
                matches = true;
            }
            
            if (matches) {
                filteredList.add(product);
            }
        }
        
        productAdapter.updateProducts(filteredList);
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
        // This won't be called in client view
        Toast.makeText(this, "Edit not available in client view", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(Product product) {
        // This won't be called in client view
        Toast.makeText(this, "Delete not available in client view", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_my_orders) {
            // Open orders activity
            Toast.makeText(this, "My Orders clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_logout) {
            // Log out
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAvailableProducts();
    }

    private void updateEmptyView() {
        if (productsList.isEmpty()) {
            recyclerViewProducts.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerViewProducts.setVisibility(View.VISIBLE);
            tvEmptyView.setVisibility(View.GONE);
        }
    }
}