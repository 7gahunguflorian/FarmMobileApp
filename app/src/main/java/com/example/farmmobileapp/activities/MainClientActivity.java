package com.example.farmmobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.farmmobileapp.R;
import com.example.farmmobileapp.adapters.ProductAdapter;
import com.example.farmmobileapp.decorations.GridSpacingItemDecoration;
import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.utils.Constants;
import com.example.farmmobileapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainClientActivity extends AppCompatActivity implements ProductAdapter.OnProductActionListener {

    private RecyclerView recyclerProducts;
    private ProgressBar progressBar;
    private TextView tvEmptyView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private CircleImageView imgProfile;

    private ProductAdapter productAdapter;
    private List<Product> productList;
    private List<Product> filteredProductList;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_client);

        // Initialize API service and session manager
        apiService = RetrofitClient.getClient().create(ApiService.class);
        sessionManager = SessionManager.getInstance(this);

        // Initialize views
        initViews();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set up product grid
        setupProductGrid();

        // Set up search functionality
        setupSearch();

        // Set up swipe to refresh
        setupSwipeRefresh();

        // Load products
        loadProducts();
    }

    private void initViews() {
        recyclerProducts = findViewById(R.id.gridProducts); // Keep same ID in layout file
        progressBar = findViewById(R.id.progressBar);
        tvEmptyView = findViewById(R.id.tvEmptyView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        searchView = findViewById(R.id.searchView);
        imgProfile = findViewById(R.id.imgProfile);

        // Set up profile image click
        imgProfile.setOnClickListener(v -> {
            // Open profile or orders activity
            Toast.makeText(MainClientActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupProductGrid() {
        productList = new ArrayList<>();
        filteredProductList = new ArrayList<>();

        productAdapter = new ProductAdapter(this, filteredProductList);
        productAdapter.setOnProductActionListener(this);

        // Set GridLayoutManager with 2 columns and spacing
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerProducts.setLayoutManager(layoutManager);

        // Add item decoration for spacing
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        recyclerProducts.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));
        recyclerProducts.setAdapter(productAdapter);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return true;
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> loadProducts());
    }

    private void loadProducts() {
        showLoading(true);

        apiService.getAllProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());

                    filteredProductList.clear();
                    filteredProductList.addAll(productList);

                    productAdapter.notifyDataSetChanged();

                    if (filteredProductList.isEmpty()) {
                        showEmptyView(true);
                    } else {
                        showEmptyView(false);
                    }
                } else {
                    Toast.makeText(MainClientActivity.this,
                            "Error loading products: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainClientActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProducts(String query) {
        filteredProductList.clear();

        if (query.isEmpty()) {
            filteredProductList.addAll(productList);
        } else {
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(query.toLowerCase()) ||
                        product.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                        (product.getFarmerName() != null &&
                                product.getFarmerName().toLowerCase().contains(query.toLowerCase()))) {
                    filteredProductList.add(product);
                }
            }
        }

        productAdapter.notifyDataSetChanged();

        if (filteredProductList.isEmpty()) {
            showEmptyView(true);
        } else {
            showEmptyView(false);
        }
    }

    private void openOrderForm(Product product) {
        Intent intent = new Intent(MainClientActivity.this, OrderFormActivity.class);
        intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.getId().toString());
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerProducts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyView(boolean show) {
        tvEmptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerProducts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // Implement ProductAdapter.OnProductActionListener methods
    @Override
    public void onEditProduct(Product product) {
        // Not used in client view
    }

    @Override
    public void onDeleteProduct(Product product) {
        // Not used in client view
    }

    @Override
    public void onOrderProduct(Product product) {
        openOrderForm(product);
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
        loadProducts();
    }
}