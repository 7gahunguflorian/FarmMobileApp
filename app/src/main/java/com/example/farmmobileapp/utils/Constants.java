package com.example.farmmobileapp.utils;

public class Constants {
    // User roles
    public static final String USER_ROLE_ADMIN = "ADMIN";
    public static final String USER_ROLE_FARMER = "FARMER";
    public static final String USER_ROLE_CLIENT = "CLIENT";

    // Order status
    public static final String ORDER_STATUS_PENDING = "PENDING";
    public static final String ORDER_STATUS_DELIVERED = "DELIVERED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    // Intent extras
    public static final String EXTRA_PRODUCT_ID = "product_id";
    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_ROLE = "user_role";

    // Shared preferences keys
    public static final String PREF_NAME = "FarmAppPrefs";
    public static final String PREF_AUTH_TOKEN = "auth_token";
    public static final String PREF_USER_DATA = "user_data";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";

    // Request codes
    public static final int RC_PICK_IMAGE = 100;
    public static final int RC_CAMERA = 101;
    public static final int RC_PERMISSION_STORAGE = 102;
    public static final int RC_PERMISSION_CAMERA = 103;

    // API Response codes
    public static final int HTTP_OK = 200;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    public static final int HTTP_INTERNAL_SERVER_ERROR = 500;

    // Currency format
    public static final String CURRENCY_FORMAT = "$%.2f";

    // Date formats
    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy";
    public static final String DATE_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss";

    // Image types
    public static final String IMAGE_TYPE_JPEG = "image/jpeg";
    public static final String IMAGE_TYPE_PNG = "image/png";
    public static final String IMAGE_TYPE_ALL = "image/*";

    // File size limits (in bytes)
    public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final long MAX_PROFILE_IMAGE_SIZE = 2 * 1024 * 1024; // 2MB

    // Network timeouts (in seconds)
    public static final int NETWORK_CONNECT_TIMEOUT = 60;
    public static final int NETWORK_READ_TIMEOUT = 60;
    public static final int NETWORK_WRITE_TIMEOUT = 60;

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 50;

    // Validation
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 50;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 30;
    public static final int MIN_PRODUCT_NAME_LENGTH = 2;
    public static final int MAX_PRODUCT_NAME_LENGTH = 100;

    // Order limits
    public static final int MIN_ORDER_QUANTITY = 1;
    public static final int MAX_ORDER_QUANTITY = 1000;
    public static final double MIN_PRODUCT_PRICE = 0.01;
    public static final double MAX_PRODUCT_PRICE = 10000.00;

    // Cache keys
    public static final String CACHE_PRODUCTS = "products_cache";
    public static final String CACHE_USER_PROFILE = "user_profile_cache";
    public static final String CACHE_ORDERS = "orders_cache";

    // Refresh intervals (in milliseconds)
    public static final long REFRESH_INTERVAL_PRODUCTS = 5 * 60 * 1000; // 5 minutes
    public static final long REFRESH_INTERVAL_ORDERS = 2 * 60 * 1000; // 2 minutes
}

