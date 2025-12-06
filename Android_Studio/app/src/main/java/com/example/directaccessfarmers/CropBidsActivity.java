package com.example.directaccessfarmers;
import com.example.directaccessfarmers.BuildConfig;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;
import android.util.Log;

public class CropBidsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvCropName, tvPrice, tvBiddingEnd, tvNoBids, tvAvgBid;
    private Button btnPlaceBid;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String cropId;
    private Crop currentCrop;

    private final List<Bid> bidList = new ArrayList<>();
    private BidsAdapter bidsAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            LanguageManager.loadLanguage(this);
        } catch (Exception e) {
            // Continue with default language
        }
        setContentView(R.layout.activity_crop_bids);
        Log.d("AI_KEY_TEST", "OpenAI API Key: " + BuildConfig.OPENAI_API_KEY);

        // Get crop ID
        cropId = getIntent().getStringExtra("cropId");

        // Firebase init
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Bind views
        recyclerView = findViewById(R.id.recyclerBids);
        progressBar = findViewById(R.id.progressBids);
        tvCropName = findViewById(R.id.tvCropName);
        tvPrice = findViewById(R.id.tvPrice);
        tvBiddingEnd = findViewById(R.id.tvBiddingEnd);
        tvNoBids = findViewById(R.id.tvNoBids);
        tvAvgBid = findViewById(R.id.tvAvgBid);
        btnPlaceBid = findViewById(R.id.btnPlaceBid);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bidsAdapter = new BidsAdapter(bidList);
        recyclerView.setAdapter(bidsAdapter);

        loadCropDetails();
        loadBids();

        btnPlaceBid.setOnClickListener(v -> showBidDialog());
    }

    // ---------- Load Crop ----------
    private void loadCropDetails() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Crops").document(cropId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        currentCrop = documentSnapshot.toObject(Crop.class);
                        if (currentCrop != null) {
                            tvCropName.setText("Crop: " + currentCrop.getName());
                            tvPrice.setText("Base Price: ₹" + currentCrop.getPricePerKg());

                            Timestamp ts = currentCrop.getBiddingEndDate();
                            String dateStr = (ts != null)
                                    ? ts.toDate().toString()
                                    : "N/A";
                            tvBiddingEnd.setText("Bidding Ends: " + dateStr);

                            if (ts != null && ts.toDate().before(new Date())) {
                                btnPlaceBid.setEnabled(false);
                                btnPlaceBid.setText("Bidding Closed");
                            }
                        }
                    } else {
                        Toast.makeText(this, "Crop not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load crop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ---------- Load Bids ----------
    private void loadBids() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Crops").document(cropId).collection("Bids")
                .orderBy("bidValue", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    progressBar.setVisibility(View.GONE);
                    if (e != null) {
                        Toast.makeText(this, "Error loading bids: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        bidList.clear();
                        bidsAdapter.notifyDataSetChanged();
                        tvNoBids.setVisibility(View.VISIBLE);
                        tvAvgBid.setText("Average bid: -");
                    } else {
                        tvNoBids.setVisibility(View.GONE);
                        bidList.clear();
                        double sum = 0;
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Bid bid = doc.toObject(Bid.class);
                            if (bid != null) {
                                bidList.add(bid);
                                sum += bid.getBidValue();
                            }
                        }
                        bidsAdapter.notifyDataSetChanged();

                        double avgBid = sum / bidList.size();
                        tvAvgBid.setText("Average bid: ₹" + String.format("%.2f", avgBid));

                        // 🔹 AI Advisor based on market + avg bids
                        if (currentCrop != null) {
                            showAIBidAdvisor(currentCrop.getName(), avgBid);
                        }
                    }
                });
    }

    // ---------- Bid Dialog ----------
    private void showBidDialog() {
        if (currentCrop == null) {
            Toast.makeText(this, "Crop not loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentCrop.getBiddingEndDate() != null &&
                currentCrop.getBiddingEndDate().toDate().before(new Date())) {
            Toast.makeText(this, "Bidding time is over!", Toast.LENGTH_SHORT).show();
            btnPlaceBid.setEnabled(false);
            btnPlaceBid.setText("Bidding Closed");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Your Bid Amount (₹)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("e.g., 1500");
        builder.setView(input);

        builder.setPositiveButton("Place Bid", (dialog, which) -> {
            String value = input.getText().toString().trim();
            if (value.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }
            double bidValue;
            try {
                bidValue = Double.parseDouble(value);
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (bidValue < currentCrop.getPricePerKg()) {
                Toast.makeText(this, "Bid must be higher than base price!", Toast.LENGTH_SHORT).show();
                return;
            }

            placeBid(bidValue);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // ---------- Place Bid ----------
    private void placeBid(double bidValue) {
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        String userName = auth.getCurrentUser().getEmail();

        db.collection("Crops").document(cropId).collection("Bids")
                .orderBy("bidValue", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    double highestBid = 0.0;
                    if (!snapshot.isEmpty()) {
                        Bid top = snapshot.getDocuments().get(0).toObject(Bid.class);
                        if (top != null) highestBid = top.getBidValue();
                    }

                    if (bidValue <= highestBid) {
                        Toast.makeText(this, "Your bid must be higher than current highest bid!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Bid bid = new Bid(userId, userName, bidValue);

                    db.collection("Crops").document(cropId)
                            .collection("Bids")
                            .add(bid)
                            .addOnSuccessListener(docRef -> {
                                Toast.makeText(this, "Bid placed successfully!", Toast.LENGTH_SHORT).show();
                                db.collection("Crops").document(cropId)
                                        .update("highestBid", bidValue);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    // ---------- AI Bid Advisor ----------
    private void showAIBidAdvisor(String cropName, double avgBid) {
        tvAvgBid.setText("Analyzing AI market data...");

        String prompt = "You are a smart agricultural bidding assistant. " +
                "The crop is " + cropName + ". " +
                "The current average bid is ₹" + avgBid + ". " +
                "Provide a one-line useful suggestion for a customer: " +
                "consider the market price trend in India and suggest a smart bid range.";

        OpenAIHelper aiHelper = new OpenAIHelper(BuildConfig.OPENAI_API_KEY);
        aiHelper.getAIResponse(prompt, new OpenAIHelper.AIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> tvAvgBid.setText("🤖 " + response));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> tvAvgBid.setText("AI Error: " + error));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bidsAdapter != null) bidsAdapter.notifyDataSetChanged();
    }
}
