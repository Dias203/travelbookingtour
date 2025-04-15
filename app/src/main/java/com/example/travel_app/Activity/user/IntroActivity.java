package com.example.travel_app.Activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Activity.admin.AdminMainActivity;
import com.example.travel_app.databinding.ActivityIntroBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class IntroActivity extends BaseActivity {
    private static final String TAG = "IntroActivity";
    private static final int SPLASH_DELAY = 2000; // 2 giây

    private ActivityIntroBinding binding;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeViews();
        initializeFirebase();
        handleUserSession();
    }

    /**
     * Khởi tạo View Binding
     */
    private void initializeViews() {
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    /**
     * Khởi tạo Firebase Auth & Firestore
     */
    private void initializeFirebase() {
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
    }

    /**
     * Kiểm tra trạng thái đăng nhập của người dùng
     */
    private void handleUserSession() {
        FirebaseUser currentUser = fAuth.getCurrentUser();

        if (currentUser != null) {
            checkUserAccessLevel(currentUser.getUid());
        } else {
            delayAndNavigateToMain();
        }
    }

    /**
     * Đợi splash 2s rồi chuyển sang MainActivity
     */
    private void delayAndNavigateToMain() {
        new Handler(Looper.getMainLooper()).postDelayed(
                this::navigateToMainActivity,
                SPLASH_DELAY
        );
    }

    /**
     * Chuyển đến MainActivity
     */
    private void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Chuyển đến AdminMainActivity
     */
    private void navigateToAdminActivity() {
        startActivity(new Intent(this, AdminMainActivity.class));
        finish();
    }

    /**
     * Lấy thông tin người dùng từ Firestore và kiểm tra quyền admin
     */
    private void checkUserAccessLevel(String uid) {
        DocumentReference userRef = fStore.collection("Users").document(uid);

        userRef.get()
                .addOnSuccessListener(this::handleUserAccessCheck)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi truy vấn Firestore", e);
                    navigateToMainActivity();
                });
    }

    /**
     * Xử lý sau khi truy vấn Firestore thành công
     */
    private void handleUserAccessCheck(DocumentSnapshot documentSnapshot) {
        Log.d(TAG, "User data: " + documentSnapshot.getData());

        if (isAdminUser(documentSnapshot)) {
            navigateToAdminActivity();
        } else {
            navigateToMainActivity();
        }
    }

    /**
     * Kiểm tra quyền admin từ documentSnapshot
     */
    private boolean isAdminUser(DocumentSnapshot documentSnapshot) {
        return Boolean.TRUE.equals(documentSnapshot.getBoolean("isAdmin"));
    }
}
