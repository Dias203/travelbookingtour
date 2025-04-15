package com.example.travel_app.Activity.admin;

import android.content.Intent;
import android.os.Bundle;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Activity.user.LoginActivity;
import com.example.travel_app.databinding.ActivityAdminMainBinding;
import com.google.firebase.auth.FirebaseAuth;

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
        binding.adminAddTour.setOnClickListener(v -> navigateTo(AdminAddTour.class));
        binding.adminAllOrder.setOnClickListener(v -> navigateTo(AllOderActivity.class));
        binding.adminLogoutBtn.setOnClickListener(v -> logout());
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
}