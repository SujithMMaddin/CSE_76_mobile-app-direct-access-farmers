package com.example.directaccessfarmers;

import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;

public class LanguageManager {

    public static void setLanguage(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration();
        configuration.setLocale(locale);

        context.getResources().updateConfiguration(configuration,
                context.getResources().getDisplayMetrics());


        Utils.setLanguage(context, languageCode);
    }

    public static void loadLanguage(Context context) {
        String languageCode = Utils.getLanguage(context);
        setLanguage(context, languageCode);
    }

    public static String[] getSupportedLanguages() {
        return new String[]{"en", "hi", "kn"};
    }

    public static String[] getSupportedLanguageNames() {
        return new String[]{"English", "हिंदी", "ಕನ್ನಡ"};
    }
}