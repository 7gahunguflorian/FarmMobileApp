package com.example.farmmobileapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.farmmobileapp.R;
import com.example.farmmobileapp.models.ApiResponse;
import com.example.farmmobileapp.network.ApiService;
import com.example.farmmobileapp.network.RetrofitClient;
import com.example.farmmobileapp.models.AuthResponse;
import com.example.farmmobileapp.models.RegisterRequest;
import com.example.farmmobileapp.utils.SessionManager;
import com.example.farmmobileapp.utils.ErrorHandler;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int IMAGE_PICK_CODE = 1000;

    private TextInputEditText etName, etUsername, etEmail, etPassword;
    private RadioGroup rgRole;
    private RadioButton rbClient, rbFarmer;
    private Button btnSelectImage, btnRegister;
    private TextView tvLogin;
    private ImageView imgProfile;
    private ProgressBar progressBar;
    private ApiService apiService;
    private SessionManager sessionManager;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        initViews();

        // Initialize API service and session manager
        apiService = RetrofitClient.getClient().create(ApiService.class);
        sessionManager = SessionManager.getInstance(this);

        // Set up click listeners
        setupClickListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        rgRole = findViewById(R.id.rgRole);
        rbClient = findViewById(R.id.rbClient);
        rbFarmer = findViewById(R.id.rbFarmer);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        imgProfile = findViewById(R.id.imgProfile);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // Select image button click listener
        btnSelectImage.setOnClickListener(v -> {
            if (checkPermission()) {
                pickImageFromGallery();
            } else {
                requestPermission();
            }
        });

        // Register button click listener
        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = rbFarmer.isChecked() ? "FARMER" : "CLIENT";

            if (validateInputs(name, username, email, password)) {
                progressBar.setVisibility(View.VISIBLE);
                registerUser(name, username, email, password, role);
            }
        });

        // Login text click listener
        tvLogin.setOnClickListener(v -> {
            finish(); // Go back to LoginActivity
        });
    }

    private boolean validateInputs(String name, String username, String email, String password) {
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }

        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters long");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void registerUser(String name, String username, String email, String password, String role) {
        RegisterRequest request = new RegisterRequest(name, username, email, password, role);
        apiService.register(request).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        AuthResponse authResponse = apiResponse.getData();
                        sessionManager.saveAuthToken(authResponse.getToken());
                        sessionManager.saveUser(authResponse.getUser());
                        navigateBasedOnRole();
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? 
                            apiResponse.getMessage() : "Registration failed";
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ErrorHandler.handleApiError(RegisterActivity.this, response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateBasedOnRole() {
        if (sessionManager.getUser() != null) {
            String role = sessionManager.getUser().getRole();

            if ("FARMER".equals(role)) {
                startActivity(new Intent(RegisterActivity.this, MainFarmerActivity.class));
            } else if ("CLIENT".equals(role)) {
                startActivity(new Intent(RegisterActivity.this, MainClientActivity.class));
            }
        }
    }
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(this, "Permission denied. Cannot select profile picture.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null) {
            selectedImageUri = data.getData();
            imgProfile.setImageURI(selectedImageUri);
        }
    }
    private File getFileFromUri(Uri uri) {
        try {
            // Use ContentResolver to open an input stream from the URI
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            // Create a temporary file
            File file = new File(getCacheDir(), "temp_profile_image.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);

            // Copy the content
            byte[] buffer = new byte[4 * 1024]; // 4KB buffer
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

            // Close streams
            inputStream.close();
            outputStream.close();

            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
