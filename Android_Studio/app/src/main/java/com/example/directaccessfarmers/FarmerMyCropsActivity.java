package com.example.directaccessfarmers;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FarmerMyCropsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private List<CropItem> cropList;
    private FarmerCropListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try { LanguageManager.loadLanguage(this); } catch (Exception ignored) {}

        setContentView(R.layout.activity_farmer_my_crops);

        recyclerView = findViewById(R.id.recyclerMyCrops);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        cropList = new ArrayList<>();
        adapter = new FarmerCropListAdapter(cropList);
        recyclerView.setAdapter(adapter);

        loadFarmerCrops();
    }

    private void loadFarmerCrops() {
        String farmerId = mAuth.getUid();
        if (farmerId == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("Crops")
                .whereEqualTo("farmerId", farmerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    cropList.clear();
                    long now = System.currentTimeMillis();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Crop crop = doc.toObject(Crop.class);
                        if (crop != null) {
                            crop.setId(doc.getId());

                            // read "sold" flag
                            Boolean sold = doc.getBoolean("sold");

                            // read bidding end from Firestore (can be Long or Timestamp)
                            long endTime = 0L;
                            Object endObj = doc.get("biddingEndDate"); // 🔁 adjust field name if different

                            if (endObj instanceof Long) {
                                endTime = (Long) endObj;
                            } else if (endObj instanceof Timestamp) {
                                endTime = ((Timestamp) endObj).toDate().getTime();
                            }

                            // ACTIVE if: time not over AND not sold
                            boolean isActive = (endTime > now) && (sold == null || !sold);

                            cropList.add(new CropItem(crop, isActive));
                        }
                    }

                    // Active crops first
                    Collections.sort(cropList, (a, b) -> {
                        if (a.isActive && !b.isActive) return -1;
                        if (!a.isActive && b.isActive) return 1;
                        return 0;
                    });

                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(cropList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load crops: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Wrapper model for crop + status
    static class CropItem {
        Crop crop;
        boolean isActive;

        CropItem(Crop crop, boolean isActive) {
            this.crop = crop;
            this.isActive = isActive;
        }
    }

    // ---------------------------------------------------
    // ADAPTER
    // ---------------------------------------------------
    class FarmerCropListAdapter extends RecyclerView.Adapter<FarmerCropListAdapter.Holder> {

        private final List<CropItem> list;

        FarmerCropListAdapter(List<CropItem> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {

            CropItem item = list.get(position);
            Crop crop = item.crop;

            holder.tv1.setText(crop.getName() + " — ₹" + crop.getPricePerKg() + "/kg");

            String status = item.isActive ? "(ACTIVE)" : "(CLOSED)";
            holder.tv2.setText("Location: " + crop.getLocation() + "  " + status);

            holder.itemView.setBackgroundColor(item.isActive ? 0xFFE0F7FA : 0xFFFFFFFF);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(FarmerMyCropsActivity.this, FarmerCropBidsActivity.class);
                intent.putExtra(FarmerCropBidsActivity.EXTRA_CROP_ID, crop.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView tv1, tv2;

            Holder(@NonNull View itemView) {
                super(itemView);
                tv1 = itemView.findViewById(android.R.id.text1);
                tv2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
