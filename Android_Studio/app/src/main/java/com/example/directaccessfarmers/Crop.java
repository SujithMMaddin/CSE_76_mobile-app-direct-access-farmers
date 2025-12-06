package com.example.directaccessfarmers;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Crop {
    private String id;
    private String farmerId;
    private String name;
    private double pricePerKg;
    private double quantityKg;
    private String location;
    private Timestamp harvestDate;
    private Timestamp biddingEndDate;

    // Existing fields
    private boolean isSold = false;
    private String winnerId = "";
    private double highestBid = 0.0;

    // ✅ Newly added fields
    private String imageUrl = "";
    private String farmerName = "";
    private String farmerPhone = "";

    // Empty constructor (Firestore needs this)
    public Crop() {}

    // Full constructor (optional)
    public Crop(String id, String farmerId, String name, double pricePerKg, double quantityKg,
                String location, Timestamp harvestDate, Timestamp biddingEndDate) {
        this.id = id;
        this.farmerId = farmerId;
        this.name = name;
        this.pricePerKg = pricePerKg;
        this.quantityKg = quantityKg;
        this.location = location;
        this.harvestDate = harvestDate;
        this.biddingEndDate = biddingEndDate;
    }

    // ---------- Getters & Setters ----------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPricePerKg() { return pricePerKg; }
    public void setPricePerKg(double pricePerKg) { this.pricePerKg = pricePerKg; }

    public double getQuantityKg() { return quantityKg; }
    public void setQuantityKg(double quantityKg) { this.quantityKg = quantityKg; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Timestamp getHarvestDate() { return harvestDate; }
    public void setHarvestDate(Timestamp harvestDate) { this.harvestDate = harvestDate; }

    public Timestamp getBiddingEndDate() { return biddingEndDate; }
    public void setBiddingEndDate(Timestamp biddingEndDate) { this.biddingEndDate = biddingEndDate; }

    public boolean isSold() { return isSold; }
    public void setSold(boolean sold) { isSold = sold; }

    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }

    public double getHighestBid() { return highestBid; }
    public void setHighestBid(double highestBid) { this.highestBid = highestBid; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getFarmerPhone() { return farmerPhone; }
    public void setFarmerPhone(String farmerPhone) { this.farmerPhone = farmerPhone; }

    // ---------- Firestore map conversion ----------
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("farmerId", farmerId);
        map.put("name", name);
        map.put("pricePerKg", pricePerKg);
        map.put("quantityKg", quantityKg);
        map.put("location", location);
        map.put("harvestDate", harvestDate);
        map.put("biddingEndDate", biddingEndDate);
        map.put("isSold", isSold);
        map.put("winnerId", winnerId);
        map.put("highestBid", highestBid);
        map.put("imageUrl", imageUrl);
        map.put("farmerName", farmerName);
        map.put("farmerPhone", farmerPhone);
        return map;
    }
}
