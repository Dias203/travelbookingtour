//package com.example.travel_app.Activity.user;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//
//import com.example.travel_app.Activity.BaseActivity;
//import com.example.travel_app.R;
//import com.example.travel_app.databinding.ActivityProfileBinding;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.ismaeldivita.chipnavigation.ChipNavigationBar;
//
//public class ProfileActivity extends BaseActivity {
//    private ActivityProfileBinding binding;
//    private FirebaseAuth auth;
//    private FirebaseUser user;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        initializeView();
//        initializeFirebase();
//        checkAndDisplayUserInfo();
//        setupButtonActions();
//        initBottomNav();
//    }
//
//    /**
//     * Khởi tạo view binding
//     */
//    private void initializeView() {
//        binding = ActivityProfileBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//    }
//
//    /**
//     * Khởi tạo Firebase auth và lấy user hiện tại
//     */
//    private void initializeFirebase() {
//        auth = FirebaseAuth.getInstance();
//        user = auth.getCurrentUser();
//    }
//
//    /**
//     * Kiểm tra trạng thái người dùng và hiển thị thông tin
//     */
//    private void checkAndDisplayUserInfo() {
//        if (user == null) {
//            redirectToLogin();
//        } else {
//            binding.textView12.setText(user.getEmail());
//            binding.textView12.setSelected(true);
//        }
//    }
//
//    /**
//     * Gán sự kiện cho các nút bấm
//     */
//    private void setupButtonActions() {
//        binding.buttonChangePassword.setOnClickListener(v -> {
//            startActivity(new Intent(this, ChangePasswordActivity.class));
//        });
//
//        binding.buttonChangeLanguage.setOnClickListener(v -> {
//            // xử lý thay đổi ngôn ngữ
//        });
//
//        binding.buttonLogout.setOnClickListener(v -> {
//            auth.signOut();
//            redirectToLogin();
//        });
//    }
//
//    /**
//     * Điều hướng về màn hình đăng nhập
//     */
//    private void redirectToLogin() {
//        Intent intent = new Intent(this, LoginActivity.class);
//        startActivity(intent);
//        finish();
//    }
//
//    private void initBottomNav() {
//        binding.bottomnav.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(int i) {
//                if (i == R.id.home) {
//                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
//                    startActivity(intent);
//
//                }
//                else if (i == R.id.cart){
//                    Intent intent = new Intent(ProfileActivity.this, PurchasedActivity.class);
//                    startActivity(intent);
//                }
//                else if(i == R.id.explorer){
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
//                    intent.setPackage("com.android.chrome"); // Đảm bảo rằng Chrome được sử dụng
//                    if (intent.resolveActivity(getPackageManager()) != null) {
//                        startActivity(intent);
//                    } else {
//                        // Xử lý trường hợp Chrome không được cài đặt
//                        intent.setPackage(null); // Khôi phục về trình duyệt mặc định
//                        startActivity(intent);
//                    }
//                }
//            }
//        });
//        binding.bottomnav.setItemSelected(R.id.profile, true);
//    }
//    /**
//     * Mở trình duyệt với URL được chỉ định
//     */
//    private void openBrowser(String url) {
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//        intent.setPackage("com.android.chrome");
//
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivity(intent);
//        } else {
//            intent.setPackage(null);
//            startActivity(intent);
//        }
//    }
//}

package com.example.travel_app.Activity.user;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.R;
import com.example.travel_app.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.Locale;

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

    private void initializeView() {
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    private void checkAndDisplayUserInfo() {
        if (user == null) {
            redirectToLogin();
        } else {
            binding.textView12.setText(user.getEmail());
            binding.textView12.setSelected(true);
        }
    }

    private void setupButtonActions() {
        binding.buttonChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });

        binding.buttonLogout.setOnClickListener(v -> {
            auth.signOut();
            redirectToLogin();
        });

        binding.buttonChangeLanguage.setOnClickListener(v -> {
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
                } else if (i == R.id.cart) {
                    Intent intent = new Intent(ProfileActivity.this, PurchasedActivity.class);
                    startActivity(intent);
                } else if (i == R.id.explorer) {
                    openBrowser("http://www.google.com");
                }
            }
        });
        binding.bottomnav.setItemSelected(R.id.profile, true);
    }

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
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}