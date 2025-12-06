package com.example.directaccessfarmers;

public class Bid {
    private String userId;
    private String customerName;
    private double bidValue;

    public Bid() {
        // Required empty constructor for Firestore
    }

    public Bid(String userId, String customerName, double bidValue) {
        this.userId = userId;
        this.customerName = customerName;
        this.bidValue = bidValue;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public double getBidValue() {
        return bidValue;
    }

    public void setBidValue(double bidValue) {
        this.bidValue = bidValue;
    }
}
