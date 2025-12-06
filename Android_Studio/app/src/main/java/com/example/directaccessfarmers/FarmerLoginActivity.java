package com.example.directaccessfarmers;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FarmerLoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvSignup;
    ProgressBar progressBar;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        try {
            LanguageManager.loadLanguage(this);
        } catch (Exception e) {

        }

        setContentView(R.layout.activity_farmer_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> loginFarmer());

        tvSignup.setOnClickListener(v -> {
            Intent i = new Intent(FarmerLoginActivity.this, SignUpActivity.class);
            i.putExtra("role", "farmer");
            startActivity(i);
        });
    }

    private void loginFarmer() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Utils.isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> checkRole())
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkRole() {
        DocumentReference docRef = db.collection("users").document(auth.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            showLoading(false);
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");
                if ("farmer".equals(role)) {
                   Toast.makeText(this, "Welcome, Farmer!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, FarmerDashboard.class));
                    finish();
                } else {
                    Toast.makeText(this, "This account is not a farmer account!", Toast.LENGTH_LONG).show();
                    auth.signOut();
                }
            } else {
                Toast.makeText(this, "Account data not found. Please contact support.", Toast.LENGTH_LONG).show();
                auth.signOut();
            }
        }).addOnFailureListener(e -> {
            showLoading(false);
            Toast.makeText(this, "Failed to verify account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
        }
        btnLogin.setEnabled(!show);
    }
}
