package com.example.travel_app.Activity.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.databinding.ActivityAdminEditBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminEditActivity extends AppCompatActivity {
    ActivityAdminEditBinding binding;
    private String productId;
    ItemDomain item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo item với giá trị từ Intent
        initializeItem();

        getData();
        setVariable();
    }

    private void initializeItem() {
        Intent intent = getIntent();

        // Tạo mới item với dữ liệu từ Intent
        item = new ItemDomain();
        item.setTitle(intent.getStringExtra("title"));
        item.setPrice(intent.getDoubleExtra("price", item.getPrice())); // Giá trị mặc định là 0.0 nếu không có trong Intent
        item.setScore(intent.getDoubleExtra("score", item.getScore())); // Giá trị mặc định là 0.0 nếu không có trong Intent
        item.setAddress(intent.getStringExtra("address"));
    }

    private void getData() {
        // Lấy dữ liệu từ item và gán vào các trường EditText
        if (item != null) {
            binding.editTitleTour.setText(item.getTitle());
            binding.editPriceTour.setText(String.valueOf(item.getPrice()));
            binding.editScoreTour.setText(String.valueOf(item.getScore()));
            binding.editAddressTour.setText(item.getAddress());
        }
    }

    private void setVariable() {
        Intent intent = getIntent();
        productId = intent.getStringExtra("id");

        // Sự kiện lưu dữ liệu khi nhấn nút Save
        binding.saveButton.setOnClickListener(v -> saveProductData());
    }

    private void saveProductData() {
        // Lưu dữ liệu cập nhật vào Firebase
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Item").child(productId);

        try {
            String title = binding.editTitleTour.getText().toString();
            double price = Double.parseDouble(binding.editPriceTour.getText().toString());
            double score = Double.parseDouble(binding.editScoreTour.getText().toString());
            String address = binding.editAddressTour.getText().toString();

            // Lưu vào Firebase
            productRef.child("title").setValue(title);
            productRef.child("price").setValue(price);
            productRef.child("score").setValue(score);
            productRef.child("address").setValue(address)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getApplicationContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(getApplicationContext(), "Vui lòng nhập giá trị hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }
}
