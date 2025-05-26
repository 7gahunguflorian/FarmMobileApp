package com.example.farmmobileapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// Generic API Response wrapper
public class ApiResponse<T> {
    @SerializedName("success")
    private boolean success = true; // Default to true since some endpoints don't include this field

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("error")
    private String error;

    // For direct response format (when data is at root level)
    @SerializedName("token")
    private String token;

    @SerializedName("user")
    private User user;

    // Constructors
    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        // If data is null but we have direct fields, create an AuthResponse
        if (data == null && token != null && !token.equals("present")) {
            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(token);
            authResponse.setUser(user);
            return (T) authResponse;
        }
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + (data != null ? data.toString() : "null") +
                ", error='" + error + '\'' +
                ", token=" + (token != null ? "present" : "null") +
                ", user=" + (user != null ? user.toString() : "null") +
                '}';
    }
}

// Specific wrapper for Orders List
class OrdersResponse {
    private List<Order> orders;
    private int totalCount;
    private String status;

    // Constructors
    public OrdersResponse() {}

    public OrdersResponse(List<Order> orders) {
        this.orders = orders;
        this.totalCount = orders != null ? orders.size() : 0;
    }

    // Getters and setters
    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        this.totalCount = orders != null ? orders.size() : 0;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
