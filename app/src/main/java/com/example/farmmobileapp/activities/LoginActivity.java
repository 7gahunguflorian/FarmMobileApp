package com.example.farmmobileapp.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.farmmobileapp.R;
import com.example.farmmobileapp.api.ApiService;
import com.example.farmmobileapp.api.RetrofitClient;
import com.example.farmmobileapp.models.AuthRequest;
import com.example.farmmobileapp.models.AuthResponse;
import com.example.farmmobileapp.ui.client.MainClientActivity;
import com.example.farmmobileapp.ui.farmer.MainFarmerActivity;
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
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);

        // Initialize API service and session manager
        apiService = RetrofitClient.getClient().create(ApiService.class);
        sessionManager = SessionManager.getInstance(this);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateBasedOnRole();
            finish();
            return;
        }

        // Login button click listener
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInputs(username, password)) {
                progressBar.setVisibility(View.VISIBLE);
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

        apiService.login(authRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    // Save token and user information
                    sessionManager.saveAuthToken(authResponse.getToken());
                    sessionManager.saveUser(authResponse.getUser());

                    // Navigate based on user role
                    navigateBasedOnRole();
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
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
            }
        }
    }
}