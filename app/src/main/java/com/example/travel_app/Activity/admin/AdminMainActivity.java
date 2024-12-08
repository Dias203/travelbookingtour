package com.example.travel_app.Activity.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Activity.user.LoginActivity;
import com.example.travel_app.R;
import com.example.travel_app.databinding.ActivityAdminMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class AdminMainActivity extends BaseActivity {
    ActivityAdminMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.adminAllTour.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), AllTourActivity.class));
        });

        binding.adminAddTour.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), AdminAddTour.class));
        });

        binding.adminAllOrder.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), AllOderActivity.class));
        });

        binding.adminLogoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

}