package com.example.directaccessfarmers;

public class Constants {
    // Firebase Collections
    public static final String COLLECTION_CROPS = "crops";
    public static final String COLLECTION_BIDS = "bids";
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_BID_HISTORY = "bidHistory";

    // Fields
    public static final String FIELD_CROP_ID = "cropId";
    public static final String FIELD_CUSTOMER_ID = "customerId";
    public static final String FIELD_FARMER_ID = "farmerId";
    public static final String FIELD_SOLD = "sold";
    public static final String FIELD_BID_VALUE = "bidValue";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_ROLE = "role";

    // Roles
    public static final String ROLE_FARMER = "farmer";
    public static final String ROLE_CUSTOMER = "customer";

    // AI Bid Parameters
    public static final double MIN_BID_MULTIPLIER = 0.8;
    public static final double MAX_BID_MULTIPLIER = 1.5;
    public static final double MARKET_TREND_WEIGHT = 0.3;
    public static final double CUSTOMER_PREFERENCE_WEIGHT = 0.4;
    public static final double BASE_PRICE_WEIGHT = 0.3;
}

