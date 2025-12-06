package com.example.directaccessfarmers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CustomerLoginActivity extends AppCompatActivity {

    private static final String TAG = "CustomerLoginActivity";

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView signupLink;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try { LanguageManager.loadLanguage(this); } catch (Exception ignored) {}

        setContentView(R.layout.activity_customer_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signupLink = findViewById(R.id.signupLink);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {

        // Signup link
        signupLink.setOnClickListener(v ->
                startActivity(new Intent(this, CustomerRegisterActivity.class)));

        // Login button
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!validateInput(email, password)) return;
            loginUser(email, password);
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showError("Enter email and password");
            return false;
        }

        if (!Utils.isValidEmail(email)) {
            showError("Invalid email format");
            return false;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }

    private void loginUser(String email, String password) {
        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        showError("Login error");
                        showLoading(false);
                        return;
                    }

                    String uid = user.getUid();

                    // Check role in Firestore
                    db.collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(doc -> {
                                showLoading(false);

                                if (!doc.exists()) {
                                    showError("User data missing");
                                    mAuth.signOut();
                                    return;
                                }

                                String role = doc.getString("role");

                                if ("customer".equals(role)) {
                                    startActivity(new Intent(this, CustomerDashboard.class));
                                    finish();
                                } else {
                                    showError("This account is not a customer account");
                                    mAuth.signOut();
                                }
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                showError("Error fetching user data");
                            });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showError("Login failed: " + e.getMessage());
                });
    }

    private void showLoading(boolean show) {
        if (progressBar != null)
            progressBar.setVisibility(show ? ProgressBar.VISIBLE : ProgressBar.GONE);

        if (loginButton != null)
            loginButton.setEnabled(!show);
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // DO NOT auto redirect to dashboard
    }
}
