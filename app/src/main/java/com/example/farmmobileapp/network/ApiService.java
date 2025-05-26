package com.example.farmmobileapp.network;

import com.example.farmmobileapp.models.AuthRequest;
import com.example.farmmobileapp.models.AuthResponse;
import com.example.farmmobileapp.models.Order;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.models.User;
import com.example.farmmobileapp.models.RegisterRequest;
import com.example.farmmobileapp.models.ApiResponse;

import java.math.BigDecimal;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // Authentication endpoints
    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body AuthRequest loginRequest);

    @POST("auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest registerRequest);

    @GET("auth/me")
    Call<ApiResponse<User>> getCurrentUser(@Header("Authorization") String authHeader);

    // New endpoint for registration with profile image
    @Multipart
    @POST("auth/register")
    Call<ApiResponse<AuthResponse>> registerWithImage(
            @Part("name") RequestBody name,
            @Part("username") RequestBody username,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password,
            @Part("role") RequestBody role,
            @Part MultipartBody.Part file);

    // User endpoints
    @Multipart
    @POST("users/profile-image")
    Call<ApiResponse<Void>> uploadProfileImage(@Header("Authorization") String token,
                                  @Part MultipartBody.Part file);

    @GET("users")
    Call<ApiResponse<List<User>>> getAllUsers(@Header("Authorization") String token);

    @GET("users/{id}")
    Call<ApiResponse<User>> getUserById(@Header("Authorization") String token, @Path("id") Long id);

    @GET("users/role/{role}")
    Call<ApiResponse<List<User>>> getUsersByRole(@Header("Authorization") String token, @Path("role") String role);

    @DELETE("users/{id}")
    Call<ApiResponse<Void>> deleteUser(@Header("Authorization") String token, @Path("id") Long id);

    // Product endpoints
    @GET("products")
    Call<List<Product>> getAllProducts(@Header("Authorization") String authHeader);

    @GET("products/available")
    Call<List<Product>> getAvailableProducts(@Header("Authorization") String authHeader);

    @GET("products/{id}")
    Call<Product> getProductById(@Header("Authorization") String authHeader, @Path("id") Long id);

    @GET("products/search")
    Call<List<Product>> searchProducts(
        @Header("Authorization") String authHeader,
        @Query("name") String name,
        @Query("minPrice") BigDecimal minPrice,
        @Query("maxPrice") BigDecimal maxPrice
    );

    @GET("products/farmer/{farmerId}")
    Call<List<Product>> getProductsByFarmerId(
        @Header("Authorization") String authHeader,
        @Path("farmerId") Long farmerId
    );

    @GET("products/farmer/me")
    Call<List<Product>> getCurrentFarmerProducts(@Header("Authorization") String authHeader);

    @POST("products")
    Call<Product> createProduct(
        @Header("Authorization") String authHeader,
        @Body Product product
    );

    @PUT("products/{id}")
    Call<Product> updateProduct(
        @Header("Authorization") String authHeader,
        @Path("id") Long id,
        @Body Product product
    );

    @DELETE("products/{id}")
    Call<Void> deleteProduct(
        @Header("Authorization") String authHeader,
        @Path("id") Long id
    );

    @Multipart
    @POST("products/{id}/image")
    Call<Product> uploadProductImage(
        @Header("Authorization") String authHeader,
        @Path("id") Long id,
        @Part MultipartBody.Part file
    );

    // Order endpoints
    @POST("orders")
    Call<ApiResponse<Order>> createOrder(@Header("Authorization") String authHeader, @Body Order order);

    @GET("orders/client")
    Call<ApiResponse<List<Order>>> getClientOrders(
        @Header("Authorization") String authHeader,
        @Query("page") int page,
        @Query("size") int size
    );

    @GET("orders/farmer")
    Call<ApiResponse<List<Order>>> getFarmerOrders(
        @Header("Authorization") String authHeader,
        @Query("page") int page,
        @Query("size") int size
    );

    @GET("orders/{id}")
    Call<ApiResponse<Order>> getOrderById(@Header("Authorization") String authHeader, @Path("id") Long id);

    @PUT("orders/{id}/delivery-status")
    Call<ApiResponse<Order>> updateDeliveryStatus(
        @Header("Authorization") String authHeader,
        @Path("id") Long id,
        @Body Order order
    );

    @PUT("orders/{id}/status")
    Call<ApiResponse<Order>> updateOrderStatus(
        @Header("Authorization") String authHeader,
        @Path("id") Long id,
        @Body Order order
    );
}