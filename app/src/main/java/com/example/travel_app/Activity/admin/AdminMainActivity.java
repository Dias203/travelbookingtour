package com.example.travel_app.Activity.admin;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Activity.user.LoginActivity;
import com.example.travel_app.Activity.user.MainActivity;
import com.example.travel_app.databinding.ActivityAdminMainBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class AdminMainActivity extends BaseActivity {
    private ActivityAdminMainBinding binding;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupEventListeners();
    }

    /**
     * Thiết lập các sự kiện click cho các nút trong giao diện admin
     */
    private void setupEventListeners() {
        binding.adminAllTour.setOnClickListener(v -> navigateTo(AllTourActivity.class));
        binding.adminAddTour.setOnClickListener(v -> navigateTo(AdminAddTourActivity.class));
        binding.adminAllOrder.setOnClickListener(v -> navigateTo(AllOderActivity.class));
        binding.adminLogoutBtn.setOnClickListener(v -> logout());
        binding.adminChangeLanguage.setOnClickListener(v -> {
            String currentLanguage = loadLanguage();
            if (currentLanguage.equals("en")) {
                changeLanguage("vi"); // Chuyển sang tiếng Việt
            } else {
                changeLanguage("en"); // Chuyển sang tiếng Anh
            }
            // Khởi động lại ứng dụng để áp dụng ngôn ngữ cho tất cả Activity
            restartApp();
        });
    }

    /**
     * Điều hướng đến một activity khác
     * @param activityClass Lớp của activity cần chuyển đến
     */
    private void navigateTo(Class<?> activityClass) {
        startActivity(new Intent(getApplicationContext(), activityClass));
    }

    /**
     * Đăng xuất người dùng và quay về màn hình đăng nhập
     */
    private void logout() {
        auth.signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    private void changeLanguage(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        saveLanguage(language);
    }

    private void saveLanguage(String language) {
        getSharedPreferences("settings", MODE_PRIVATE)
                .edit()
                .putString("language", language)
                .apply();
    }

    private String loadLanguage() {
        return getSharedPreferences("settings", MODE_PRIVATE)
                .getString("language", "en"); // Mặc định là tiếng Anh
    }

    private void restartApp() {
        Intent intent = new Intent(this, AdminMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}