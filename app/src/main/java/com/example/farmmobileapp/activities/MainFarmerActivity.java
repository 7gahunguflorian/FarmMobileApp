package com.example.farmmobileapp.activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFarmerActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private Button buttonMyProducts, buttonAvailableProducts;
    private ImageView imageViewProfile;
    private OrderAdapter orderAdapter;
    private List<Order> ordersList = new ArrayList<>();
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_farmer);

        initViews();
        setupRecyclerView();
        setupButtons();
        loadUserProfile();
        loadOrders();
    }

    private void initViews() {
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        buttonMyProducts = findViewById(R.id.buttonMyProducts);
        buttonAvailableProducts = findViewById(R.id.buttonAvailableProducts);
        imageViewProfile = findViewById(R.id.imageViewProfile);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(ordersList, true); // true for farmer view
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(orderAdapter);

        // Set item click listener for the adapter
        orderAdapter.setOnOrderStatusChangeListener(new OrderAdapter.OnOrderStatusChangeListener() {
            @Override
            public void onOrderStatusChange(Order order, String newStatus) {
                updateOrderStatus(order.getId(), newStatus);
            }
        });
    }

    private void setupButtons() {
        buttonMyProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainFarmerActivity.this, FarmerProductsActivity.class);
                startActivity(intent);
            }
        });

        buttonAvailableProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainFarmerActivity.this, AvailableProductsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadUserProfile() {
        Call<User> call = apiService.getCurrentUser("Bearer " + sessionManager.getToken());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    // Load profile image if available
                    if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                        Glide.with(MainFarmerActivity.this)
                                .load(RetrofitClient.BASE_URL + user.getProfileImageUrl())
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile)
                                .into(imageViewProfile);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(MainFarmerActivity.this, "Failed to load profile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrders() {
        // This endpoint would need to be added to the backend
        Call<List<Order>> call = apiService.getFarmerOrders("Bearer " + sessionManager.getToken());
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ordersList.clear();
                    ordersList.addAll(response.body());
                    orderAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainFarmerActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Toast.makeText(MainFarmerActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderStatus(long orderId, String newStatus) {
        OrderStatusRequest request = new OrderStatusRequest(newStatus);
        Call<Order> call = apiService.updateOrderStatus("Bearer " + sessionManager.getToken(), orderId, request);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainFarmerActivity.this, "Order status updated", Toast.LENGTH_SHORT).show();
                    loadOrders(); // Refresh the orders list
                } else {
                    Toast.makeText(MainFarmerActivity.this, "Failed to update order status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Toast.makeText(MainFarmerActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders(); // Refresh orders when coming back to this activity
    }
}