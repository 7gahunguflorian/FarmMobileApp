package com.example.farmmobileapp.network;

import com.example.farmmobileapp.models.AuthRequest;
import com.example.farmmobileapp.models.AuthResponse;
import com.example.farmmobileapp.models.Order;
import com.example.farmmobileapp.models.Product;
import com.example.farmmobileapp.models.User;
import com.example.farmmobileapp.models.RegisterRequest;

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
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    // New endpoint for registration with profile image
    @Multipart
    @POST("api/auth/register")
    Call<AuthResponse> registerWithImage(
            @Part("name") RequestBody name,
            @Part("username") RequestBody username,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password,
            @Part("role") RequestBody role,
            @Part MultipartBody.Part file);

    // User endpoints
    @GET("api/users/me")
    Call<User> getCurrentUser(@Header("Authorization") String token);

    @Multipart
    @POST("api/users/profile-image")
    Call<Void> uploadProfileImage(@Header("Authorization") String token,
                                  @Part MultipartBody.Part file);

    @GET("api/users")
    Call<List<User>> getAllUsers(@Header("Authorization") String token);

    @GET("api/users/{id}")
    Call<User> getUserById(@Header("Authorization") String token, @Path("id") Long id);

    @GET("api/users/role/{role}")
    Call<List<User>> getUsersByRole(@Header("Authorization") String token, @Path("role") String role);

    @DELETE("api/users/{id}")
    Call<Void> deleteUser(@Header("Authorization") String token, @Path("id") Long id);

    // Product endpoints
    @GET("api/products")
    Call<List<Product>> getAllProducts();

    @GET("api/products/{id}")
    Call<Product> getProductById(@Path("id") Long id);

    @GET("api/products/search")
    Call<List<Product>> searchProductsByName(@Query("name") String name);

    @GET("api/products/search")
    Call<List<Product>> searchProductsByPrice(@Query("minPrice") Double minPrice,
                                              @Query("maxPrice") Double maxPrice);

    @GET("api/products/farmer/{farmerId}")
    Call<List<Product>> getProductsByFarmerId(@Path("farmerId") Long farmerId);

    // Add this missing method for getting current farmer's products
    @GET("api/products/my-products")
    Call<List<Product>> getFarmerProducts(@Header("Authorization") String token);

    @POST("api/products")
    Call<Product> createProduct(@Header("Authorization") String token, @Body Product product);

    @PUT("api/products/{id}")
    Call<Product> updateProduct(@Header("Authorization") String token,
                                @Path("id") Long id,
                                @Body Product product);

    @Multipart
    @POST("api/products/{id}/image")
    Call<Void> uploadProductImage(@Header("Authorization") String token,
                                  @Path("id") Long id,
                                  @Part MultipartBody.Part file);

    @DELETE("api/products/{id}")
    Call<Void> deleteProduct(@Header("Authorization") String token, @Path("id") Long id);

    // Order endpoints
    @POST("api/orders")
    Call<Order> createOrder(@Header("Authorization") String token, @Body Order order);

    @GET("api/orders")
    Call<List<Order>> getUserOrders(@Header("Authorization") String token);

    @GET("api/orders/farmer")
    Call<List<Order>> getFarmerOrders(@Header("Authorization") String token);

    @PUT("api/orders/{id}/status")
    Call<Order> updateOrderStatus(@Header("Authorization") String token,
                                  @Path("id") Long id,
                                  @Body Order order);
}