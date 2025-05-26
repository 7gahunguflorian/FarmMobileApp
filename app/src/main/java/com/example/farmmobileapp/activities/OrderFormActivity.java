package com.example.farmmobileapp.activities;

import com.example.farmmobileapp.R;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.models.Order;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.models.ApiResponse;
import com.example.farmmobileapp.utils.Constants;
import com.example.farmmobileapp.utils.ErrorHandler;
import com.example.farmmobileapp.utils.ImageUtils;
import com.example.farmmobileapp.utils.SessionManager;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.Locale;
import java.io.IOException;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderFormActivity extends AppCompatActivity {
    private static final String TAG = "OrderFormActivity";

    private TextView tvProductName, tvProductPrice, tvFarmerName, tvProductDescription;
    private TextView tvTotalPrice, tvAvailableQuantity;
    private ImageView imgProduct;
    private TextInputLayout tilQuantity, tilDeliveryLocation;
    private TextInputEditText etQuantity, etDeliveryLocation;
    private Button btnPlaceOrder;
    private ProgressBar progressBar;

    private Product product;
    private ApiService apiService;
    private SessionManager sessionManager;
    private Long productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_order_form);
            Log.d(TAG, "onCreate: Setting up OrderFormActivity");

            // Initialize views
            initViews();

            // Set up toolbar
            setupToolbar();

            // Initialize API service and session manager
            apiService = RetrofitClient.getClient().create(ApiService.class);
            sessionManager = SessionManager.getInstance(this);

            // Get product ID from intent
            String productIdString = getIntent().getStringExtra(Constants.EXTRA_PRODUCT_ID);
            if (productIdString != null) {
                try {
                    productId = Long.parseLong(productIdString);
                    Log.d(TAG, "onCreate: Loading product with ID: " + productId);
                    loadProductDetails(productId);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "onCreate: Invalid product ID format", e);
                    Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Log.e(TAG, "onCreate: No product ID provided in intent");
                Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                finish();
            }
            // Set up click listeners
            setupClickListeners();
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Error initializing activity", e);
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            Log.d(TAG, "initViews: Initializing views");
            tvProductName = findViewById(R.id.textViewProductName);
            tvProductPrice = findViewById(R.id.textViewPricePerUnit);
            tvFarmerName = findViewById(R.id.textViewFarmerName);
            tvProductDescription = findViewById(R.id.textViewDescription);
            tvTotalPrice = findViewById(R.id.textViewTotalPrice);
            tvAvailableQuantity = findViewById(R.id.textViewAvailableQty);
            imgProduct = findViewById(R.id.imageViewProduct);
            
            // Initialize TextInputLayouts
            tilQuantity = findViewById(R.id.tilQuantity);
            tilDeliveryLocation = findViewById(R.id.tilDeliveryLocation);
            
            // Initialize TextInputEditTexts
            etQuantity = findViewById(R.id.editTextQuantity);
            etDeliveryLocation = findViewById(R.id.editTextDeliveryLocation);
            
            btnPlaceOrder = findViewById(R.id.buttonPayAndOrder);
            progressBar = findViewById(R.id.progressBar);

            // Verify all views are initialized
            if (tvProductName == null || tvProductPrice == null || tvFarmerName == null || 
                tvProductDescription == null || tvTotalPrice == null || tvAvailableQuantity == null || 
                imgProduct == null || tilQuantity == null || tilDeliveryLocation == null || 
                etQuantity == null || etDeliveryLocation == null || btnPlaceOrder == null || 
                progressBar == null) {
                throw new IllegalStateException("One or more views failed to initialize");
            }
            
            Log.d(TAG, "initViews: All views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "initViews: Error initializing views", e);
            throw new RuntimeException("Failed to initialize views: " + e.getMessage(), e);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Place Order");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        btnPlaceOrder.setOnClickListener(v -> {
            if (validateInputs()) {
                placeOrder();
            }
        });

        // Update total price when quantity changes
        etQuantity.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateTotalPrice();
            }
        });

        // Add text change listener for real-time total price updates
        etQuantity.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                updateTotalPrice();
            }
        });
    }

    private void loadProductDetails(Long productId) {
        String authHeader = sessionManager.getAuthHeaderValue();
        if (authHeader == null) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress();
        apiService.getProductById(authHeader, productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                hideProgress();
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body();
                    populateFields(product);
                } else {
                    String errorMessage = "Failed to load product details";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(OrderFormActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                hideProgress();
                Toast.makeText(OrderFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields(Product product) {
        try {
            if (product == null) {
                Log.e(TAG, "populateFields: Product is null");
                return;
            }

            Log.d(TAG, "populateFields: Populating fields");
            tvProductName.setText(product.getName());
            tvProductPrice.setText(String.format(Locale.getDefault(), Constants.CURRENCY_FORMAT, product.getPrice()));
            tvFarmerName.setText("Farmer: " + (product.getOwner() != null ? product.getOwner().getUsername() : "Unknown"));
            tvProductDescription.setText(product.getDescription());
            tvAvailableQuantity.setText("Available: " + product.getAvailableQuantity() + " units");

            // Load product image
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                ImageUtils.loadImage(this, product.getImageUrl(), imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.placeholder_product);
            }

            // Set default quantity
            etQuantity.setText("1");
            updateTotalPrice();
            Log.d(TAG, "populateFields: Product details populated successfully");
        } catch (Exception e) {
            Log.e(TAG, "populateFields: Error populating fields", e);
            Toast.makeText(this, "Error populating product details", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs() {
        String quantityStr = etQuantity.getText() != null ? etQuantity.getText().toString().trim() : "";
        String deliveryLocation = etDeliveryLocation.getText() != null ? etDeliveryLocation.getText().toString().trim() : "";

        if (quantityStr.isEmpty()) {
            tilQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return false;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            tilQuantity.setError("Invalid quantity");
            etQuantity.requestFocus();
            return false;
        }
        if (quantity < Constants.MIN_ORDER_QUANTITY) {
            tilQuantity.setError("Minimum quantity is " + Constants.MIN_ORDER_QUANTITY);
            etQuantity.requestFocus();
            return false;
        }

        if (quantity > Constants.MAX_ORDER_QUANTITY) {
            tilQuantity.setError("Maximum quantity is " + Constants.MAX_ORDER_QUANTITY);
            etQuantity.requestFocus();
            return false;
        }

        if (product != null && quantity > product.getAvailableQuantity()) {
            tilQuantity.setError("Only " + product.getAvailableQuantity() + " units available");
            etQuantity.requestFocus();
            return false;
        }

        if (deliveryLocation.isEmpty()) {
            tilDeliveryLocation.setError("Delivery location is required");
            etDeliveryLocation.requestFocus();
            return false;
        }

        if (deliveryLocation.length() < 5) {
            tilDeliveryLocation.setError("Please provide a more detailed delivery location");
            etDeliveryLocation.requestFocus();
            return false;
        }

        // Clear any previous errors
        tilQuantity.setError(null);
        tilDeliveryLocation.setError(null);
        return true;
    }

    private void updateTotalPrice() {
        if (product != null) {
            String quantityStr = etQuantity.getText() != null ? etQuantity.getText().toString().trim() : "";
            if (!quantityStr.isEmpty()) {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                    tvTotalPrice.setText(String.format(Locale.getDefault(),
                            "Total: " + Constants.CURRENCY_FORMAT, totalPrice));
                } catch (NumberFormatException e) {
                    tvTotalPrice.setText("Total: $0.00");
                }
            } else {
                tvTotalPrice.setText("Total: $0.00");
            }
        }
    }

    private void placeOrder() {
        String authHeader = sessionManager.getAuthHeaderValue();
        if (authHeader == null) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get input values
        String quantityStr = etQuantity.getText().toString().trim();
        String deliveryLocation = etDeliveryLocation.getText().toString().trim();

        if (quantityStr.isEmpty() || deliveryLocation.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Double quantity = Double.parseDouble(quantityStr);
            if (quantity <= 0) {
                Toast.makeText(this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create order object
            Order order = new Order();
            order.setProductId(productId);
            order.setQuantity(quantity);
            order.setTotalPrice(quantity * product.getPrice().doubleValue());
            order.setDeliveryLocation(deliveryLocation);
            order.setStatus("PENDING");

            showProgress();
            apiService.createOrder(authHeader, order).enqueue(new Callback<ApiResponse<Order>>() {
                @Override
                public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                    hideProgress();
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<Order> apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            Toast.makeText(OrderFormActivity.this, "Order placed successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String errorMessage = apiResponse.getMessage() != null ? 
                                apiResponse.getMessage() : "Failed to place order";
                            Toast.makeText(OrderFormActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        ErrorHandler.handleApiError(OrderFormActivity.this, response);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                    hideProgress();
                    Toast.makeText(OrderFormActivity.this, 
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity format", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        btnPlaceOrder.setEnabled(false);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
        btnPlaceOrder.setEnabled(true);
    }
}


