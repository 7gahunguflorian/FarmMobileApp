package com.example.farmmobileapp.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.farmmobileapp.OrderFormActivity;
import com.example.farmmobileapp.R;
import com.example.farmmobileapp.adapters.ProductAdapter;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.utils.Constants;
import com.example.farmmobileapp.utils.UserManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainClientActivity extends AppCompatActivity {

    private GridView gridProducts;
    private ProgressBar progressBar;
    private TextView tvEmptyView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private CircleImageView imgProfile;

    private ProductAdapter productAdapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_client);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userManager = UserManager.getInstance();

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
        gridProducts = findViewById(R.id.gridProducts);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyView = findViewById(R.id.tvEmptyView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        searchView = findViewById(R.id.searchView);
        imgProfile = findViewById(R.id.imgProfile);

        // Set up profile image click
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open profile or orders activity
                // Intent intent = new Intent(MainClientActivity.this, ProfileActivity.class);
                // startActivity(intent);
                Toast.makeText(MainClientActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupProductGrid() {
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList);
        gridProducts.setAdapter(productAdapter);

        gridProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product selectedProduct = productList.get(position);
                openOrderForm(selectedProduct);
            }
        });
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
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadProducts();
            }
        });
    }

    private void loadProducts() {
        showLoading(true);

        db.collection(Constants.COLLECTION_PRODUCTS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            productList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Product product = document.toObject(Product.class);
                                product.setId(document.getId());
                                productList.add(product);
                            }

                            productAdapter.notifyDataSetChanged();

                            if (productList.isEmpty()) {
                                showEmptyView(true);
                            } else {
                                showEmptyView(false);
                            }
                        } else {
                            Toast.makeText(MainClientActivity.this, "Error loading products: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        showLoading(false);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void filterProducts(String query) {
        if (query.isEmpty()) {
            productAdapter = new ProductAdapter(this, productList);
        } else {
            List<Product> filteredList = new ArrayList<>();
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(query.toLowerCase()) ||
                        product.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                        product.getFarmerName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }
            productAdapter = new ProductAdapter(this, filteredList);
        }

        gridProducts.setAdapter(productAdapter);
    }

    private void openOrderForm(Product product) {
        Intent intent = new Intent(MainClientActivity.this, OrderFormActivity.class);
        intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        gridProducts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyView(boolean show) {
        tvEmptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        gridProducts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_my_orders) {
            // Open orders activity
            // Intent intent = new Intent(MainClientActivity.this, ClientOrdersActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "My Orders clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_logout) {
            // Log out
            userManager.logout();
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