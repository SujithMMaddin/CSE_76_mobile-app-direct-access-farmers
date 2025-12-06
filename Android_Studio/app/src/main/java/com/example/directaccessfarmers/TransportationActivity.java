package com.example.directaccessfarmers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.appcompat.app.AlertDialog;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TransportationActivity extends AppCompatActivity {

    EditText etDrop;
    Button btnProceed;
    ProgressBar progressBar;
    TextView tvStatus;

    FirebaseFirestore db;
    FirebaseAuth auth;

    String cropId;
    String farmerId;
    double amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try { LanguageManager.loadLanguage(this); } catch (Exception ignored) {}

        setContentView(R.layout.activity_transportation);

        etDrop = findViewById(R.id.etDrop);
        btnProceed = findViewById(R.id.btnProceed);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cropId = getIntent().getStringExtra("cropId");
        farmerId = getIntent().getStringExtra("farmerId");
        amount = getIntent().getDoubleExtra("amount", 0);

        btnProceed.setOnClickListener(v -> openPaymentChoice());
    }

    private void openPaymentChoice() {

        String dropLoc = etDrop.getText().toString().trim();

        if (dropLoc.isEmpty()) {
            Toast.makeText(this, "Enter delivery location", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Payment Method");

        String[] methods = {"Pay Using UPI", "Cash On Delivery"};

        builder.setItems(methods, (dialog, which) -> {
            if (which == 0) {
                // ⭐ UPI
                Intent intent = new Intent(this, PaymentActivity.class);
                intent.putExtra("cropId", cropId);
                intent.putExtra("farmerId", farmerId);
                intent.putExtra("amount", amount);
                startActivity(intent);

            } else {
                // ⭐ COD
                saveCOD(dropLoc);
            }
        });

        builder.show();
    }

    private void saveCOD(String location) {

        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> codMap = new HashMap<>();
        codMap.put("cropId", cropId);
        codMap.put("customerId", auth.getUid());
        codMap.put("farmerId", farmerId);
        codMap.put("location", location);
        codMap.put("paymentMode", "COD");
        codMap.put("status", "COD Payment Pending");
        codMap.put("timestamp", new Timestamp(new Date()));

        // Save to crop subcollection
        db.collection("Crops").document(cropId)
                .collection("Payment")
                .add(codMap);

        // ⭐ Notify farmer
        db.collection("FarmerNotifications")
                .add(codMap);

        progressBar.setVisibility(View.GONE);
        tvStatus.setText("COD Booked ✔ Farmer Notified!");
    }
}
