package com.example.travel_app.Activity.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.R;
import com.example.travel_app.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class ProfileActivity extends BaseActivity {
    private ActivityProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeView();
        initializeFirebase();
        checkAndDisplayUserInfo();
        setupButtonActions();
        initBottomNav();
    }

    /**
     * Khởi tạo view binding
     */
    private void initializeView() {
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    /**
     * Khởi tạo Firebase auth và lấy user hiện tại
     */
    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    /**
     * Kiểm tra trạng thái người dùng và hiển thị thông tin
     */
    private void checkAndDisplayUserInfo() {
        if (user == null) {
            redirectToLogin();
        } else {
            binding.textView12.setText(user.getEmail());
        }
    }

    /**
     * Gán sự kiện cho các nút bấm
     */
    private void setupButtonActions() {
        binding.buttonChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });

        binding.buttonLogout.setOnClickListener(v -> {
            auth.signOut();
            redirectToLogin();
        });
    }

    /**
     * Điều hướng về màn hình đăng nhập
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void initBottomNav() {
        binding.bottomnav.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                if (i == R.id.home) {
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);

                }
                else if (i == R.id.cart){
                    Intent intent = new Intent(ProfileActivity.this, PurchasedActivity.class);
                    startActivity(intent);
                }
                else if(i == R.id.explorer){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                    intent.setPackage("com.android.chrome"); // Đảm bảo rằng Chrome được sử dụng
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        // Xử lý trường hợp Chrome không được cài đặt
                        intent.setPackage(null); // Khôi phục về trình duyệt mặc định
                        startActivity(intent);
                    }
                }
            }
        });
    }
    /**
     * Mở trình duyệt với URL được chỉ định
     */
    private void openBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage("com.android.chrome");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            intent.setPackage(null);
            startActivity(intent);
        }
    }
}