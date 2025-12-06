package com.example.directaccessfarmers;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.*;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    TextView tvAmount, tvStatus;
    ImageView imgQrCode;
    Button btnConfirmPayment;

    FirebaseFirestore db;
    FirebaseAuth auth;

    String cropId, farmerId, farmerUpi;
    double amount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        tvAmount = findViewById(R.id.tvAmount);
        tvStatus = findViewById(R.id.tvStatus);
        imgQrCode = findViewById(R.id.imgQrCode);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cropId = getIntent().getStringExtra("cropId");

        loadCropDetails();
    }

    private void loadCropDetails() {
        db.collection("Crops").document(cropId)
                .get()
                .addOnSuccessListener(doc -> {
                    amount = doc.getDouble("highestBid");
                    farmerId = doc.getString("farmerId");

                    tvAmount.setText("Amount: ₹" + amount);

                    loadFarmerUpi();
                });
    }

    private void loadFarmerUpi() {
        db.collection("users").document(farmerId)
                .get()
                .addOnSuccessListener(doc -> {
                    farmerUpi = doc.getString("upiId");

                    generateQrCode();
                    btnConfirmPayment.setOnClickListener(v -> savePayment());
                });
    }

    private void generateQrCode() {

        String upiUri = "upi://pay?pa=" + farmerUpi +
                "&pn=Farmer" +
                "&am=" + amount +
                "&cu=INR";

        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(upiUri, BarcodeFormat.QR_CODE, 500, 500);
            imgQrCode.setImageBitmap(bitmap);
        }
        catch (WriterException e) {
            tvStatus.setText("Failed to generate QR: " + e.getMessage());
        }
    }

    private void savePayment() {

        Map<String, Object> map = new HashMap<>();
        map.put("cropId", cropId);
        map.put("farmerId", farmerId);
        map.put("customerId", auth.getUid());
        map.put("amount", amount);
        map.put("upiId", farmerUpi);
        map.put("status", "Paid via QR (Manual Confirmation)");
        map.put("timestamp", new Timestamp(new Date()));

        // Save to payment history
        db.collection("Crops").document(cropId)
                .collection("Payment")
                .add(map);

        // Notify farmer
        db.collection("FarmerNotifications")
                .add(map);

        tvStatus.setText("Payment confirmed and saved!");
        Toast.makeText(this, "Thank you! Payment saved.", Toast.LENGTH_LONG).show();

        finish();
    }
}



