package com.example.farmmobileapp.api;

import com.example.farmmobileapp.models.Order;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.models.User;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    // User endpoints
    @POST("auth/register")
    Call<User> register(@Body User user);

    @POST("auth/login")
    Call<User> login(@Body User user);

    // Product endpoints
    @GET("products")
    Call<List<Product>> getAllProducts(@Header("Authorization") String authHeader);

    @GET("products/farmer")
    Call<List<Product>> getCurrentFarmerProducts(@Header("Authorization") String authHeader);

    @GET("products/{id}")
    Call<Product> getProductById(@Header("Authorization") String authHeader, @Path("id") Long id);

    @POST("products")
    Call<Product> createProduct(@Header("Authorization") String authHeader, @Body Product product);

    @PUT("products/{id}")
    Call<Product> updateProduct(@Header("Authorization") String authHeader, @Path("id") Long id, @Body Product product);

    @Multipart
    @POST("products/{id}/image")
    Call<Product> uploadProductImage(
        @Header("Authorization") String authHeader,
        @Path("id") Long id,
        @Part MultipartBody.Part image
    );

    // Order endpoints
    @POST("orders")
    Call<Order> createOrder(@Header("Authorization") String authHeader, @Body Order order);

    @GET("orders/client")
    Call<List<Order>> getClientOrders(@Header("Authorization") String authHeader);

    @GET("orders/farmer")
    Call<List<Order>> getFarmerOrders(@Header("Authorization") String authHeader);

    @PUT("orders/{orderId}/status")
    Call<Order> updateOrderStatus(
        @Header("Authorization") String authHeader,
        @Path("orderId") Long orderId,
        @Body Map<String, String> status
    );
} 