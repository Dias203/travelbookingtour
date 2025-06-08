package com.example.travel_app;

import android.app.Application;
import android.content.res.Configuration;

import java.util.Locale;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        applySavedLanguage();
    }

    private void applySavedLanguage() {
        String language = getSharedPreferences("settings", MODE_PRIVATE)
                .getString("language", "en"); // Mặc định tiếng Anh
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}