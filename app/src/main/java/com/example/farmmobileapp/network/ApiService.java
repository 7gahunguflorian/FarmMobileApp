package com.example.farmmobileapp.network;


import com.example.farmmobileapp.models.AuthRequest;
import com.example.farmmobileapp.models.AuthResponse;
import com.example.farmmobileapp.models.Order;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.models.User;
import com.example.farmmobileapp.models.RegisterRequest;

import java.util.List;

import okhttp3.MultipartBody;
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
    @POST("/api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("/api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    // User endpoints
    @GET("/api/users/me")
    Call<User> getCurrentUser(@Header("Authorization") String token);

    @Multipart
    @POST("/api/users/profile-image")
    Call<Void> uploadProfileImage(@Header("Authorization") String token,
                                  @Part MultipartBody.Part image);

    // Product endpoints
    @GET("/api/products")
    Call<List<Product>> getAllProducts(@Header("Authorization") String token);

    @GET("/api/products/search")
    Call<List<Product>> searchProducts(@Header("Authorization") String token,
                                       @Query("query") String query);

    @GET("/api/products/farmer/{farmerId}")
    Call<List<Product>> getFarmerProducts(@Header("Authorization") String token,
                                          @Path("farmerId") String farmerId);

    @POST("/api/products")
    Call<Product> createProduct(@Header("Authorization") String token,
                                @Body Product product);

    @PUT("/api/products/{id}")
    Call<Product> updateProduct(@Header("Authorization") String token,
                                @Path("id") String id,
                                @Body Product product);

    @DELETE("/api/products/{id}")
    Call<Void> deleteProduct(@Header("Authorization") String token,
                             @Path("id") String id);

    @Multipart
    @POST("/api/products/{id}/image")
    Call<Void> uploadProductImage(@Header("Authorization") String token,
                                  @Path("id") String id,
                                  @Part MultipartBody.Part image);

    // Orders endpoints (will need to be implemented on the backend)
    @POST("/api/orders")
    Call<Order> createOrder(@Header("Authorization") String token, @Body Order order);

    @GET("/api/orders")
    Call<List<Order>> getUserOrders(@Header("Authorization") String token);

    @GET("/api/orders/farmer")
    Call<List<Order>> getFarmerOrders(@Header("Authorization") String token);

    @PUT("/api/orders/{id}/status")
    Call<Order> updateOrderStatus(@Header("Authorization") String token,
                                  @Path("id") String id,
                                  @Body Order order);
}