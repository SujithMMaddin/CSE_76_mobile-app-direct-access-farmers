package com.example.directaccessfarmers;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

public class FarmerCropDetailsActivity extends AppCompatActivity {

    private TextView tvCropName, tvPrice, tvQuantity, tvLocation, tvHarvestDate,
            tvBiddingEnd, tvWinner, tvFarmerName, tvFarmerPhone;
    private FirebaseFirestore db;
    private String cropId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            LanguageManager.loadLanguage(this);
        } catch (Exception e) {
            // Continue with default language
        }
        setContentView(R.layout.activity_farmer_crop_details);

        tvCropName = findViewById(R.id.tvCropName);
        tvPrice = findViewById(R.id.tvPrice);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvLocation = findViewById(R.id.tvLocation);
        tvHarvestDate = findViewById(R.id.tvHarvestDate);
        tvBiddingEnd = findViewById(R.id.tvBiddingEnd);
        tvWinner = findViewById(R.id.tvWinner);
        tvFarmerName = findViewById(R.id.tvFarmerName);
        tvFarmerPhone = findViewById(R.id.tvFarmerPhone);

        tvWinner.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();
        cropId = getIntent().getStringExtra("cropId");

        if (cropId == null || cropId.isEmpty()) {
            Toast.makeText(this, "Invalid crop ID", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            loadCropDetails();
        }
    }

    private void loadCropDetails() {
        db.collection("Crops").document(cropId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String cropName = doc.getString("name");
                        Double price = doc.getDouble("pricePerKg");
                        Double quantity = doc.getDouble("quantityKg");
                        String location = doc.getString("location");
                        String harvestDate = String.valueOf(doc.get("harvestDate"));
                        String biddingEnd = String.valueOf(doc.get("biddingEndDate"));
                        String farmerName = doc.getString("farmerName");
                        String farmerPhone = doc.getString("farmerPhone");
                        Boolean isSold = doc.getBoolean("isSold");
                        Double highestBid = doc.getDouble("highestBid");
                        String winnerId = doc.getString("winnerId");

                        tvCropName.setText(cropName != null ? cropName : "-");
                        tvPrice.setText("₹" + (price != null ? price : 0));
                        tvQuantity.setText("Quantity: " + (quantity != null ? quantity : 0) + " kg");
                        tvLocation.setText("Location: " + (location != null ? location : "-"));
                        tvHarvestDate.setText("Harvest Date: " + (harvestDate != null ? harvestDate : "-"));
                        tvBiddingEnd.setText("Bidding Ends: " + (biddingEnd != null ? biddingEnd : "-"));
                        tvFarmerName.setText("Farmer: " + (farmerName != null ? farmerName : "-"));
                        tvFarmerPhone.setText("📞 Phone: " + (farmerPhone != null ? farmerPhone : "-"));

                        if (Boolean.TRUE.equals(isSold)) {
                            tvWinner.setVisibility(View.VISIBLE);
                            tvWinner.setText("Winner ID: " + winnerId + " (₹" + highestBid + ")");
                        }
                    } else {
                        Toast.makeText(this, "Crop not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load crop: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
