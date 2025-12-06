package com.example.directaccessfarmers;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;   // ✅ ADDED
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class FarmerDashboard extends AppCompatActivity {

    private EditText etCropName, etQuantity, etPrice;
    private AutoCompleteTextView etLocation;
    private TextView tvHarvestDate, tvBiddingEndDate;
    private Button btnSelectImage, btnAddCrop, btnCheckMarketPrice, btnViewBids;
    private ImageView imagePreview;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Uri selectedImageUri = null;

    // ✅ Keep old format for pure dates (harvest date)
    private final SimpleDateFormat serverDateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // ✅ New format for date + time (bidding end)
    private final SimpleDateFormat serverDateTimeFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    private static final String[] LOCATION_SUGGESTIONS = {
            "Bengaluru Urban", "Bengaluru Rural", "Mysuru", "Mandya", "Tumakuru",
            "Chikkamagaluru", "Belagavi", "Kalaburagi", "Bidar", "Hassan"
    };

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imagePreview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            LanguageManager.loadLanguage(this);
        } catch (Exception e) {
            // Continue with default language
        }
        setContentView(R.layout.activity_farmer_dashboard);

        // Firebase setup
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Bind views
        etCropName = findViewById(R.id.etCropName);
        etQuantity = findViewById(R.id.etQuantity);
        etPrice = findViewById(R.id.etPrice);
        etLocation = findViewById(R.id.etLocation);
        tvHarvestDate = findViewById(R.id.etHarvestDate);
        tvBiddingEndDate = findViewById(R.id.etBiddingLastDate);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnAddCrop = findViewById(R.id.btnAddCrop);
        btnCheckMarketPrice = findViewById(R.id.btnCheckMarketPrice);
        imagePreview = findViewById(R.id.imagePreview);
        progressBar = findViewById(R.id.progressBar);
        btnViewBids = findViewById(R.id.btnViewBids);

        // Auto-suggest locations
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, LOCATION_SUGGESTIONS);
        etLocation.setAdapter(adapter);
        etLocation.setThreshold(1);

        // 📅 Harvest date → only date
        tvHarvestDate.setOnClickListener(v -> showDatePicker(tvHarvestDate));

        // 📅⏰ Bidding end → date + time
        tvBiddingEndDate.setOnClickListener(v -> showDatePicker(tvBiddingEndDate));

        btnSelectImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnAddCrop.setOnClickListener(v -> validateAndUploadCrop());

        // 🌾 Open Market Price (AI) activity
        btnCheckMarketPrice.setOnClickListener(v ->
                startActivity(new Intent(FarmerDashboard.this, MarketPriceActivity.class)));

        // 📊 View Bids / My Crops
        btnViewBids.setOnClickListener(v -> {
            Intent i = new Intent(FarmerDashboard.this, FarmerMyCropsActivity.class);
            startActivity(i);
        });
    }

    /**
     * Date picker
     * - For harvest date: only date
     * - For bidding end: date + time (via extra TimePicker)
     */
    private void showDatePicker(final TextView targetView) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar chosen = Calendar.getInstance();
                    chosen.set(year, month, dayOfMonth);

                    if (targetView.getId() == R.id.etBiddingLastDate) {
                        // ✅ For bidding end, also ask time
                        showTimePickerForBidding(chosen, targetView);
                    } else {
                        // Harvest date → just date
                        targetView.setText(serverDateFormat.format(chosen.getTime()));
                    }
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show();
    }

    /**
     * Time picker only for bidding end date
     */
    private void showTimePickerForBidding(Calendar dateCal, TextView targetView) {
        int hour = dateCal.get(Calendar.HOUR_OF_DAY);
        int minute = dateCal.get(Calendar.MINUTE);

        TimePickerDialog tpd = new TimePickerDialog(
                this,
                (timePicker, selHour, selMinute) -> {
                    dateCal.set(Calendar.HOUR_OF_DAY, selHour);
                    dateCal.set(Calendar.MINUTE, selMinute);
                    dateCal.set(Calendar.SECOND, 0);

                    // Display as "yyyy-MM-dd HH:mm"
                    targetView.setText(serverDateTimeFormat.format(dateCal.getTime()));
                },
                hour,
                minute,
                true
        );
        tpd.show();
    }

    private void validateAndUploadCrop() {
        String cropName = etCropName.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String harvestDateStr = tvHarvestDate.getText().toString().trim();
        String biddingEndDateStr = tvBiddingEndDate.getText().toString().trim();

        if (TextUtils.isEmpty(cropName) || TextUtils.isEmpty(quantityStr)
                || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(location)
                || TextUtils.isEmpty(harvestDateStr) || TextUtils.isEmpty(biddingEndDateStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double quantity, price;
        try {
            quantity = Double.parseDouble(quantityStr);
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity or price", Toast.LENGTH_SHORT).show();
            return;
        }

        Date harvestDate;
        Date biddingEndDate;

        try {
            // ✅ Harvest date: still only date
            harvestDate = serverDateFormat.parse(harvestDateStr);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid harvest date format", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // ✅ First try parse with date+time (what we now set for bidding end)
            biddingEndDate = serverDateTimeFormat.parse(biddingEndDateStr);
        } catch (Exception e1) {
            try {
                // Fallback: if somehow only date was entered
                biddingEndDate = serverDateFormat.parse(biddingEndDateStr);
            } catch (Exception e2) {
                Toast.makeText(this, "Invalid bidding end date format", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String cropId = db.collection("Crops").document().getId();

        Map<String, Object> cropMap = new HashMap<>();
        cropMap.put("id", cropId);
        cropMap.put("farmerId", mAuth.getUid());
        cropMap.put("name", cropName);
        cropMap.put("pricePerKg", price);
        cropMap.put("quantityKg", quantity);
        cropMap.put("location", location);
        cropMap.put("harvestDate", new Timestamp(harvestDate));
        cropMap.put("biddingEndDate", new Timestamp(biddingEndDate)); // ✅ FULL timestamp
        cropMap.put("isSold", false);
        cropMap.put("winnerId", "");
        cropMap.put("highestBid", 0.0);
        cropMap.put("createdAt", new Timestamp(new Date()));

        progressBar.setVisibility(View.VISIBLE);
        btnAddCrop.setEnabled(false);

        // Save image locally
        if (selectedImageUri != null) {
            String localPath = saveImageLocally(selectedImageUri, cropId);
            cropMap.put("imageUrl", localPath);
        } else {
            cropMap.put("imageUrl", "");
        }

        saveCropToFirestore(cropId, cropMap);
    }

    private String saveImageLocally(Uri imageUri, String cropId) {
        try {
            File folder = new File(getExternalFilesDir("Images"), "");
            if (!folder.exists()) folder.mkdirs();

            File imageFile = new File(folder, "crop_" + cropId + ".jpg");
            InputStream input = getContentResolver().openInputStream(imageUri);
            FileOutputStream output = new FileOutputStream(imageFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            input.close();
            output.close();

            Toast.makeText(this, "✅ Image saved to: " + imageFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            Toast.makeText(this, "❌ Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    private void saveCropToFirestore(String cropId, Map<String, Object> cropMap) {
        String farmerId = mAuth.getUid();

        db.collection("users").document(farmerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        cropMap.put("farmerName", snapshot.getString("name"));
                        cropMap.put("farmerPhone", snapshot.getString("phone"));
                    }

                    db.collection("Crops").document(cropId)
                            .set(cropMap)
                            .addOnSuccessListener(unused -> {
                                progressBar.setVisibility(View.GONE);
                                btnAddCrop.setEnabled(true);
                                Toast.makeText(this, "🌾 Crop added successfully!", Toast.LENGTH_SHORT).show();
                                clearForm();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnAddCrop.setEnabled(true);
                                Toast.makeText(this, "Failed to add crop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void clearForm() {
        etCropName.setText("");
        etQuantity.setText("");
        etPrice.setText("");
        etLocation.setText("");
        tvHarvestDate.setText("Harvest Date");
        tvBiddingEndDate.setText("Bidding End Date");
        imagePreview.setImageResource(R.drawable.ic_launcher_background);
        selectedImageUri = null;
    }
}
