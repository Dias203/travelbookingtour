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
    ActivityProfileBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        displayInfo();
        initBottomNav();

    }

    private void displayInfo() {

        if(user == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            binding.textView12.setText(user.getEmail());
        }

        binding.buttonChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), ChangePasswordActivity.class));
        });

        binding.buttonLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
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
                    Intent intent = new Intent(ProfileActivity.this, BookmarkActivity.class);
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
}