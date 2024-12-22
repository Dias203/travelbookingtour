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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePasswordActivity extends BaseActivity {

    ActivityChangePasswordAcitivityBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordAcitivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Không thể xác thực người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class)); // Chuyển đến màn hình đăng nhập
            finish();
            return;
        }

        // Ban đầu ẩn icon
        binding.confirmNewPassword.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        // Lắng nghe sự kiện nhập liệu trên Confirm Password
        binding.confirmNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String newPassword = binding.newPassword.getText().toString().trim();
                String confirmPassword = s.toString().trim();

                if (newPassword.equals(confirmPassword) && !confirmPassword.isEmpty()) {
                    // Hiện icon khi mật khẩu khớp
                    binding.confirmNewPassword.setCompoundDrawablesWithIntrinsicBounds(
                            null, null, AppCompatResources.getDrawable(ChangePasswordActivity.this, R.drawable.check), null);
                } else {
                    // Ẩn icon nếu không khớp
                    binding.confirmNewPassword.setCompoundDrawablesWithIntrinsicBounds(
                            null, null, AppCompatResources.getDrawable(ChangePasswordActivity.this, R.drawable.cancel), null);
                }
            }
        });

        // Xử lý nút quay lại
        binding.backBtn.setOnClickListener(v -> finish());

        // Xử lý khi người dùng xác nhận đổi mật khẩu
        binding.btnConfirm.setOnClickListener(v -> {
            String oldPassword = binding.oldPassword.getText().toString().trim();
            String newPassword = binding.newPassword.getText().toString().trim();
            String confirmNewPassword = binding.confirmNewPassword.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else {
                changePassword(oldPassword, newPassword, confirmNewPassword);
            }
        });
    }

    private void changePassword(String oldPassword, String newPassword, String confirmNewPassword) {
        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        } else {
            binding.confirmNewPassword.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, AppCompatResources.getDrawable(this, R.drawable.check), null); // Hiện icon
            //Firebase sử dụng AuthCredential để thực hiện các thao tác xác thực,
            // chẳng hạn như đăng nhập, liên kết tài khoản, hoặc xác thực lại người dùng.
            // Tạo credential từ email và mật khẩu cũ
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

            // Xác thực lại người dùng
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Cập nhật mật khẩu mới
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            // Cập nhật mật khẩu trong Firestore
                            FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                            fStore.collection("Users").document(user.getUid())
                                    .update("Password", newPassword)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Mật khẩu đã được cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                        // Quay lại màn hình chính sau khi cập nhật
                                        startActivity(new Intent(this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Cập nhật Firestore thất bại", Toast.LENGTH_SHORT).show();
                                        Log.e("FirestoreUpdate", "Error updating Firestore", e);
                                    });
                        } else {
                            Toast.makeText(this, "Có lỗi xảy ra khi cập nhật mật khẩu", Toast.LENGTH_SHORT).show();
                            Log.e("UpdatePassword", "Error updating password", updateTask.getException());
                        }
                    });
                } else {
                    Toast.makeText(this, "Mật khẩu cũ không đúng hoặc không thể xác thực", Toast.LENGTH_SHORT).show();
                    Log.e("Reauthenticate", "Error in reauthentication", task.getException());
                }
            });

        }

    }

}
