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
import com.example.farmmobileapp.adapters.FarmerOrderAdapter;
import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.models.Order;
import com.example.farmmobileapp.models.User;
import com.example.farmmobileapp.models.ApiResponse;
import com.example.farmmobileapp.utils.SessionManager;
import com.example.farmmobileapp.utils.ErrorHandler;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFarmerActivity extends AppCompatActivity {

    private static final String TAG = "MainFarmerActivity";

    private RecyclerView recyclerViewOrders;
    private Button buttonMyProducts, buttonAvailableProducts, buttonLogout;
    private ImageView imageViewProfile;
    private FarmerOrderAdapter orderAdapter;
    private List<Order> ordersList = new ArrayList<>();
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_farmer);

        try {
            initViews();
            setupRecyclerView();
            setupButtons();
            loadUserProfile();
            loadOrders();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Fallback - go back to login
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void initViews() {
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        buttonMyProducts = findViewById(R.id.buttonMyProducts);
        buttonAvailableProducts = findViewById(R.id.buttonAvailableProducts);
        imageViewProfile = findViewById(R.id.imageViewProfile);

        // Initialize SessionManager and ApiService first
        sessionManager = SessionManager.getInstance(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Try to find logout button, but don't crash if it doesn't exist
        try {
            buttonLogout = findViewById(R.id.buttonLogout);
            if (buttonLogout == null) {
                Log.w(TAG, "Logout button not found in layout - you may need to add it to your XML");
            }
        } catch (Exception e) {
            Log.w(TAG, "Error finding logout button: " + e.getMessage());
            buttonLogout = null;
        }

        // Verify required views exist
        if (recyclerViewOrders == null) {
            throw new RuntimeException("recyclerViewOrders not found - check your layout file");
        }
        if (buttonMyProducts == null) {
            throw new RuntimeException("buttonMyProducts not found - check your layout file");
        }
        if (buttonAvailableProducts == null) {
            throw new RuntimeException("buttonAvailableProducts not found - check your layout file");
        }
        if (imageViewProfile == null) {
            throw new RuntimeException("imageViewProfile not found - check your layout file");
        }
    }

    private void setupRecyclerView() {
        orderAdapter = new FarmerOrderAdapter(this, ordersList);
        orderAdapter.setOnOrderStatusChangeListener((order, newStatus) -> {
            updateOrderStatus(order.getId(), newStatus);
        });
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void setupButtons() {
        buttonMyProducts.setOnClickListener(v -> {
            try {
                Log.d(TAG, "Attempting to open MyProductsActivity");
                Intent intent = new Intent(MainFarmerActivity.this, MyProductsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error opening My Products", e);
                Toast.makeText(this, "Error opening My Products: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        buttonAvailableProducts.setOnClickListener(v -> {
            try {
                Log.d(TAG, "Attempting to open AvailableProductsActivity");
                Intent intent = new Intent(MainFarmerActivity.this, AvailableProductsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error opening Available Products", e);
                Toast.makeText(this, "Error opening Available Products: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Only set up logout button if it exists
        if (buttonLogout != null) {
            buttonLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logoutUser();
                }
            });
        } else {
            Log.w(TAG, "Logout button not found - skipping click listener setup");
        }
    }

    private void logoutUser() {
        try {
            sessionManager.logout();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainFarmerActivity.this, LoginActivity.class);
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
            User user = sessionManager.getUser();
            if (user != null) {
                // Load profile image if available
                if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                    String imageUrl = user.getProfileImageUrl();
                    // Replace localhost with the actual server IP
                    if (imageUrl.contains("localhost")) {
                        imageUrl = imageUrl.replace("localhost", "192.168.88.247");
                    }
                    
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(imageViewProfile);
                } else {
                    imageViewProfile.setImageResource(R.drawable.ic_profile);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user profile", e);
        }
    }

    private void showProgress() {
        // Show a loading indicator
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        // Hide the loading indicator
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    private void loadOrders() {
        String authHeader = sessionManager.getAuthHeaderValue();
        if (authHeader == null) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress();
        apiService.getFarmerOrders(authHeader, 0, 20).enqueue(new Callback<ApiResponse<List<Order>>>() {
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
                        Toast.makeText(MainFarmerActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ErrorHandler.handleApiError(MainFarmerActivity.this, response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                hideProgress();
                Toast.makeText(MainFarmerActivity.this, 
                    "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parseRawResponse(Response<List<Order>> response) {
        try {
            Gson gson = new Gson();
            String rawJson = gson.toJson(response.body());
            Log.d(TAG, "Raw JSON response: " + rawJson);

            JsonElement jsonElement = gson.fromJson(rawJson, JsonElement.class);

            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                Type orderListType = new TypeToken<List<Order>>() {
                }.getType();
                List<Order> orders = gson.fromJson(jsonArray, orderListType);

                ordersList.clear();
                ordersList.addAll(orders);
                orderAdapter.notifyDataSetChanged();

            } else if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                if (jsonObject.has("data")) {
                    JsonElement dataElement = jsonObject.get("data");
                    if (dataElement.isJsonArray()) {
                        Type orderListType = new TypeToken<List<Order>>() {
                        }.getType();
                        List<Order> orders = gson.fromJson(dataElement, orderListType);

                        ordersList.clear();
                        ordersList.addAll(orders);
                        orderAdapter.notifyDataSetChanged();
                    }
                } else if (jsonObject.has("orders")) {
                    JsonElement ordersElement = jsonObject.get("orders");
                    if (ordersElement.isJsonArray()) {
                        Type orderListType = new TypeToken<List<Order>>() {
                        }.getType();
                        List<Order> orders = gson.fromJson(ordersElement, orderListType);

                        ordersList.clear();
                        ordersList.addAll(orders);
                        orderAdapter.notifyDataSetChanged();
                    }
                } else {
                    Order order = gson.fromJson(jsonObject, Order.class);
                    ordersList.clear();
                    ordersList.add(order);
                    orderAdapter.notifyDataSetChanged();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing raw response", e);
        }
    }

    private void updateOrderStatus(Long orderId, String newStatus) {
        String authHeader = sessionManager.getAuthHeaderValue();
        if (authHeader == null) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find the order in the list
        Order existingOrder = ordersList.stream()
            .filter(order -> order.getId().equals(orderId))
            .findFirst()
            .orElse(null);

        if (existingOrder == null) {
            Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new Order object with the updated status
        Order updatedOrder = new Order();
        updatedOrder.setId(existingOrder.getId());
        updatedOrder.setProductId(existingOrder.getProductId());
        updatedOrder.setQuantity(existingOrder.getQuantity());
        updatedOrder.setTotalPrice(existingOrder.getTotalPrice());
        updatedOrder.setDeliveryLocation(existingOrder.getDeliveryLocation());
        updatedOrder.setStatus(newStatus);
        updatedOrder.setClientId(existingOrder.getClientId());
        updatedOrder.setFarmerId(existingOrder.getFarmerId());

        showProgress();
        apiService.updateOrderStatus(authHeader, orderId, updatedOrder).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                hideProgress();
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MainFarmerActivity.this, "Order status updated successfully", Toast.LENGTH_SHORT).show();
                    loadOrders(); // Refresh the orders list
                } else {
                    ErrorHandler.handleApiError(MainFarmerActivity.this, response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                hideProgress();
                Toast.makeText(MainFarmerActivity.this, 
                    "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}