package com.example.directaccessfarmers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import java.util.Locale;

public class Utils {

    private static final String PREF_NAME = "DirectAccessFarmers";
    private static final String KEY_LANGUAGE = "selected_language";

    public static String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "₹%.2f", amount);
    }

    public static boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPrice(double price) {
        return price > 0 && price <= 100000;
    }

    public static boolean isValidQuantity(double quantity) {
        return quantity > 0 && quantity <= 10000;
    }

    // FIXED METHOD - This was the issue
    public static void setLanguage(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    // FIXED METHOD - This was the issue
    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "en");
    }
}
