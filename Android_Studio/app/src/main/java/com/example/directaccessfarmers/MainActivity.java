package com.example.directaccessfarmers;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button btnFarmerLogin, btnCustomerLogin;
    private String[] languages = {"English", "हिंदी", "ಕನ್ನಡ"};
    private String[] languageCodes = {"en", "hi", "kn"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            LanguageManager.loadLanguage(this);
        } catch (Exception e) {
            // Continue with default language
        }
        Log.d(TAG, "onCreate: Starting MainActivity");

        // Load saved language with proper error handling
        try {
            LanguageManager.loadLanguage(this);
            Log.d(TAG, "Language loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error loading language: " + e.getMessage(), e);
            // Continue with default language
        }

        setContentView(R.layout.activity_main);
        Log.d(TAG, "Content view set successfully");

        // Initialize toolbar with null check
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                Log.d(TAG, "Toolbar set successfully");
            } else {
                Log.w(TAG, "Toolbar not found in layout");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar: " + e.getMessage(), e);
        }

        // Initialize buttons with null checks
        try {
            btnFarmerLogin = findViewById(R.id.btnFarmerLogin);
            btnCustomerLogin = findViewById(R.id.btnCustomerLogin);

            if (btnFarmerLogin == null) {
                Log.e(TAG, "btnFarmerLogin not found in layout");
                Toast.makeText(this, "Layout error: Farmer login button not found", Toast.LENGTH_LONG).show();
                return;
            }

            if (btnCustomerLogin == null) {
                Log.e(TAG, "btnCustomerLogin not found in layout");
                Toast.makeText(this, "Layout error: Customer login button not found", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d(TAG, "Buttons found successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error finding buttons: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing interface", Toast.LENGTH_LONG).show();
            return;
        }

        // Set click listeners with error handling
        try {
            btnFarmerLogin.setOnClickListener(v -> {
                Log.d(TAG, "Farmer login button clicked");
                try {
                    Intent intent = new Intent(MainActivity.this, FarmerLoginActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting FarmerLoginActivity: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Error opening farmer login", Toast.LENGTH_SHORT).show();
                }
            });

            btnCustomerLogin.setOnClickListener(v -> {
                Log.d(TAG, "Customer login button clicked");
                try {
                    Intent intent = new Intent(MainActivity.this, CustomerLoginActivity.class);
                    Log.d(TAG, "Intent created, starting activity");
                    startActivity(intent);
                    Log.d(TAG, "CustomerLoginActivity started successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Error starting CustomerLoginActivity: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, "Error opening customer login: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            Log.d(TAG, "Click listeners set successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting click listeners: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up buttons", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.main_menu, menu);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error creating options menu: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            if (item.getItemId() == R.id.menu_language) {
                showLanguageDialog();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling menu item: " + e.getMessage(), e);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLanguageDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Language / भाषा चुनें / ಭಾಷೆ ಆಯ್ಕೆಮಾಡಿ");
            builder.setItems(languages, (dialog, which) -> {
                try {
                    LanguageManager.setLanguage(this, languageCodes[which]);
                    recreate(); // refresh activity with new language
                } catch (Exception e) {
                    Log.e(TAG, "Error changing language: " + e.getMessage(), e);
                    Toast.makeText(this, "Error changing language", Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing language dialog: " + e.getMessage(), e);
            Toast.makeText(this, "Error showing language options", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            invalidateOptionsMenu(); // ensures menu is refreshed after recreate
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
        }
    }
}