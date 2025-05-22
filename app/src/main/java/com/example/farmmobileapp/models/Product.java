package com.example.farmmobileapp.models;

public class Product {
    private Long id;
    private String name;
    private double price;
    private int availableQuantity;
    private String description;
    private String imageUrl;
    private Long farmerId;
    private String farmerName;

    // Default constructor
    public Product() {}

    // Constructor
    public Product(String name, double price, String description, int availableQuantity) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.availableQuantity = availableQuantity;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    // Keep this for backward compatibility
    public int getQuantity() {
        return availableQuantity;
    }

    public void setQuantity(int quantity) {
        this.availableQuantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
}