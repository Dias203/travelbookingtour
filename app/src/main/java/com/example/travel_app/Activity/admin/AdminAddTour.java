package com.example.travel_app.Activity.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.databinding.ActivityAdminAddTourBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Random;

public class AdminAddTour extends BaseActivity {
    ActivityAdminAddTourBinding binding;
    private static final int PICK_IMAGE = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddTourBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Quay lại màn hình trước
        binding.backBtn.setOnClickListener(v -> finish());

        // Mở gallery để chọn ảnh
        binding.cardView.setOnClickListener(v -> openGallery());

        // Xử lý thêm tour
        binding.addTourButton.setOnClickListener(v -> uploadImageToFirebase(imageUri));
    }

    // Lấy ảnh từ thiết bị
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            binding.picTour.setImageURI(imageUri);
        }
    }

    // Tải ảnh lên Firebase
    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(System.currentTimeMillis() + ".jpg");

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        addTourToFirebase(imageUrl);
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(AdminAddTour.this, "Tải ảnh thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("FirebaseStorage", "Error: " + e.getMessage());
                    });
        }
    }


    private void addTourToFirebase(String imageUrl) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Item");

        // Lấy số lượng phần tử hiện tại
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                long nextIndex = count; // Tính chỉ số tiếp theo

                // Tạo số ngẫu nhiên đảm bảo nó luôn dương và lớn hơn nextIndex
                Random random = new Random();
                int randomNum = Math.abs(random.nextInt()) + 1;  // Đảm bảo số ngẫu nhiên luôn lớn hơn 0

                // Đảm bảo nextIndex + randomNum > nextIndex
                long newId = nextIndex + randomNum;  // Tổng hợp nextIndex và randomNum để tạo ID mới

                // Tạo đối tượng ItemDomain và thêm vào Firebase Realtime Database với chỉ số tiếp theo
                ItemDomain tour = new ItemDomain();
                tour.setId(String.valueOf(newId)); // Sử dụng chỉ số tiếp theo làm ID

                // Kiểm tra các trường dữ liệu trước khi thêm vào database
                String title = binding.titleTour.getText().toString().trim();
                String address = binding.addressTour.getText().toString().trim();
                String description = binding.descriptionTour.getText().toString().trim();
                String duration = binding.durationTour.getText().toString().trim();
                String timeTour = binding.timeTour.getText().toString().trim();
                String dateTour = binding.dateTour.getText().toString().trim();
                String tourGuideName = binding.tourGuideName.getText().toString().trim();
                String tourGuidePhone = binding.tourGuidePhone.getText().toString().trim();
                String priceStr = binding.priceTour.getText().toString().trim();
                String bedNumStr = binding.bedNum.getText().toString().trim();
                String scoreStr = binding.scoreTour.getText().toString().trim();
                String categoryIdStr = binding.CategoryId.getText().toString().trim();
                String distance = binding.distanceTour.getText().toString().trim();

                // Kiểm tra các trường không được null hoặc rỗng
                if (title.isEmpty() || address.isEmpty() || description.isEmpty() || duration.isEmpty() || timeTour.isEmpty() ||
                        dateTour.isEmpty() || tourGuideName.isEmpty() || tourGuidePhone.isEmpty() || priceStr.isEmpty() ||
                        bedNumStr.isEmpty() || scoreStr.isEmpty() || categoryIdStr.isEmpty()) {
                    Toast.makeText(AdminAddTour.this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return; // Dừng quá trình nếu có trường nào bị bỏ trống
                }

                // Nếu tất cả các trường hợp đều hợp lệ, thực hiện gán dữ liệu vào đối tượng tour
                tour.setTitle(title);
                tour.setAddress(address);
                tour.setDescription(description);
                tour.setDistance(distance);
                tour.setPic(imageUrl);
                tour.setDuration(duration);
                tour.setTimeTour(timeTour);
                tour.setDateTour(dateTour);
                tour.setTourGuideName(tourGuideName);
                tour.setTourGuidePhone(tourGuidePhone);
                tour.setPrice(Integer.parseInt(priceStr));
                tour.setBed(Integer.parseInt(bedNumStr));
                tour.setScore(Double.parseDouble(scoreStr));
                tour.setCategoryId(Integer.parseInt(categoryIdStr));
                tour.setRecommended(binding.recommendCheckBox.isChecked());
                tour.setPopular(binding.popularCheckBox.isChecked());
                tour.setTourGuidePic("https://firebasestorage.googleapis.com/v0/b/travel-app-51c86.appspot.com/o/profile.png?alt=media&token=33895bf9-4dfc-4866-99d6-fb24451939fa");

                // Lưu dữ liệu vào Firebase Realtime Database
                databaseRef.child(tour.getId()).setValue(tour)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(AdminAddTour.this, "Thêm tour thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(AdminAddTour.this, "Thêm tour thất bại!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminAddTour.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}