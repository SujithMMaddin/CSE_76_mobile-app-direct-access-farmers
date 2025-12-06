package com.example.directaccessfarmers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CropDetailsActivity extends AppCompatActivity {

    private ImageView imageCrop;
    private TextView tvCropName, tvPrice, tvQuantity, tvLocation, tvHarvestDate, tvBiddingEnd, tvFarmerName, tvFarmerPhone;
    private TextView tvWinner, tvWinnerMessage, tvCountdown;
    private Button btnCallFarmer, btnPlaceBid, btnPayNow;
    private ProgressBar progressDetails;
    private RecyclerView recyclerBids;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String cropId;
    private String currentUserId;

    private CountDownTimer countDownTimer;

    private List<Bid> bidList = new ArrayList<>();
    private BidAdapter bidAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try { LanguageManager.loadLanguage(this); } catch (Exception ignored) {}

        setContentView(R.layout.activity_crop_details);

        imageCrop = findViewById(R.id.imageCrop);
        tvCropName = findViewById(R.id.tvCropName);
        tvPrice = findViewById(R.id.tvPrice);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvLocation = findViewById(R.id.tvLocation);
        tvHarvestDate = findViewById(R.id.tvHarvestDate);
        tvBiddingEnd = findViewById(R.id.tvBiddingEnd);
        tvFarmerName = findViewById(R.id.tvFarmerName);
        tvFarmerPhone = findViewById(R.id.tvFarmerPhone);
        tvWinner = findViewById(R.id.tvWinner);
        tvWinnerMessage = findViewById(R.id.tvWinnerMessage);
        tvCountdown = findViewById(R.id.tvCountdown);

        btnCallFarmer = findViewById(R.id.btnCallFarmer);
        btnPlaceBid = findViewById(R.id.btnPlaceBid);
        btnPayNow = findViewById(R.id.btnPayNow);

        progressDetails = findViewById(R.id.progressDetails);
        recyclerBids = findViewById(R.id.recyclerBids);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        recyclerBids.setLayoutManager(new LinearLayoutManager(this));
        bidAdapter = new BidAdapter(bidList);
        recyclerBids.setAdapter(bidAdapter);

        cropId = getIntent().getStringExtra("cropId");

        if (cropId != null) loadCropDetails();
        else {
            Toast.makeText(this, "Invalid crop ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadCropDetails() {
        progressDetails.setVisibility(View.VISIBLE);

        db.collection("Crops").document(cropId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    progressDetails.setVisibility(View.GONE);
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "Crop not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    setCropDetails(snapshot);
                    loadAllBids();
                })
                .addOnFailureListener(e -> {
                    progressDetails.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setCropDetails(DocumentSnapshot s) {

        tvCropName.setText(s.getString("name"));
        tvPrice.setText("Base Price: ₹" + s.getDouble("pricePerKg"));
        tvQuantity.setText("Quantity: " + s.getDouble("quantityKg") + " kg");
        tvLocation.setText("Location: " + s.getString("location"));

        Timestamp harvestTs = s.getTimestamp("harvestDate");
        Timestamp biddingTs = s.getTimestamp("biddingEndDate");

        if (harvestTs != null)
            tvHarvestDate.setText("Harvest Date: " + harvestTs.toDate());

        if (biddingTs != null) {
            long endTime = biddingTs.toDate().getTime();
            tvBiddingEnd.setText("Bidding Ends: " + biddingTs.toDate());
            startCountdown(endTime);
        }

        tvFarmerName.setText("Farmer: " + s.getString("farmerName"));
        tvFarmerPhone.setText("📞 " + s.getString("farmerPhone"));

        String img = s.getString("imageUrl");
        if (img != null && !img.isEmpty()) {
            if (img.startsWith("/storage"))
                Glide.with(this).load(Uri.fromFile(new File(img))).into(imageCrop);
            else
                Glide.with(this).load(img).into(imageCrop);
        }

        Double highest = s.getDouble("highestBid");
        if (highest != null && highest > 0) {
            tvWinner.setVisibility(View.VISIBLE);
            tvWinner.setText("Highest Bid: ₹" + highest);
        }

        boolean isSold = s.getBoolean("isSold") != null && s.getBoolean("isSold");
        String winnerId = s.getString("winnerId");
        String farmerId = s.getString("farmerId");
        String farmerPhone = s.getString("farmerPhone");

        if (isSold && winnerId != null && winnerId.equals(currentUserId)) {
            btnPayNow.setVisibility(View.VISIBLE);
            btnPlaceBid.setVisibility(View.GONE);
            tvWinnerMessage.setVisibility(View.VISIBLE);

            btnPayNow.setText("Proceed to Delivery & Payment");

            btnPayNow.setOnClickListener(v -> {
                Intent i = new Intent(this, TransportationActivity.class);
                i.putExtra("cropId", cropId);
                i.putExtra("farmerId", farmerId);
                i.putExtra("farmerPhone", farmerPhone);
                i.putExtra("winnerName", mAuth.getCurrentUser().getDisplayName());
                startActivity(i);
            });

        } else {
            btnPayNow.setVisibility(View.GONE);
            tvWinnerMessage.setVisibility(View.GONE);
            btnPlaceBid.setVisibility(View.VISIBLE);
        }

        btnCallFarmer.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_DIAL);
            i.setData(Uri.parse("tel:" + s.getString("farmerPhone")));
            startActivity(i);
        });

        btnPlaceBid.setOnClickListener(v -> {
            String bidStr = ((EditText) findViewById(R.id.etBidAmount)).getText().toString().trim();
            if (bidStr.isEmpty()) {
                Toast.makeText(this, "Enter bid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double bidValue = Double.parseDouble(bidStr);
            submitBid(bidValue);
        });
    }

    private void startCountdown(long endTime) {

        if (countDownTimer != null)
            countDownTimer.cancel();

        long now = System.currentTimeMillis();
        long diff = endTime - now;

        if (diff <= 0) {
            tvCountdown.setText("Bidding closed");
            return;
        }

        countDownTimer = new CountDownTimer(diff, 1000) {
            @Override
            public void onTick(long ms) {
                long days = ms / (1000 * 60 * 60 * 24);
                long hours = (ms / (1000 * 60 * 60)) % 24;
                long minutes = (ms / (1000 * 60)) % 60;
                long seconds = (ms / 1000) % 60;

                tvCountdown.setText(
                        "Time left: " + days + "d " + hours + "h " + minutes + "m " + seconds + "s"
                );
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("Bidding closed");
            }
        }.start();
    }

    private void loadAllBids() {
        db.collection("Crops").document(cropId)
                .collection("Bids")
                .orderBy("bidValue", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(docs -> {
                    bidList.clear();
                    for (DocumentSnapshot d : docs.getDocuments()) {
                        Bid b = d.toObject(Bid.class);
                        if (b != null) bidList.add(b);
                    }
                    bidAdapter.notifyDataSetChanged();
                });
    }

    private void submitBid(double bidValue) {
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(u -> {

                    String name = u.exists() ? u.getString("name") : "Customer";

                    Bid bid = new Bid(currentUserId, name, bidValue, Timestamp.now());

                    db.collection("Crops").document(cropId)
                            .collection("Bids")
                            .add(bid)
                            .addOnSuccessListener(a -> {
                                Toast.makeText(this, "Bid placed!", Toast.LENGTH_SHORT).show();
                                loadCropDetails();
                            });
                });
    }

    public static class Bid {
        public String customerId;
        public String customerName;
        public double bidValue;
        public Timestamp timestamp;

        public Bid() {}
        public Bid(String id, String name, double val, Timestamp ts) {
            this.customerId = id;
            this.customerName = name;
            this.bidValue = val;
            this.timestamp = ts;
        }
    }

    class BidAdapter extends RecyclerView.Adapter<BidAdapter.Holder> {

        private final List<Bid> list;

        BidAdapter(List<Bid> list) { this.list = list; }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int pos) {
            Bid b = list.get(pos);
            h.t1.setText(b.customerName + " — ₹" + b.bidValue);
            h.t2.setText(b.timestamp.toDate().toString());
        }

        @Override
        public int getItemCount() { return list.size(); }

        class Holder extends RecyclerView.ViewHolder {
            TextView t1, t2;
            Holder(View v) {
                super(v);
                t1 = v.findViewById(android.R.id.text1);
                t2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}
