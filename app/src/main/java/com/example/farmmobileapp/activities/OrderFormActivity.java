package com.example.farmmobileapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.farmmobileapp.utils.Constants;
import com.example.farmmobileapp.utils.ImageUtils;
import com.example.farmmobileapp.utils.SessionManager;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class OrderFormActivity extends AppCompatActivity {

    private TextView tvProductName, tvProductPrice, tvFarmerName, tvProductDescription;
    private TextView tvTotalPrice, tvAvailableQuantity;
    private ImageView imgProduct;
    private EditText etQuantity, etDeliveryLocation;
    private Button btnPlaceOrder;
    private ProgressBar progressBar;

    private Product product;
    private ApiService apiService;
    private SessionManager sessionManager;
    private Long productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_form);

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
                loadProductDetails();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Set up click listeners
        setupClickListeners();
    }
    private void initViews() {
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvFarmerName = findViewById(R.id.tvFarmerName);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvAvailableQuantity = findViewById(R.id.tvAvailableQuantity);
        imgProduct = findViewById(R.id.imgProduct);
        etQuantity = findViewById(R.id.etQuantity);
        etDeliveryLocation = findViewById(R.id.etDeliveryLocation);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        progressBar = findViewById(R.id.progressBar);
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
    }
    private void loadProductDetails() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getProductById(productId).enqueue(new Callback<Product>() {
            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderFormActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    private void displayProductDetails() {
        tvProductName.setText(product.getName());
        tvProductPrice.setText(String.format(Locale.getDefault(), Constants.CURRENCY_FORMAT, product.getPrice()));
        tvFarmerName.setText("Farmer: " + (product.getFarmerName() != null ? product.getFarmerName() : "Unknown"));
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
    }

    private void updateTotalPrice() {
        if (product != null) {
            String quantityStr = etQuantity.getText().toString().trim();
            if (!quantityStr.isEmpty()) {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    double totalPrice = product.getPrice() * quantity;
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

    private boolean validateInputs() {
        String quantityStr = etQuantity.getText().toString().trim();
        String deliveryLocation = etDeliveryLocation.getText().toString().trim();

        if (quantityStr.isEmpty()) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return false;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            etQuantity.setError("Invalid quantity");
            etQuantity.requestFocus();
            return false;
        }
        if (quantity < Constants.MIN_ORDER_QUANTITY) {
            etQuantity.setError("Minimum quantity is " + Constants.MIN_ORDER_QUANTITY);
            etQuantity.requestFocus();
            return false;
        }

        if (quantity > Constants.MAX_ORDER_QUANTITY) {
            etQuantity.setError("Maximum quantity is " + Constants.MAX_ORDER_QUANTITY);
            etQuantity.requestFocus();
            return false;
        }

        if (product != null && quantity > product.getAvailableQuantity()) {
            etQuantity.setError("Only " + product.getAvailableQuantity() + " units available");
            etQuantity.requestFocus();
            return false;
        }

        if (deliveryLocation.isEmpty()) {
            etDeliveryLocation.setError("Delivery location is required");
            etDeliveryLocation.requestFocus();
            return false;
        }

        if (deliveryLocation.length() < 5) {
            etDeliveryLocation.setError("Please provide a more detailed delivery location");
            etDeliveryLocation.requestFocus();
            return false;
        }

        return true;
    }

    private void placeOrder() {
        progressBar.setVisibility(View.VISIBLE);
        btnPlaceOrder.setEnabled(false);

        String quantityStr = etQuantity.getText().toString().trim();
        String deliveryLocation = etDeliveryLocation.getText().toString().trim();

        int quantity = Integer.parseInt(quantityStr);
        double totalPrice = product.getPrice() * quantity;

        // Create order object
        Order order = new Order(productId, quantity, totalPrice, deliveryLocation);
        order.setProductName(product.getName());
        order.setProductImageUrl(product.getImageUrl());
        order.setFarmerId(product.getFarmerId());
        order.setFarmerName(product.getFarmerName());

        apiService.createOrder(sessionManager.getAuthHeaderValue(), order)
                .enqueue(new Callback<Order>() {
                    @Override
                    public void onResponse(Call<Order> call, Response<Order> response) {
                        progressBar.setVisibility(View.GONE);
                        btnPlaceOrder.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(OrderFormActivity.this,
                                    "Order placed successfully!",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            String errorMessage = "Failed to place order";
                            if (response.code() == Constants.HTTP_BAD_REQUEST) {
                                errorMessage = "Invalid order data";
                            } else if (response.code() == Constants.HTTP_UNAUTHORIZED) {
                                errorMessage = "Please login again";
                                sessionManager.logout();
                            } else if (response.code() == Constants.HTTP_CONFLICT) {
                                errorMessage = "Product is no longer available";
                            }
                            Toast.makeText(OrderFormActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Order> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnPlaceOrder.setEnabled(true);
                        Toast.makeText(OrderFormActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
//public void onResponse(Call<Product> call, Response<Product> response) {
//    progressBar.setVisibility(View.GONE);
//
//    if (response.isSuccessful() && response.body() != null) {
//        product = response.body();
//        displayProductDetails();
//    } else {
//        Toast.makeText(OrderFormActivity.this,
//                "Failed to load product details: " + response.code(),
//                Toast.LENGTH_SHORT).show();
//        finish();
//    }
//}
//
//@Override


