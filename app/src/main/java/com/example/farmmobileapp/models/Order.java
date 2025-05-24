package com.example.farmmobileapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Order {
    private Long id;

    @SerializedName("client")
    private User client;

    private Long productId;
    private String productName;
    private String productImageUrl;

    @SerializedName("products")
    private List<Product> products;

    @SerializedName("productQuantities")
    private Map<String, Integer> productQuantities;

    private int quantity;

    @SerializedName("totalPrice")
    private double totalPrice;

    @SerializedName("deliveryInfo")
    private DeliveryInfo deliveryInfo;

    private String deliveryLocation;

    @SerializedName("client")
    private Long clientId;
    private String clientName;

    private Long farmerId;
    private String farmerName;

    @SerializedName("status")
    private String status; // "PENDING", "DELIVERED", "CANCELLED", etc.

    @SerializedName("orderDate")
    private Date createdAt;

    @SerializedName("statusNotes")
    private String statusNotes;

    @SerializedName("statusUpdateTime")
    private Date statusUpdateTime;

    // Inner class for DeliveryInfo
    public static class DeliveryInfo {
        private String address;
        private String city;
        private String phone;

        // Getters and setters
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        @Override
        public String toString() {
            return address + ", " + city;
        }
    }

    // Default constructor
    public Order() {}

    // Constructor
    public Order(Long productId, int quantity, double totalPrice, String deliveryLocation) {
        this.productId = productId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.deliveryLocation = deliveryLocation;
        this.status = "PENDING";
        this.createdAt = new Date();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
        if (client != null) {
            this.clientId = client.getId();
            this.clientName = client.getName();
        }
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        // If we have products list, get the first product name
        if (products != null && !products.isEmpty()) {
            return products.get(0).getName();
        }
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImageUrl() {
        // If we have products list, get the first product image
        if (products != null && !products.isEmpty()) {
            return products.get(0).getImageUrl();
        }
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Map<String, Integer> getProductQuantities() {
        return productQuantities;
    }

    public void setProductQuantities(Map<String, Integer> productQuantities) {
        this.productQuantities = productQuantities;
    }

    public int getQuantity() {
        // If we have productQuantities, sum them up
        if (productQuantities != null && !productQuantities.isEmpty()) {
            return productQuantities.values().stream().mapToInt(Integer::intValue).sum();
        }
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public DeliveryInfo getDeliveryInfo() {
        return deliveryInfo;
    }

    public void setDeliveryInfo(DeliveryInfo deliveryInfo) {
        this.deliveryInfo = deliveryInfo;
    }

    public String getDeliveryLocation() {
        // If we have deliveryInfo, use it
        if (deliveryInfo != null) {
            return deliveryInfo.toString();
        }
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public Long getClientId() {
        if (client != null) {
            return client.getId();
        }
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        if (client != null) {
            return client.getName();
        }
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatusNotes() {
        return statusNotes;
    }

    public void setStatusNotes(String statusNotes) {
        this.statusNotes = statusNotes;
    }

    public Date getStatusUpdateTime() {
        return statusUpdateTime;
    }

    public void setStatusUpdateTime(Date statusUpdateTime) {
        this.statusUpdateTime = statusUpdateTime;
    }

    public boolean isRecentOrder() {
        // Check if the order was created within the last 5 minutes
        if (createdAt == null) return false;
        return (System.currentTimeMillis() - createdAt.getTime()) < 300000; // 5 minutes in milliseconds
    }
}

