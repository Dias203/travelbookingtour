package com.example.travel_app.Activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Activity.admin.AdminMainActivity;
import com.example.travel_app.databinding.ActivityIntroBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class IntroActivity extends BaseActivity {
    ActivityIntroBinding binding;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        // Kiểm tra nếu người dùng đã đăng nhập
        if (fAuth.getCurrentUser() != null) {
            // Nếu người dùng đã đăng nhập, lấy UID và kiểm tra quyền truy cập
            String uid = fAuth.getCurrentUser().getUid();
            checkUserAccessLevel(uid);
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(IntroActivity.this, MainActivity.class));
                finish();
            }, 2000);
        }
    }

    private void checkUserAccessLevel(String uid) {
        DocumentReference df = fStore.collection("Users").document(uid);
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("TAG", "onSuccess: " + documentSnapshot.getData());

                // Kiểm tra xem người dùng có phải là admin không
                if (documentSnapshot.getBoolean("isAdmin") == Boolean.TRUE) {
                    // Nếu là admin, vào AdminMainActivity
                    startActivity(new Intent(getApplicationContext(), AdminMainActivity.class));
                    finish();
                } else {
                    // Nếu không phải admin, vào MainActivity
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        });
    }
}
