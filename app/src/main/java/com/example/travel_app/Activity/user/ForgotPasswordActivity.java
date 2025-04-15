package com.example.travel_app.Activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgotPasswordActivity extends BaseActivity {
    private static final String TAG = "ForgotPasswordActivity";
    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeViews();        // Khởi tạo view binding
        initializeFirebase();     // Khởi tạo FirebaseAuth
        setupClickListeners();    // Thiết lập sự kiện click cho nút
    }

    /**
     * Khởi tạo view binding
     */
    private void initializeViews() {
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    /**
     * Khởi tạo Firebase Auth
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Thiết lập sự kiện click cho các nút
     */
    private void setupClickListeners() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.btnConfirm.setOnClickListener(v -> handleResetPassword());
    }

    /**
     * Xử lý yêu cầu đặt lại mật khẩu
     * Kiểm tra hợp lệ email và gửi email đặt lại nếu hợp lệ
     */
    private void handleResetPassword() {
        String email = getEmailInput();

        if (isValidEmail(email)) {
            sendPasswordResetEmail(email);  // Gửi email đặt lại mật khẩu
        } else {
            showToast("Vui lòng nhập đầy đủ thông tin");
        }
    }

    /**
     * Lấy địa chỉ email từ ô nhập liệu
     * @return Chuỗi email đã được loại bỏ khoảng trắng
     */
    private String getEmailInput() {
        return Objects.requireNonNull(binding.email.getText()).toString().trim();
    }

    /**
     * Kiểm tra email có hợp lệ hay không
     * @param email Địa chỉ email cần kiểm tra
     * @return True nếu hợp lệ, False nếu không
     */
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email);
    }

    /**
     * Gửi email đặt lại mật khẩu tới địa chỉ được cung cấp
     * @param email Địa chỉ email để gửi link đặt lại
     */
    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    showToast("Gửi thành công");
                    navigateToLogin(); // Chuyển về màn hình đăng nhập
                })
                .addOnFailureListener(e -> showToast("Gửi thất bại: " + e.getMessage()));
    }

    /**
     * Chuyển sang màn hình đăng nhập
     */
    private void navigateToLogin() {
        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Hiển thị thông báo dạng Toast
     * @param message Nội dung cần hiển thị
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
