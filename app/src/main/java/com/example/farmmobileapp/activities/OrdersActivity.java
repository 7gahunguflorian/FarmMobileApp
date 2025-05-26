package com.example.farmmobileapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmmobileapp.R;
import com.example.farmmobileapp.adapters.OrderAdapter;
import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.models.Order;
import com.example.farmmobileapp.models.User;
import com.example.farmmobileapp.utils.SessionManager;
import com.example.farmmobileapp.utils.ErrorHandler;
import com.example.farmmobileapp.models.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersActivity extends AppCompatActivity {
    private static final String TAG = "OrdersActivity";

    private RecyclerView recyclerViewOrders;
    private TextView textViewEmpty;
    private ProgressBar progressBar;
    private OrderAdapter orderAdapter;
    private List<Order> ordersList = new ArrayList<>();
    private ApiService apiService;
    private SessionManager sessionManager;
    private boolean isFarmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        // Initialize views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Initialize API service and session manager
        apiService = RetrofitClient.getClient().create(ApiService.class);
        sessionManager = SessionManager.getInstance(this);

        // Get user type from session
        User currentUser = sessionManager.getUser();
        isFarmer = currentUser != null && "FARMER".equals(currentUser.getRole());

        // Setup RecyclerView
        setupRecyclerView();

        // Load orders
        loadOrders();
    }

    private void initViews() {
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(isFarmer ? "My Orders" : "My Orders");
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, ordersList, isFarmer);
        orderAdapter.setOnOrderStatusChangeListener(this::updateOrderStatus);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        String authHeader = sessionManager.getAuthHeaderValue();
        if (authHeader == null) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress();
        apiService.getClientOrders(authHeader, 0, 20).enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                hideProgress();
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Order>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        ordersList.clear();
                        ordersList.addAll(apiResponse.getData());
                        orderAdapter.notifyDataSetChanged();
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? 
                            apiResponse.getMessage() : "Failed to load orders";
                        Toast.makeText(OrdersActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ErrorHandler.handleApiError(OrdersActivity.this, response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                hideProgress();
                Toast.makeText(OrdersActivity.this, 
                    "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderStatus(Order order, String newStatus) {
        String authHeader = sessionManager.getAuthHeaderValue();
        if (authHeader == null) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showProgress();
        apiService.updateOrderStatus(authHeader, order.getId(), order).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                hideProgress();
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Order> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(OrdersActivity.this, "Order status updated", Toast.LENGTH_SHORT).show();
                        loadOrders(); // Reload orders to reflect changes
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? 
                            apiResponse.getMessage() : "Failed to update order status";
                        Toast.makeText(OrdersActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ErrorHandler.handleApiError(OrdersActivity.this, response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                hideProgress();
                Toast.makeText(OrdersActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewOrders.setVisibility(View.GONE);
        textViewEmpty.setVisibility(View.GONE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    private void showEmptyView() {
        textViewEmpty.setVisibility(View.VISIBLE);
        recyclerViewOrders.setVisibility(View.GONE);
    }

    private void hideEmptyView() {
        textViewEmpty.setVisibility(View.GONE);
        recyclerViewOrders.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 