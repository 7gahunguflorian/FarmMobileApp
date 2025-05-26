package com.example.farmmobileapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.farmmobileapp.network.RetrofitClient;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class Product implements Parcelable {
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private BigDecimal price;

    @SerializedName("availableQuantity")
    private Integer availableQuantity;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("ownerId")
    private Long ownerId;

    @SerializedName("ownerName")
    private String ownerName;

    @SerializedName("owner")
    private User owner;

    public Product() {
        // Default constructor
    }

    protected Product(Parcel in) {
        id = in.readByte() == 0 ? null : in.readLong();
        name = in.readString();
        description = in.readString();
        String priceStr = in.readString();
        price = priceStr != null ? new BigDecimal(priceStr) : null;
        availableQuantity = in.readByte() == 0 ? null : in.readInt();
        imageUrl = in.readString();
        ownerId = in.readByte() == 0 ? null : in.readLong();
        ownerName = in.readString();
        owner = in.readParcelable(User.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (id == null ? 0 : 1));
        if (id != null) {
            dest.writeLong(id);
        }
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(price != null ? price.toString() : null);
        dest.writeByte((byte) (availableQuantity == null ? 0 : 1));
        if (availableQuantity != null) {
            dest.writeInt(availableQuantity);
        }
        dest.writeString(imageUrl);
        dest.writeByte((byte) (ownerId == null ? 0 : 1));
        if (ownerId != null) {
            dest.writeLong(ownerId);
        }
        dest.writeString(ownerName);
        dest.writeParcelable(owner, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getFarmerName() {
        if (ownerName != null && !ownerName.isEmpty()) {
            return ownerName;
        }
        return owner != null ? owner.getName() : "Unknown Farmer";
    }

    // Helper method to get full image URL
    public String getFullImageUrl() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        // Handle different URL formats
        if (imageUrl.startsWith("http")) {
            return imageUrl;
        } else if (imageUrl.startsWith("/")) {
            // Remove the leading slash and /api/ from the base URL
            String baseUrl = RetrofitClient.getBaseUrl();
            if (baseUrl.endsWith("/api/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 5);
            }
            return baseUrl + imageUrl;
        } else {
            // Remove /api/ from the base URL
            String baseUrl = RetrofitClient.getBaseUrl();
            if (baseUrl.endsWith("/api/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 5);
            }
            return baseUrl + "/images/" + imageUrl;
        }
    }

    @Override
    public void finalize() throws Throwable {
        try {
            // Clean up any resources
            imageUrl = null;
            name = null;
            description = null;
            ownerName = null;
            owner = null;
        } finally {
            super.finalize();
        }
    }
}