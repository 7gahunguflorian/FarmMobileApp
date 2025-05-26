package com.example.farmmobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.farmmobileapp.R;
import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.models.AuthRequest;
import com.example.farmmobileapp.models.AuthResponse;
import com.example.farmmobileapp.models.ApiResponse;
import com.example.farmmobileapp.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        initViews();

        // Initialize API service and session manager
        apiService = RetrofitClient.getClient().create(ApiService.class);
        sessionManager = SessionManager.getInstance(this);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateBasedOnRole();
            finish();
            return;
        }

        // Set up click listeners
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // Login button click listener
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInputs(username, password)) {
                progressBar.setVisibility(View.VISIBLE);
                btnLogin.setEnabled(false);
                loginUser(username, password);
            }
        });

        // Register text click listener
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private boolean validateInputs(String username, String password) {
        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void loginUser(String username, String password) {
        AuthRequest authRequest = new AuthRequest(username, password);
        Log.e("LoginActivity", "Attempting login for user: " + username);

        apiService.login(authRequest).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                Log.e("LoginActivity", "Response code: " + response.code());
                Log.e("LoginActivity", "Response body: " + (response.body() != null ? response.body().toString() : "null"));

                if (response.isSuccessful()) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    Log.e("LoginActivity", "API Response success: " + (apiResponse != null ? apiResponse.isSuccess() : "null"));
                    Log.e("LoginActivity", "API Response message: " + (apiResponse != null ? apiResponse.getMessage() : "null"));
                    Log.e("LoginActivity", "API Response data: " + (apiResponse != null && apiResponse.getData() != null ? apiResponse.getData().toString() : "null"));
                    
                    // If response is null, try to parse error body
                    if (apiResponse == null) {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : null;
                            Log.e("LoginActivity", "Error body: " + errorBody);
                            Toast.makeText(LoginActivity.this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                            return;
                        } catch (Exception e) {
                            Log.e("LoginActivity", "Error reading error body", e);
                            Toast.makeText(LoginActivity.this, "Error processing server response", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // If success is false but we have data, still try to use it
                    if (!apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Log.e("LoginActivity", "Success is false but data is present, attempting to use data");
                    } else if (!apiResponse.isSuccess() || apiResponse.getData() == null) {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Login failed";
                        Log.e("LoginActivity", "Login failed: " + errorMsg);
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AuthResponse authResponse = apiResponse.getData();
                    
                    // Validate response data
                    if (authResponse.getToken() == null || authResponse.getToken().isEmpty() || authResponse.getToken().equals("present")) {
                        Log.e("LoginActivity", "Invalid token in response");
                        Toast.makeText(LoginActivity.this, "Invalid authentication response", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (authResponse.getUser() == null) {
                        Log.e("LoginActivity", "No user data in response");
                        Toast.makeText(LoginActivity.this, "User data not received", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Save token and user information
                    Log.d("LoginActivity", "Saving token: " + authResponse.getToken());
                    sessionManager.saveAuthToken(authResponse.getToken());
                    sessionManager.saveUser(authResponse.getUser());

                    // Verify the data was saved correctly
                    if (!sessionManager.isLoggedIn()) {
                        Log.e("LoginActivity", "Failed to save login data to session");
                        Toast.makeText(LoginActivity.this, "Failed to save login data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.e("LoginActivity", "Login successful, navigating to role-based activity");
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    // Navigate based on user role
                    navigateBasedOnRole();
                    finish();
                } else {
                    String errorMessage = "Login failed";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                            Log.e("LoginActivity", "Error response body: " + errorMessage);
                        }
                    } catch (Exception e) {
                        Log.e("LoginActivity", "Error reading error body", e);
                        if (response.code() == 401) {
                            errorMessage = "Invalid username or password";
                        } else if (response.code() == 400) {
                            errorMessage = "Please check your credentials";
                        } else if (response.code() >= 500) {
                            errorMessage = "Server error. Please try again later";
                        }
                    }
                    Log.e("LoginActivity", "Login failed with message: " + errorMessage);
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                String errorMessage = "Network error";
                if (t.getMessage() != null) {
                    if (t.getMessage().contains("Failed to connect")) {
                        errorMessage = "Cannot connect to server. Please check your internet connection.";
                    } else {
                        errorMessage = "Network error: " + t.getMessage();
                    }
                }
                Log.e("LoginActivity", "Network error during login", t);
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateBasedOnRole() {
        if (sessionManager.getUser() != null) {
            String role = sessionManager.getUser().getRole();

            if ("FARMER".equals(role)) {
                startActivity(new Intent(LoginActivity.this, MainFarmerActivity.class));
            } else if ("CLIENT".equals(role)) {
                startActivity(new Intent(LoginActivity.this, MainClientActivity.class));
            } else {
                Toast.makeText(this, "Unknown user role", Toast.LENGTH_SHORT).show();
                sessionManager.logout();
            }
        } else {
            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
            sessionManager.logout();
        }
    }
}