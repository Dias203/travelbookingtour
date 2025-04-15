package com.example.travel_app.Activity.admin;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.databinding.ActivityAdminEditBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminEditActivity extends BaseActivity {
    private static final String TAG = "AdminEditActivity";

    private ActivityAdminEditBinding binding;
    private String productId;
    private ItemDomain item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeItem();
        displayItemData();
        setupEventListeners();
    }

    /**
     * Khởi tạo đối tượng ItemDomain từ dữ liệu Intent
     */
    private void initializeItem() {
        Intent intent = getIntent();
        item = new ItemDomain();
        item.setTitle(intent.getStringExtra("title"));
        item.setPrice(intent.getDoubleExtra("price", 0.0));
        item.setScore(intent.getDoubleExtra("score", 0.0));
        item.setAddress(intent.getStringExtra("address"));
        productId = intent.getStringExtra("id");
    }

    /**
     * Hiển thị dữ liệu của item lên giao diện
     */
    private void displayItemData() {
        if (item != null) {
            binding.editTitleTour.setText(item.getTitle());
            binding.editPriceTour.setText(String.valueOf(item.getPrice()));
            binding.editScoreTour.setText(String.valueOf(item.getScore()));
            binding.editAddressTour.setText(item.getAddress());
        }
    }

    /**
     * Thiết lập các sự kiện click cho các nút
     */
    private void setupEventListeners() {
        binding.saveButton.setOnClickListener(v -> saveProductData());
    }

    /**
     * Lưu dữ liệu sản phẩm đã chỉnh sửa
     */
    private void saveProductData() {
        if (!isNetworkConnected()) {
            showToast("Không có kết nối Internet. Vui lòng kiểm tra lại!");
            return;
        }

        String newTitle = binding.editTitleTour.getText().toString().trim();
        String address = binding.editAddressTour.getText().toString().trim();
        String priceStr = binding.editPriceTour.getText().toString().trim();
        String scoreStr = binding.editScoreTour.getText().toString().trim();

        if (!isInputValid(newTitle, address, priceStr, scoreStr)) return;

        double price = Double.parseDouble(priceStr);
        double score = Double.parseDouble(scoreStr);

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Item");

        if (newTitle.equals(item.getTitle())) {
            updateProduct(productId, newTitle, price, score, address);
        } else {
            checkTitleExists(databaseRef, newTitle, () ->
                    updateProduct(productId, newTitle, price, score, address));
        }
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu đầu vào
     */
    private boolean isInputValid(String title, String address, String priceStr, String scoreStr) {
        if (!title.matches("[a-zA-ZÀ-ỹ\\s]+")) {
            showToast("Tiêu đề chỉ được chứa chữ cái và khoảng trắng");
            return false;
        }

        if (!address.matches("[a-zA-ZÀ-ỹ0-9\\s,.]+")) {
            showToast("Địa chỉ chỉ được chứa chữ cái, số và khoảng trắng");
            return false;
        }

        try {
            Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            showToast("Sai định dạng giá. Vui lòng nhập số!");
            return false;
        }

        try {
            Double.parseDouble(scoreStr);
        } catch (NumberFormatException e) {
            showToast("Sai định dạng đánh giá. Vui lòng nhập số!");
            return false;
        }

        return true;
    }

    /**
     * Kiểm tra xem tiêu đề mới đã tồn tại chưa
     */
    private void checkTitleExists(DatabaseReference databaseRef, String newTitle, Runnable onSuccess) {
        databaseRef.orderByChild("title").equalTo(newTitle)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            showToast("Tên tour đã tồn tại!");
                        } else {
                            onSuccess.run();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                        showToast("Lỗi kiểm tra trùng tên: " + error.getMessage());
                    }
                });
    }

    /**
     * Cập nhật thông tin sản phẩm lên Firebase
     */
    private void updateProduct(String id, String title, double price, double score, String address) {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Item").child(id);

        productRef.child("title").setValue(title);
        productRef.child("price").setValue(price);
        productRef.child("score").setValue(score);
        productRef.child("address").setValue(address)
                .addOnSuccessListener(aVoid -> {
                    showToast("Cập nhật thành công");
                    finish();
                })
                .addOnFailureListener(e -> showToast("Lỗi: " + e.getMessage()));
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
     * Hiển thị thông báo Toast
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}