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
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void setupButtons() {
        buttonMyProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainFarmerActivity.this, MyProductsActivity.class);
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

        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainFarmerActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
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
                                        // Assume it's just the filename and construct the full URL
                                        imageUrl = RetrofitClient.BASE_URL + "/uploads/" + imageUrl;
                                    }
                                }

                                Log.d(TAG, "Loading profile image from: " + imageUrl);

                                Glide.with(MainFarmerActivity.this)
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

    private void loadOrders() {
        Log.d(TAG, "Loading farmer orders...");

        try {
            Call<List<Order>> call = apiService.getFarmerOrders(sessionManager.getAuthHeaderValue());
            call.enqueue(new Callback<List<Order>>() {
                @Override
                public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                    Log.d(TAG, "Orders response code: " + response.code());

                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                List<Order> orders = response.body();
                                Log.d(TAG, "Received " + orders.size() + " orders");

                                ordersList.clear();
                                ordersList.addAll(orders);
                                orderAdapter.notifyDataSetChanged();
                            } else {
                                parseRawResponse(response);
                            }
                        } else {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading error body", e);
                            }

                            Log.e(TAG, "Failed to load orders: " + response.code() + " - " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing orders response", e);
                    }
                }

                @Override
                public void onFailure(Call<List<Order>> call, Throwable t) {
                    Log.e(TAG, "Network error loading orders", t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initiating orders load", e);
        }
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
        try {
            Order orderUpdate = new Order();
            orderUpdate.setStatus(newStatus);

            Call<Order> call = apiService.updateOrderStatus(sessionManager.getAuthHeaderValue(), orderId, orderUpdate);
            call.enqueue(new Callback<Order>() {
                @Override
                public void onResponse(Call<Order> call, Response<Order> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(MainFarmerActivity.this, "Order status updated", Toast.LENGTH_SHORT).show();
                        loadOrders();
                    } else {
                        Log.w(TAG, "Failed to update order status: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Order> call, Throwable t) {
                    Log.e(TAG, "Error updating order", t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initiating order status update", e);
        }
    }
}