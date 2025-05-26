package com.example.farmmobileapp.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import android.util.Log;

public class Order {
    @SerializedName("id")
    private Long id;

    @SerializedName("productId")
    private Long productId;

    @SerializedName("product")
    private Product product;

    @SerializedName("clientId")
    private Long clientId;

    @SerializedName("client")
    private User client;

    @SerializedName("farmerId")
    private Long farmerId;

    @SerializedName("farmer")
    private User farmer;

    @SerializedName("quantity")
    private Double quantity;

    @SerializedName("totalPrice")
    private Double totalPrice;

    // Use camelCase to match backend DTO
    @SerializedName("deliveryAddress")
    private String deliveryAddress;

    @SerializedName("deliveryNotes")
    private String deliveryNotes;

    @SerializedName("estimatedDeliveryTime")
    private String estimatedDeliveryTime;

    @SerializedName("actualDeliveryTime")
    private String actualDeliveryTime;

    @SerializedName("deliveryStatus")
    private String deliveryStatus;

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("updatedAt")
    private Date updatedAt;

    // Use 'items' to match backend DTO
    @SerializedName("items")
    private List<ProductItem> products;

    @SerializedName("statusDescription")
    private String statusDescription;

    @SerializedName("statusNotes")
    private String statusNotes;

    @SerializedName("statusUpdateTime")
    private Date statusUpdateTime;

    @SerializedName("orderDate")
    private Date orderDate;

    // Default constructor
    public Order() {
        this.status = "PENDING"; // Default status for new orders
        this.deliveryStatus = "PENDING";
        this.deliveryAddress = "";
        this.deliveryNotes = "";
        // Set estimated delivery time to 24 hours from now in ISO format
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 24);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.estimatedDeliveryTime = sdf.format(calendar.getTime());
    }

    // Renamed inner class to match backend expectations
    public static class ProductItem {
        @SerializedName("productId")
        private Long productId;

        @SerializedName("quantity")
        private Integer quantity;

        @SerializedName("price")
        private Double unitPrice;

        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    }

    // Keep backward compatibility with old OrderProduct class
    @Deprecated
    public static class OrderProduct extends ProductItem {
        @SerializedName("price")
        private Double price;

        @SerializedName("subtotal")
        private Double subtotal;

        public Double getPrice() { return price; }
        public void setPrice(Double price) {
            this.price = price;
            this.setUnitPrice(price); // Map to new field
        }
        public Double getSubtotal() { return subtotal; }
        public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    }

    // Constructor for creating new orders
    public Order(Long productId, Integer quantity, BigDecimal totalPrice, String deliveryAddress) {
        this.productId = productId;
        this.quantity = quantity.doubleValue();
        this.totalPrice = totalPrice.doubleValue();
        setDeliveryAddress(deliveryAddress); // Use setter to ensure proper initialization
        this.status = "PENDING"; // Default status for new orders
        this.deliveryStatus = "PENDING";
        // Set estimated delivery time to 24 hours from now in ISO format
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 24);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.estimatedDeliveryTime = sdf.format(calendar.getTime());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public User getFarmer() {
        return farmer;
    }

    public void setFarmer(User farmer) {
        this.farmer = farmer;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getDeliveryAddress() {
        return deliveryAddress != null ? deliveryAddress : "";
    }

    public void setDeliveryAddress(String deliveryAddress) {
        if (deliveryAddress == null) {
            throw new IllegalArgumentException("Delivery address cannot be null");
        }
        String trimmedAddress = deliveryAddress.trim();
        if (trimmedAddress.isEmpty()) {
            throw new IllegalArgumentException("Delivery address cannot be empty");
        }
        this.deliveryAddress = trimmedAddress;
        // Update delivery notes when address changes
        this.deliveryNotes = "Delivery requested to: " + this.deliveryAddress;
        Log.d("Order", "Setting delivery address to: " + this.deliveryAddress);
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }

    public String getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(String estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public String getActualDeliveryTime() {
        return actualDeliveryTime;
    }

    public void setActualDeliveryTime(String actualDeliveryTime) {
        this.actualDeliveryTime = actualDeliveryTime;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
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

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ProductItem> getProducts() {
        return products;
    }

    public void setProducts(List<ProductItem> products) {
        this.products = products;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
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

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    // Backward compatibility methods
    public String getDeliveryLocation() {
        return getDeliveryAddress();
    }

    public void setDeliveryLocation(String deliveryLocation) {
        setDeliveryAddress(deliveryLocation);
    }

    // Helper method to check if order is recent (within 5 minutes)
    public boolean isRecentOrder() {
        if (createdAt == null) return false;
        return (System.currentTimeMillis() - createdAt.getTime()) < 300000; // 5 minutes
    }

    public String getProductName() {
        return product != null ? product.getName() : "";
    }

    public String getProductImageUrl() {
        return product != null ? product.getImageUrl() : "";
    }

    public String getFarmerName() {
        return farmer != null ? farmer.getName() : "";
    }

    public String getClientName() {
        return client != null ? client.getName() : "";
    }
}