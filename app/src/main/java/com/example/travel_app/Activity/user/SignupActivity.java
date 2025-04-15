package com.example.travel_app.Activity.user;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.databinding.ActivitySignupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends BaseActivity {
    private ActivitySignupBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            navigateTo(ProfileActivity.class);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeView();
        initFirebase();
        initListeners();

        // Kiểm tra kết nối khi tạo Activity
        checkNetworkAndHandleUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra kết nối mỗi khi quay lại màn hình
        checkNetworkAndHandleUI();
    }

    /**
     * Khởi tạo view binding
     */
    private void initializeView() {
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    /**
     * Khởi tạo Firebase
     */
    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    /**
     * Gán sự kiện click cho các nút
     */
    private void initListeners() {
        binding.loginNow.setOnClickListener(v -> navigateTo(LoginActivity.class));

        binding.btnRegister.setOnClickListener(v -> {
            String email = binding.email.getText().toString().trim();
            String password = binding.password.getText().toString().trim();

            if (isValidInput(email, password)) {
                registerUser(email, password);
            }
        });
    }

    /**
     * Kiểm tra input hợp lệ
     */
    private boolean isValidInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            showToast("Nhập địa chỉ email!");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            showToast("Nhập password!");
            return false;
        }
        return true;
    }

    /**
     * Đăng ký người dùng mới
     */
    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    saveUserToFirestore(user, email, password);
                    showToast("Tạo tài khoản thành công!");
                    navigateTo(MainActivity.class);
                })
                .addOnFailureListener(e -> showToast("Tạo tài khoản thất bại"));
    }

    /**
     * Lưu thông tin người dùng vào Firestore
     */
    private void saveUserToFirestore(FirebaseUser user, String email, String password) {
        DocumentReference userRef = fStore.collection("Users").document(user.getUid());
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("UserEmail", email);
        userInfo.put("Password", password);
        userInfo.put("isAdmin", false);
        userRef.set(userInfo);
    }

    /**
     * Hiển thị thông báo ngắn
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Điều hướng tới một Activity mới và kết thúc Activity hiện tại
     */
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        finish();
    }

    /**
     * Kiểm tra kết nối mạng
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * Kiểm tra mạng và xử lý giao diện
     */
    private void checkNetworkAndHandleUI() {
        if (!isNetworkConnected()) {
            showToast("Không có kết nối mạng. Vui lòng kiểm tra lại.");
            binding.btnRegister.setEnabled(false);
        } else {
            binding.btnRegister.setEnabled(true);
        }
    }
}
