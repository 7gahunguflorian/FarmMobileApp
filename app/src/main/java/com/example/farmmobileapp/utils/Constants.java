package com.example.farmmobileapp.utils;


public class Constants {
    // Collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_PRODUCTS = "products";
    public static final String COLLECTION_ORDERS = "orders";

    // User types
    public static final String USER_TYPE_FARMER = "FARMER";
    public static final String USER_TYPE_CLIENT = "CLIENT";

    // Order status
    public static final String ORDER_STATUS_PENDING = "PENDING";
    public static final String ORDER_STATUS_DELIVERED = "DELIVERED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    // Intent extras
    public static final String EXTRA_PRODUCT_ID = "product_id";
    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_USER_TYPE = "user_type";

    // Shared preferences
    public static final String PREF_NAME = "FarmAppPrefs";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_TYPE = "user_type";
    public static final String PREF_USER_PROFILE_IMAGE = "user_profile_image";

    // Request codes
    public static final int RC_SIGN_IN = 123;
    public static final int RC_PICK_IMAGE = 124;

    // Currency format
    public static final String CURRENCY_FORMAT = "$%.2f";

    // Image storage paths
    public static final String STORAGE_PRODUCTS = "product_images";
    public static final String STORAGE_PROFILES = "profile_images";
}

