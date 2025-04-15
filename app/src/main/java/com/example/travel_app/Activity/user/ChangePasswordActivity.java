package com.example.travel_app.Activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.R;
import com.example.travel_app.databinding.ActivityChangePasswordAcitivityBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity cho phép người dùng thay đổi mật khẩu tài khoản
 */
public class ChangePasswordActivity extends BaseActivity {
    private ActivityChangePasswordAcitivityBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        setupFirebase();
        checkUser();
        setupPasswordListener();
        setupButtons();
    }

    /**
     * Khởi tạo giao diện người dùng
     */
    private void initViews() {
        binding = ActivityChangePasswordAcitivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        hideConfirmIcon();
    }

    /**
     * Thiết lập kết nối với Firebase
     */
    private void setupFirebase() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    /**
     * Kiểm tra thông tin người dùng hiện tại
     */
    private void checkUser() {
        if (user == null || user.getEmail() == null) {
            showErrorAndExit();
        }
    }

    /**
     * Hiển thị thông báo lỗi và đóng màn hình
     */
    private void showErrorAndExit() {
        Toast.makeText(this, "Lỗi xác thực. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    /**
     * Ẩn icon xác nhận ban đầu
     */
    private void hideConfirmIcon() {
        binding.confirmNewPassword.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    /**
     * Thiết lập lắng nghe thay đổi mật khẩu
     */
    private void setupPasswordListener() {
        binding.confirmNewPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateConfirmStatus();
            }
        });
    }

    /**
     * Cập nhật trạng thái xác nhận mật khẩu
     */
    private void updateConfirmStatus() {
        String newPass = binding.newPassword.getText().toString().trim();
        String confirmPass = binding.confirmNewPassword.getText().toString().trim();

        if (isPasswordMatch(newPass, confirmPass)) {
            showMatchIcon();
        } else {
            showMismatchIcon();
        }
    }

    /**
     * Kiểm tra mật khẩu có khớp nhau không
     */
    private boolean isPasswordMatch(String newPass, String confirmPass) {
        return newPass.equals(confirmPass) && !confirmPass.isEmpty();
    }

    /**
     * Hiển thị icon khi mật khẩu khớp
     */
    private void showMatchIcon() {
        binding.confirmNewPassword.setCompoundDrawablesWithIntrinsicBounds(
                null, null, AppCompatResources.getDrawable(this, R.drawable.check), null);
    }

    /**
     * Hiển thị icon khi mật khẩu không khớp
     */
    private void showMismatchIcon() {
        binding.confirmNewPassword.setCompoundDrawablesWithIntrinsicBounds(
                null, null, AppCompatResources.getDrawable(this, R.drawable.cancel), null);
    }

    /**
     * Thiết lập sự kiện cho các nút
     */
    private void setupButtons() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.btnConfirm.setOnClickListener(v -> processPasswordChange());
    }

    /**
     * Xử lý yêu cầu đổi mật khẩu
     */
    private void processPasswordChange() {
        String oldPass = binding.oldPassword.getText().toString().trim();
        String newPass = binding.newPassword.getText().toString().trim();
        String confirmPass = binding.confirmNewPassword.getText().toString().trim();

        if (hasEmptyField(oldPass, newPass, confirmPass)) {
            showEmptyFieldError();
        } else {
            changePassword(oldPass, newPass, confirmPass);
        }
    }

    /**
     * Kiểm tra có trường nào bỏ trống không
     */
    private boolean hasEmptyField(String... fields) {
        for (String field : fields) {
            if (field.isEmpty()) return true;
        }
        return false;
    }

    /**
     * Hiển thị thông báo lỗi khi có trường bỏ trống
     */
    private void showEmptyFieldError() {
        Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
    }

    /**
     * Thực hiện đổi mật khẩu
     */
    private void changePassword(String oldPass, String newPass, String confirmPass) {
        if (!newPass.equals(confirmPass)) {
            showPasswordMismatchError();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updatePassword(newPass);
            } else {
                showAuthError(task.getException());
            }
        });
    }

    /**
     * Hiển thị lỗi khi mật khẩu xác nhận không khớp
     */
    private void showPasswordMismatchError() {
        Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
    }

    /**
     * Cập nhật mật khẩu mới lên Firebase
     */
    private void updatePassword(String newPass) {
        user.updatePassword(newPass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateFirestorePassword(newPass);
            } else {
                showUpdateError(task.getException());
            }
        });
    }

    /**
     * Hiển thị lỗi xác thực
     */
    private void showAuthError(Exception e) {
        Toast.makeText(this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
        Log.e("AuthError", "Lỗi xác thực", e);
    }

    /**
     * Cập nhật mật khẩu trên Firestore
     */
    private void updateFirestorePassword(String newPass) {
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .update("Password", newPass)
                .addOnSuccessListener(aVoid -> showSuccess())
                .addOnFailureListener(e -> showFirestoreError(e));
    }

    /**
     * Hiển thị lỗi cập nhật mật khẩu
     */
    private void showUpdateError(Exception e) {
        Toast.makeText(this, "Lỗi khi đổi mật khẩu", Toast.LENGTH_SHORT).show();
        Log.e("UpdateError", "Lỗi cập nhật", e);
    }

    /**
     * Hiển thị thông báo thành công
     */
    private void showSuccess() {
        Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
        redirectToMain();
    }

    /**
     * Hiển thị lỗi cập nhật Firestore
     */
    private void showFirestoreError(Exception e) {
        Toast.makeText(this, "Lỗi cập nhật dữ liệu", Toast.LENGTH_SHORT).show();
        Log.e("FirestoreError", "Lỗi Firestore", e);
    }

    /**
     * Chuyển về màn hình chính
     */
    private void redirectToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}