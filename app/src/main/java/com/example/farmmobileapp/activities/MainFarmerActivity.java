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
import com.example.farmmobileapp.R;
import com.example.farmmobileapp.adapters.OrderAdapter;
import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.models.Order;
import com.example.farmmobileapp.models.User;
import com.example.farmmobileapp.utils.SessionManager;

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

        sessionManager = SessionManager.getInstance(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, ordersList);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void setupButtons() {
        buttonMyProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent intent = new Intent(MainFarmerActivity.this, FarmerProductsActivity.class);
                // startActivity(intent);
                Toast.makeText(MainFarmerActivity.this, "My Products clicked", Toast.LENGTH_SHORT).show();
            }
        });

        buttonAvailableProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent intent = new Intent(MainFarmerActivity.this, AvailableProductsActivity.class);
                // startActivity(intent);
                Toast.makeText(MainFarmerActivity.this, "Available Products clicked", Toast.LENGTH_SHORT).show();
            }
        });

        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open profile activity
                Toast.makeText(MainFarmerActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserProfile() {
        Call<User> call = apiService.getCurrentUser(sessionManager.getAuthHeaderValue());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    // Update user in session
                    sessionManager.saveUser(user);

                    // Load profile image if available
                    if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                        String imageUrl = user.getProfileImageUrl();
                        if (!imageUrl.startsWith("http")) {
                            imageUrl = RetrofitClient.BASE_URL + "images/" + imageUrl;
                        }

                        Glide.with(MainFarmerActivity.this)
                                .load(imageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(imageViewProfile);
                    }
                } else {
                    Toast.makeText(MainFarmerActivity.this,
                            "Failed to load profile: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(MainFarmerActivity.this,
                        "Failed to load profile: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrders() {
        // This endpoint would need to be implemented on your backend
        Call<List<Order>> call = apiService.getFarmerOrders(sessionManager.getAuthHeaderValue());
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ordersList.clear();
                    ordersList.addAll(response.body());
                    orderAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainFarmerActivity.this,
                            "Failed to load orders: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Toast.makeText(MainFarmerActivity.this,
                        "Error loading orders: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderStatus(Long orderId, String newStatus) {
        Order orderUpdate = new Order();
        orderUpdate.setStatus(newStatus);

        Call<Order> call = apiService.updateOrderStatus(sessionManager.getAuthHeaderValue(), orderId, orderUpdate);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainFarmerActivity.this, "Order status updated", Toast.LENGTH_SHORT).show();
                    loadOrders(); // Refresh the orders list
                } else {
                    Toast.makeText(MainFarmerActivity.this,
                            "Failed to update order status: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Toast.makeText(MainFarmerActivity.this,
                        "Error updating order: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders(); // Refresh orders when coming back to this activity
    }
}