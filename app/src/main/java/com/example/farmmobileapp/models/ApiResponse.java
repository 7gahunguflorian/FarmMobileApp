package com.example.farmmobileapp.models;


import java.util.List;

// Generic API Response wrapper
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;

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
