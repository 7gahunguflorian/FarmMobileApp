package com.example.farmmobileapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.farmmobileapp.R;
import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.models.Order;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.models.ApiResponse;
import com.example.farmmobileapp.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.math.BigDecimal;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class CreateOrderActivity extends AppCompatActivity {
    private static final String TAG = "CreateOrderActivity";

    private ImageView imageViewProduct;
    private TextView textViewProductName;
    private TextView textViewPricePerUnit;
    private TextView textViewFarmerName;
    private TextView textViewDescription;
    private TextView textViewAvailableQty;
    private TextInputLayout tilQuantity;
    private TextInputLayout tilDeliveryLocation;
    private TextInputEditText editTextQuantity;
    private TextInputEditText editTextDeliveryLocation;
    private TextView textViewTotalPrice;
    private Button buttonPlaceOrder;
    private ProgressBar progressBar;

    private ApiService apiService;
    private SessionManager sessionManager;
    private Product currentProduct;
    private Long productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        // Initialize views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Initialize API service and session manager
        apiService = RetrofitClient.getClient().create(ApiService.class);
        sessionManager = SessionManager.getInstance(this);

        // Get product ID from intent
        productId = getIntent().getLongExtra("product_id", -1);
        if (productId == -1) {
            Toast.makeText(this, "Invalid product", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load product details
        loadProductDetails();

        // Setup click listeners
        setupClickListeners();
    }

    private void initViews() {
        imageViewProduct = findViewById(R.id.imageViewProduct);
        textViewProductName = findViewById(R.id.textViewProductName);
        textViewPricePerUnit = findViewById(R.id.textViewPricePerUnit);
        textViewFarmerName = findViewById(R.id.textViewFarmerName);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewAvailableQty = findViewById(R.id.textViewAvailableQty);
        tilQuantity = findViewById(R.id.tilQuantity);
        tilDeliveryLocation = findViewById(R.id.tilDeliveryLocation);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        editTextDeliveryLocation = findViewById(R.id.editTextDeliveryLocation);
        textViewTotalPrice = findViewById(R.id.textViewTotalPrice);
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create Order");
    }

    private void setupClickListeners() {
        editTextQuantity.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                updateTotalPrice();
            }
        });

        buttonPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void loadProductDetails() {
        String authHeader = sessionManager.getAuthHeaderValue();
        if (authHeader == null) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showProgress();
        apiService.getProductById(authHeader, productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                hideProgress();
                if (response.isSuccessful() && response.body() != null) {
                    currentProduct = response.body();
                    populateFields(currentProduct);
                } else {
                    Toast.makeText(CreateOrderActivity.this, "Failed to load product details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                hideProgress();
                Toast.makeText(CreateOrderActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields(Product product) {
        textViewProductName.setText(product.getName());
        textViewPricePerUnit.setText(String.format("FBU %.2f per unit", product.getPrice()));
        textViewFarmerName.setText("Farmer: " + product.getFarmerName());
        textViewDescription.setText(product.getDescription());
        textViewAvailableQty.setText(String.format("Available: %d units", product.getAvailableQuantity()));

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            String imageUrl = product.getImageUrl();
            // Construct the full URL with server IP
            imageUrl = "http://192.168.88.247:8180" + imageUrl;

            // Get auth token
            String authToken = sessionManager.getAuthToken();
            if (authToken != null) {
                // Create GlideUrl with auth header
                GlideUrl glideUrl = new GlideUrl(imageUrl, new LazyHeaders.Builder()
                        .addHeader("Authorization", "Bearer " + authToken)
                        .build());

                Glide.with(this)
                        .load(glideUrl)
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.placeholder_product);
            }
        } else {
            imageViewProduct.setImageResource(R.drawable.placeholder_product);
        }

        updateTotalPrice();
    }

    private void updateTotalPrice() {
        String quantityStr = editTextQuantity.getText().toString();
        if (!quantityStr.isEmpty()) {
            try {
                Double quantity = Double.parseDouble(quantityStr);
                BigDecimal price = currentProduct.getPrice();
                BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(quantity));
                textViewTotalPrice.setText(String.format("Total Price: FBU %.2f", totalPrice));
            } catch (NumberFormatException e) {
                textViewTotalPrice.setText("Total Price: FBU 0.00");
            }
        } else {
            textViewTotalPrice.setText("Total Price: FBU 0.00");
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String quantityStr = editTextQuantity.getText().toString();
        if (quantityStr.isEmpty()) {
            tilQuantity.setError("Please enter quantity");
            isValid = false;
        } else {
            try {
                Double quantity = Double.parseDouble(quantityStr);
                if (quantity <= 0) {
                    tilQuantity.setError("Quantity must be greater than 0");
                    isValid = false;
                } else if (quantity > currentProduct.getAvailableQuantity()) {
                    tilQuantity.setError("Quantity exceeds available amount");
                    isValid = false;
                } else {
                    tilQuantity.setError(null);
                }
            } catch (NumberFormatException e) {
                tilQuantity.setError("Invalid quantity");
                isValid = false;
            }
        }

        String deliveryLocation = editTextDeliveryLocation.getText().toString();
        if (deliveryLocation.isEmpty()) {
            tilDeliveryLocation.setError("Please enter delivery location");
            isValid = false;
        } else {
            tilDeliveryLocation.setError(null);
        }

        return isValid;
    }

    private void placeOrder() {
        if (!validateInputs()) {
            return;
        }

        String authHeader = sessionManager.getAuthHeaderValue();
        if (authHeader == null) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Double quantity = Double.parseDouble(editTextQuantity.getText().toString());
        BigDecimal totalPrice = currentProduct.getPrice().multiply(BigDecimal.valueOf(quantity));

        // Create order with correct field structure for backend
        Order order = new Order();
        order.setProductId(currentProduct.getId());
        order.setQuantity(quantity);
        order.setTotalPrice(totalPrice.doubleValue());
        order.setStatus("PENDING");
        order.setOrderDate(new Date());

        // Set delivery info using the corrected setter
        String deliveryAddress = editTextDeliveryLocation.getText().toString().trim();
        Log.d(TAG, "Raw delivery address from input: " + deliveryAddress);

        if (deliveryAddress.isEmpty()) {
            Toast.makeText(this, "Please enter a delivery location", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            order.setDeliveryAddress(deliveryAddress);
            order.setDeliveryStatus("PENDING");
            Log.d(TAG, "Delivery address set successfully: " + order.getDeliveryAddress());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error setting delivery address: " + e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Create product item using the new structure
        Order.ProductItem productItem = new Order.ProductItem();
        productItem.setProductId(currentProduct.getId());
        productItem.setQuantity(quantity.intValue());
        productItem.setUnitPrice(currentProduct.getPrice().doubleValue());

        // Add item to order
        List<Order.ProductItem> items = new ArrayList<>();
        items.add(productItem);
        order.setProducts(items);

        // Log the order for debugging
        String orderJson = new Gson().toJson(order);
        Log.d(TAG, "Creating order with delivery address: " + deliveryAddress);
        Log.d(TAG, "Order JSON: " + orderJson);

        showProgress();
        apiService.createOrder(authHeader, order).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                hideProgress();
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Order> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(CreateOrderActivity.this, "Order placed successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ?
                                apiResponse.getMessage() : "Failed to place order";
                        Log.e(TAG, "Order creation failed: " + errorMessage);
                        Toast.makeText(CreateOrderActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMessage = "Failed to place order";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                            Log.e(TAG, "Order creation failed with error: " + errorMessage);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(CreateOrderActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                hideProgress();
                Log.e(TAG, "Network error during order creation", t);
                Toast.makeText(CreateOrderActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        buttonPlaceOrder.setEnabled(false);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
        buttonPlaceOrder.setEnabled(true);
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