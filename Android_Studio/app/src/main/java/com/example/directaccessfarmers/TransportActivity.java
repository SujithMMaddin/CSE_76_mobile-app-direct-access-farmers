package com.example.directaccessfarmers;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TransportActivity extends AppCompatActivity {

    private EditText etDriverName, etVehicleNo;
    private Spinner spStatus;
    private Button btnUpdate;
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
        setContentView(R.layout.activity_transport);

        etDriverName = findViewById(R.id.etDriverName);
        etVehicleNo = findViewById(R.id.etVehicleNo);
        spStatus = findViewById(R.id.spStatus);
        btnUpdate = findViewById(R.id.btnUpdate);

        db = FirebaseFirestore.getInstance();
        cropId = getIntent().getStringExtra("cropId");

        String[] statuses = {"Pending", "Picked Up", "In Transit", "Delivered"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(adapter);

        btnUpdate.setOnClickListener(v -> updateTransport());
    }

    private void updateTransport() {
        String driver = etDriverName.getText().toString().trim();
        String vehicle = etVehicleNo.getText().toString().trim();
        String status = spStatus.getSelectedItem().toString();

        if (driver.isEmpty() || vehicle.isEmpty()) {
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> transport = new HashMap<>();
        transport.put("driverName", driver);
        transport.put("vehicleNo", vehicle);
        transport.put("status", status);
        transport.put("timestamp", new Timestamp(new Date()));

        db.collection("Crops").document(cropId)
                .collection("Transport").add(transport)
                .addOnSuccessListener(doc -> Toast.makeText(this, "✅ Transport Updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
