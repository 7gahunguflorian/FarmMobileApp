package com.example.farmmobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.example.farmmobileapp.R;
import com.example.farmmobileapp.adapters.ProductAdapter;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.models.User;
import com.example.farmmobileapp.models.ApiResponse;
import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.utils.ErrorHandler;
import com.example.farmmobileapp.utils.ImageUtils;
import com.example.farmmobileapp.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.hdodenhof.circleimageview.CircleImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyProductsActivity extends AppCompatActivity implements ProductAdapter.OnProductActionListener {
    private static final String TAG = "FarmApp:MyProducts";

    private RecyclerView recyclerViewProducts;
    private ProgressBar progressBar;
    private TextView tvEmptyView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddProduct;
    private CircleImageView imageViewProfile;
    private ProductAdapter productAdapter;
    private List<Product> productsList = new ArrayList<>();
    private ApiService apiService;
    private SessionManager sessionManager;
    private boolean productsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_products);
        Log.d(TAG, "onCreate: Activity started");

        // Configure Glide
        Glide.with(this)
            .setDefaultRequestOptions(
                new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
            );

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();
        loadProfileImage();
        loadProducts();
    }

    private void initViews() {
        Log.d(TAG, "initViews: Initializing views");
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyView = findViewById(R.id.tvEmptyView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        imageViewProfile = findViewById(R.id.imageViewProfile);

        sessionManager = SessionManager.getInstance(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
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

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this, productsList, false);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadProducts);
    }

    private void setupFab() {
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddProductActivity.class);
            startActivity(intent);
        });
    }

    private void loadProducts() {
        Log.d(TAG, "loadProducts: Starting to load products");
        showProgress();
        String authHeader = sessionManager.getAuthHeaderValue();
        
        if (authHeader == null) {
            Log.e(TAG, "loadProducts: No auth header available");
            showError("Authentication required");
            return;
        }

        apiService.getCurrentFarmerProducts(authHeader).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                hideProgress();
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    Log.d(TAG, "loadProducts: Successfully loaded " + products.size() + " products");
                    productsList.clear();
                    productsList.addAll(products);
                    productAdapter.notifyDataSetChanged();
                    updateEmptyView();
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
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(TAG, "loadProducts: Network error", t);
                hideProgress();
                showError("Network error: " + t.getMessage());
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
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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


    public void onProductClick(Product product) {
        // Not used in my products view
    }

    @Override
    public void onEditClick(Product product) {
        Intent intent = new Intent(this, EditProductActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Product product) {
        String authHeader = sessionManager.getAuthHeaderValue();
        if (authHeader == null) {
            showError("Authentication required");
            return;
        }

        showProgress();
        apiService.deleteProduct(authHeader, product.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MyProductsActivity.this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                    loadProducts(); // Refresh the product list
                } else {
                    String errorMessage = "Failed to delete product";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(MyProductsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MyProductsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!productsLoaded) {
            loadProducts();
            productsLoaded = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear any pending callbacks
        if (apiService != null) {
            // Cancel any pending requests
            // Note: This is a placeholder as Retrofit doesn't provide direct request cancellation
            // You might want to implement a custom solution if needed
        }
        
        // Clear the adapter
        if (productAdapter != null) {
            productAdapter = null;
        }
        
        // Clear the list
        if (productsList != null) {
            productsList.clear();
        }
        
        // Clear Glide cache for this activity
        Glide.get(this).clearMemory();
    }
}