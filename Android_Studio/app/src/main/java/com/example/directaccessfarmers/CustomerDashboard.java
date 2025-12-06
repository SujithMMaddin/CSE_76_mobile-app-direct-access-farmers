package com.example.directaccessfarmers;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;

import java.util.*;

public class CustomerDashboard extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private AutoCompleteTextView actvLocation;
    private CropAdapterCustomer adapter;
    private final List<Crop> cropList = new ArrayList<>();
    private final List<Crop> filteredList = new ArrayList<>();

    private FirebaseFirestore db;
    private ListenerRegistration cropsListener;

    private static final String[] LOCATION_SUGGESTIONS = {
            "Bengaluru", "Mysuru", "Hubballi", "Mandya", "Tumakuru",
            "Belagavi", "Kalaburagi", "Bidar", "Hassan", "Mangaluru"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            LanguageManager.loadLanguage(this);
        } catch (Exception e) {
            // Continue with default language
        }
        setContentView(R.layout.activity_customer_dashboard);

        recyclerView = findViewById(R.id.recyclerViewCrops);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        actvLocation = findViewById(R.id.actvLocation);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CropAdapterCustomer(this, filteredList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        ArrayAdapter<String> locAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, LOCATION_SUGGESTIONS);
        actvLocation.setAdapter(locAdapter);

        actvLocation.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            filterByLocation(selected);
        });

        actvLocation.setOnDismissListener(() -> {
            String txt = actvLocation.getText().toString().trim();
            if (TextUtils.isEmpty(txt)) {
                resetFilter();
            }
        });

        loadCropsRealtime();
    }

    private void loadCropsRealtime() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // ✅ Safer query — avoids index errors
        Query q = db.collection("Crops")
                .orderBy("createdAt", Query.Direction.DESCENDING);

        cropsListener = q.addSnapshotListener((snapshots, e) -> {
            progressBar.setVisibility(View.GONE);

            if (e != null) {
                Toast.makeText(CustomerDashboard.this,
                        "Error loading crops: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (snapshots == null || snapshots.isEmpty()) {
                cropList.clear();
                filteredList.clear();
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(View.VISIBLE);
                return;
            }

            cropList.clear();

            for (DocumentSnapshot ds : snapshots.getDocuments()) {
                try {
                    Crop c = ds.toObject(Crop.class);
                    if (c != null) {
                        if (c.getId() == null || c.getId().isEmpty()) {
                            c.setId(ds.getId());
                        }

                        // Only add active crops
                        if (!c.isSold()) {
                            cropList.add(c);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            syncFilterAfterLoad();
        });
    }

    private void syncFilterAfterLoad() {
        filteredList.clear();
        long now = System.currentTimeMillis();

        for (Crop c : cropList) {
            Timestamp endTs = c.getBiddingEndDate();

            // show crops only if bidding is still active
            if (endTs == null || endTs.toDate().getTime() > now) {
                filteredList.add(c);
            }
        }

        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void filterByLocation(String location) {
        filteredList.clear();
        long now = System.currentTimeMillis();

        for (Crop c : cropList) {
            if (c.getLocation() != null &&
                    c.getLocation().toLowerCase().contains(location.toLowerCase())) {

                Timestamp endTs = c.getBiddingEndDate();
                if (endTs == null || endTs.toDate().getTime() > now) {
                    filteredList.add(c);
                }
            }
        }

        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void resetFilter() {
        syncFilterAfterLoad();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cropsListener != null) cropsListener.remove();
        if (adapter != null) adapter.cleanup();
    }
}
