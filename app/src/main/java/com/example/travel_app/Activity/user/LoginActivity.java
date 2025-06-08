package com.example.travel_app.Activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Activity.admin.AdminMainActivity;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;

    @Override
    public void onStart() {
        super.onStart();
        checkIfUserIsLoggedIn();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeViews();
        initializeFirebase();
        setupClickListeners();
    }

    private void initializeViews() {
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    private void setupClickListeners() {
        binding.registerNow.setOnClickListener(view -> navigateToSignup());
        binding.btnLogin.setOnClickListener(view -> attemptLogin());
        binding.backBtn.setOnClickListener(view -> finish());
        binding.facebookBtn.setOnClickListener(view -> handleFacebookLogin());
        binding.twitterBtn.setOnClickListener(view -> handleTwitterLogin());
        binding.forgotPasswordBtn.setOnClickListener(v -> navigateToForgotPassword());
    }

    private void checkIfUserIsLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserAccessLevel(currentUser.getUid());
        }
    }

    private void navigateToSignup() {
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToForgotPassword() {
        startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));
    }

    private void attemptLogin() {
        showProgressBar();
        String email = getEmailInput();
        String password = getPasswordInput();

        if (!validateInputs(email, password)) {
            hideProgressBar();
            return;
        }

        performLogin(email, password);
    }

    private void showProgressBar() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        binding.progressBar.setVisibility(View.GONE);
    }

    private String getEmailInput() {
        return String.valueOf(binding.email.getText());
    }

    private String getPasswordInput() {
        return String.valueOf(binding.password.getText());
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            showToast("Nhập địa chỉ email!");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            showToast("Nhập mật khẩu!");
            return false;
        }
        return true;
    }

    private void performLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    showToast("Đăng nhập thành công!");
                    checkUserAccessLevel(Objects.requireNonNull(authResult.getUser()).getUid());
                })
                .addOnFailureListener(e -> {
                    hideProgressBar();
                    showToast("Sai tên đăng nhập hoặc mật khẩu!");
                });
    }

    private void checkUserAccessLevel(String uid) {
        DocumentReference df = fStore.collection("Users").document(uid);
        df.get().addOnSuccessListener(documentSnapshot -> {
            Log.d(TAG, "onSuccess: " + documentSnapshot.getData());
            if (isAdminUser(documentSnapshot)) {
                navigateToAdminDashboard();
            } else {
                navigateToCallerActivity();
            }
        });
    }

    private boolean isAdminUser(DocumentSnapshot documentSnapshot) {
        return documentSnapshot.getBoolean("isAdmin") == Boolean.TRUE;
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(getApplicationContext(), AdminMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void navigateToCallerActivity() {
        String caller = getIntent().getStringExtra("caller");
        Intent intent;

        if ("TicketActivity".equals(caller)) {
            intent = new Intent(getApplicationContext(), TicketActivity.class);
            ItemDomain object = (ItemDomain) getIntent().getSerializableExtra("object");
            if (object != null) {
                intent.putExtra("object", object);
            }
        } else if ("PurchasedActivity".equals(caller)) {
            intent = new Intent(getApplicationContext(), PurchasedActivity.class);
        } else {
            intent = new Intent(getApplicationContext(), MainActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setResult(RESULT_OK, intent); // Trả về kết quả cho activity gọi
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void handleFacebookLogin() {
        // Triển khai đăng nhập bằng Facebook trong tương lai
    }

    private void handleTwitterLogin() {
        // Triển khai đăng nhập bằng Twitter trong tương lai
    }
}