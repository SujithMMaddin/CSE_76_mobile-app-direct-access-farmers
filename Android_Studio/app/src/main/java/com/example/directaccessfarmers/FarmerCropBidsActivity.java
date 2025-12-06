package com.example.directaccessfarmers;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * FarmerCropBidsActivity
 * ----------------------
 * ✅ Displays all bids for a crop
 * ✅ Highlights highest bid (live)
 * ✅ Allows manual "Confirm Winner" if bidding ended
 * ✅ Automatically locks after end time too
 */
public class FarmerCropBidsActivity extends AppCompatActivity {

    public static final String EXTRA_CROP_ID = "cropId";

    private TextView tvCropName, tvBiddingEnd, tvWinnerInfo, tvNoBids;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private Button btnConfirmWinner; // ✅ Added for manual confirmation

    private FirebaseFirestore db;
    private ListenerRegistration bidsListener;

    private String cropId;
    private Crop currentCrop;

    private final List<BidItem> bidList = new ArrayList<>();
    private BidsListAdapter adapter;

    private final SimpleDateFormat sdfDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            LanguageManager.loadLanguage(this);
        } catch (Exception e) {
            // Continue with default language
        }
        setContentView(R.layout.activity_farmer_crop_bids);

        // Bind views
        tvCropName = findViewById(R.id.tvCropTitle);
        tvBiddingEnd = findViewById(R.id.tvEndTime);
        tvWinnerInfo = findViewById(R.id.tvWinnerInfo);
        tvNoBids = findViewById(R.id.tvNoBids);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerBids);
        btnConfirmWinner = findViewById(R.id.btnConfirmWinner); // ✅ safe binding

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BidsListAdapter(bidList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        cropId = getIntent().getStringExtra(EXTRA_CROP_ID);
        if (cropId == null || cropId.isEmpty()) {
            Toast.makeText(this, "Invalid crop id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load crop + listen to bids
        loadCropAndListenBids();

        // ✅ Manual Confirm Winner
        btnConfirmWinner.setOnClickListener(v -> {
            if (currentCrop == null || bidList.isEmpty()) {
                Toast.makeText(this, "No bids to confirm yet.", Toast.LENGTH_SHORT).show();
                return;
            }
            confirmHighestBidManually();
        });
    }

    /**
     * Load crop info and begin listening to bids
     */
    private void loadCropAndListenBids() {
        progressBar.setVisibility(View.VISIBLE);

        DocumentReference cropRef = db.collection("Crops").document(cropId);
        cropRef.get().addOnSuccessListener(doc -> {
            progressBar.setVisibility(View.GONE);
            if (!doc.exists()) {
                Toast.makeText(this, "Crop not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            currentCrop = doc.toObject(Crop.class);
            if (currentCrop != null) {
                tvCropName.setText(currentCrop.getName());
                Timestamp endTs = currentCrop.getBiddingEndDate();
                if (endTs != null) {
                    tvBiddingEnd.setText("Bidding Ends: " + sdfDatetime.format(endTs.toDate()));
                } else {
                    tvBiddingEnd.setText("Bidding Ends: N/A");
                }

                if (currentCrop.isSold()) {
                    tvWinnerInfo.setText("Final Winner: " + currentCrop.getWinnerId()
                            + " | ₹" + String.format(Locale.getDefault(), "%.2f", currentCrop.getHighestBid()));
                    tvWinnerInfo.setTextColor(Color.parseColor("#2E7D32"));
                    btnConfirmWinner.setEnabled(false);
                }
            }

            // Auto-lock if bidding ended
            if (currentCrop != null && currentCrop.getBiddingEndDate() != null) {
                Date endDate = currentCrop.getBiddingEndDate().toDate();
                if (endDate.before(new Date()) && !currentCrop.isSold()) {
                    BidLockHelper.lockCropBidding(cropId, db, (success, msg) -> {
                        if (success) {
                            cropRef.get().addOnSuccessListener(updated -> {
                                Crop c = updated.toObject(Crop.class);
                                if (c != null) {
                                    currentCrop = c;
                                    tvWinnerInfo.setText("Final Winner: " + c.getWinnerId()
                                            + " | ₹" + String.format(Locale.getDefault(), "%.2f", c.getHighestBid()));
                                    tvWinnerInfo.setTextColor(Color.parseColor("#2E7D32"));
                                    btnConfirmWinner.setEnabled(false);
                                }
                            });
                        }
                    });
                }
            }

            listenBids();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to load crop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    /**
     * Listen to live bid updates
     */
    private void listenBids() {
        progressBar.setVisibility(View.VISIBLE);
        CollectionReference bidsRef = db.collection("Crops").document(cropId).collection("Bids");

        bidsListener = bidsRef.orderBy("bidValue", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    progressBar.setVisibility(View.GONE);
                    if (e != null) {
                        Toast.makeText(FarmerCropBidsActivity.this,
                                "Error loading bids: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        bidList.clear();
                        adapter.notifyDataSetChanged();
                        tvNoBids.setVisibility(View.VISIBLE);
                        tvWinnerInfo.setText("No bids yet.");
                        return;
                    }

                    tvNoBids.setVisibility(View.GONE);
                    bidList.clear();

                    double highest = 0;
                    String winnerName = "";
                    String winnerId = "";

                    for (DocumentSnapshot ds : snapshots.getDocuments()) {
                        Double value = ds.getDouble("bidValue");
                        String custName = ds.getString("customerName");
                        String custId = ds.getString("customerId");
                        Date time = ds.getDate("timestamp");
                        if (value == null) value = 0.0;

                        bidList.add(new BidItem(custId, custName, value, time));

                        if (value > highest) {
                            highest = value;
                            winnerName = (custName != null) ? custName : "-";
                            winnerId = (custId != null) ? custId : "-";
                        }
                    }

                    adapter.setTopBid(highest);
                    adapter.notifyDataSetChanged();

                    if (highest > 0) {
                        tvWinnerInfo.setText("Current Highest: ₹" +
                                String.format(Locale.getDefault(), "%.2f", highest) +
                                " — " + winnerName);
                        tvWinnerInfo.setTextColor(Color.parseColor("#2E7D32"));
                    } else {
                        tvWinnerInfo.setText("No bids yet.");
                    }
                });
    }

    /**
     * Manual confirmation of the highest bid
     */
    private void confirmHighestBidManually() {
        if (bidList.isEmpty()) {
            Toast.makeText(this, "No bids available.", Toast.LENGTH_SHORT).show();
            return;
        }

        BidItem topBid = bidList.get(0);
        db.collection("Crops").document(cropId)
                .update("isSold", true,
                        "winnerId", topBid.customerId,
                        "highestBid", topBid.bidValue)
                .addOnSuccessListener(unused -> {
                    tvWinnerInfo.setText("Final Winner: " + topBid.customerName +
                            " | ₹" + String.format(Locale.getDefault(), "%.2f", topBid.bidValue));
                    tvWinnerInfo.setTextColor(Color.parseColor("#2E7D32"));
                    btnConfirmWinner.setEnabled(false);
                    Toast.makeText(this, "✅ Winner confirmed successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error confirming winner: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bidsListener != null) bidsListener.remove();
    }

    // -------- Bid model ----------
    public static class BidItem {
        public final String customerId;
        public final String customerName;
        public final double bidValue;
        public final Date timestamp;

        public BidItem(String customerId, String customerName, double bidValue, Date timestamp) {
            this.customerId = customerId;
            this.customerName = customerName;
            this.bidValue = bidValue;
            this.timestamp = timestamp;
        }
    }

    // -------- Adapter -------------
    static class BidsListAdapter extends RecyclerView.Adapter<BidsListAdapter.Holder> {
        private final List<BidItem> items;
        private double topBid = 0;

        BidsListAdapter(List<BidItem> items) { this.items = items; }

        void setTopBid(double top) { this.topBid = top; }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            BidItem b = items.get(position);
            String title = (b.customerName != null ? b.customerName : "Unknown")
                    + " — ₹" + String.format(Locale.getDefault(), "%.2f", b.bidValue);
            String subtitle = (b.timestamp != null)
                    ? new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(b.timestamp)
                    : "";
            holder.tv1.setText(title);
            holder.tv2.setText(subtitle);

            if (Double.compare(b.bidValue, topBid) == 0) {
                holder.tv1.setTextColor(Color.parseColor("#2E7D32"));
            } else {
                holder.tv1.setTextColor(Color.BLACK);
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tv1, tv2;
            Holder(@NonNull View itemView) {
                super(itemView);
                tv1 = itemView.findViewById(android.R.id.text1);
                tv2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
