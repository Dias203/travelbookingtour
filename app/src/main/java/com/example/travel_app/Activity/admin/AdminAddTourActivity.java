package com.example.travel_app.Activity.admin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AdminAddTourActivity extends BaseActivity {
    private static final String TAG = "AdminAddTourActivity";
    private static final int PICK_IMAGE = 1;
    private static final String DEFAULT_GUIDE_PIC = "https://firebasestorage.googleapis.com/v0/b/travel-app-51c86.appspot.com/o/profile.png?alt=media&token=33895bf9-4dfc-4866-99d6-fb24451939fa";
    private static final String PREF_NAME = "AdminAddTourPrefs";
    private static final String KEY_TITLE = "title";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_TIME = "time";
    private static final String KEY_DATE = "date";
    private static final String KEY_GUIDE_NAME = "guide_name";
    private static final String KEY_GUIDE_PHONE = "guide_phone";
    private static final String KEY_PRICE = "price";
    private static final String KEY_BED = "bed";
    private static final String KEY_SCORE = "score";
    private static final String KEY_CATEGORY_ID = "category_id";
    private static final String KEY_IMAGE_URI = "image_uri";
    private static final String KEY_RECOMMENDED = "recommended";
    private static final String KEY_POPULAR = "popular";

    private ActivityAdminAddTourBinding binding;
    private Uri imageUri;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddTourBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        restoreSavedData();
        setupEventListeners();
    }

    private void restoreSavedData() {
        // Khôi phục dữ liệu từ SharedPreferences
        if (prefs.contains(KEY_TITLE)) {
            binding.titleTour.setText(prefs.getString(KEY_TITLE, ""));
        }
        if (prefs.contains(KEY_ADDRESS)) {
            binding.addressTour.setText(prefs.getString(KEY_ADDRESS, ""));
        }
        if (prefs.contains(KEY_DESCRIPTION)) {
            binding.descriptionTour.setText(prefs.getString(KEY_DESCRIPTION, ""));
        }
        if (prefs.contains(KEY_DURATION)) {
            binding.durationTour.setText(prefs.getString(KEY_DURATION, ""));
        }
        if (prefs.contains(KEY_TIME)) {
            binding.timeTour.setText(prefs.getString(KEY_TIME, ""));
        }
        if (prefs.contains(KEY_DATE)) {
            binding.dateTour.setText(prefs.getString(KEY_DATE, ""));
        }
        if (prefs.contains(KEY_GUIDE_NAME)) {
            binding.tourGuideName.setText(prefs.getString(KEY_GUIDE_NAME, ""));
        }
        if (prefs.contains(KEY_GUIDE_PHONE)) {
            binding.tourGuidePhone.setText(prefs.getString(KEY_GUIDE_PHONE, ""));
        }
        if (prefs.contains(KEY_PRICE)) {
            binding.priceTour.setText(prefs.getString(KEY_PRICE, ""));
        }
        if (prefs.contains(KEY_BED)) {
            binding.bedNum.setText(prefs.getString(KEY_BED, ""));
        }
        if (prefs.contains(KEY_SCORE)) {
            binding.scoreTour.setText(prefs.getString(KEY_SCORE, ""));
        }
        if (prefs.contains(KEY_CATEGORY_ID)) {
            binding.CategoryId.setText(prefs.getString(KEY_CATEGORY_ID, ""));
        }
        if (prefs.contains(KEY_IMAGE_URI)) {
            imageUri = Uri.parse(prefs.getString(KEY_IMAGE_URI, ""));
            binding.picTour.setImageURI(imageUri);
            binding.hintSuggest.setVisibility(View.GONE);
        }
        if (prefs.contains(KEY_RECOMMENDED)) {
            binding.recommendCheckBox.setChecked(prefs.getBoolean(KEY_RECOMMENDED, false));
        }
        if (prefs.contains(KEY_POPULAR)) {
            binding.popularCheckBox.setChecked(prefs.getBoolean(KEY_POPULAR, false));
        }
    }

    private void saveInputData() {
        // Lưu dữ liệu vào SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TITLE, binding.titleTour.getText().toString().trim());
        editor.putString(KEY_ADDRESS, binding.addressTour.getText().toString().trim());
        editor.putString(KEY_DESCRIPTION, binding.descriptionTour.getText().toString().trim());
        editor.putString(KEY_DURATION, binding.durationTour.getText().toString().trim());
        editor.putString(KEY_TIME, binding.timeTour.getText().toString().trim());
        editor.putString(KEY_DATE, binding.dateTour.getText().toString().trim());
        editor.putString(KEY_GUIDE_NAME, binding.tourGuideName.getText().toString().trim());
        editor.putString(KEY_GUIDE_PHONE, binding.tourGuidePhone.getText().toString().trim());
        editor.putString(KEY_PRICE, binding.priceTour.getText().toString().trim());
        editor.putString(KEY_BED, binding.bedNum.getText().toString().trim());
        editor.putString(KEY_SCORE, binding.scoreTour.getText().toString().trim());
        editor.putString(KEY_CATEGORY_ID, binding.CategoryId.getText().toString().trim());
        if (imageUri != null) {
            editor.putString(KEY_IMAGE_URI, imageUri.toString());
        }
        editor.putBoolean(KEY_RECOMMENDED, binding.recommendCheckBox.isChecked());
        editor.putBoolean(KEY_POPULAR, binding.popularCheckBox.isChecked());
        editor.apply();
    }

    private void clearSavedData() {
        // Xóa dữ liệu tạm thời trong SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    private void setupEventListeners() {
        binding.backBtn.setOnClickListener(v -> {
            saveInputData(); // Lưu dữ liệu trước khi thoát
            finish();
        });
        binding.cardView.setOnClickListener(v -> openGallery());
        binding.addTourButton.setOnClickListener(v -> uploadImageToFirebase(imageUri));
    }

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
            binding.hintSuggest.setVisibility(View.GONE);
            saveInputData(); // Lưu URI của ảnh vào SharedPreferences
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (!isNetworkConnected()) {
            showToast("Không có kết nối Internet. Vui lòng kiểm tra lại!");
            return;
        }

        if (imageUri == null) {
            showToast("Vui lòng chọn ảnh cho tour!");
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child(System.currentTimeMillis() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> addTourToFirebase(uri.toString())))
                .addOnFailureListener(e -> {
                    showToast("Tải ảnh thất bại: " + e.getMessage());
                    Log.e(TAG, "Error: " + e.getMessage());
                });
    }

    private void addTourToFirebase(String imageUrl) {
        if (!isTourInputValid()) return;

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Item");
        String inputTitle = binding.titleTour.getText().toString().trim();

        checkTourTitleExists(databaseRef, inputTitle, () -> addNewTour(databaseRef, imageUrl));
    }

    private void checkTourTitleExists(DatabaseReference databaseRef, String title, Runnable onSuccess) {
        databaseRef.orderByChild("title").equalTo(title)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            showToast("Tour đã tồn tại!");
                        } else {
                            onSuccess.run();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showToast("Lỗi kiểm tra trùng tiêu đề: " + error.getMessage());
                    }
                });
    }

    private void addNewTour(DatabaseReference databaseRef, String imageUrl) {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                String newId = generateTourId(count);
                ItemDomain tour = createTourItem(imageUrl, newId);

                databaseRef.child(newId).setValue(tour)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                clearSavedData(); // Xóa dữ liệu tạm thời sau khi thêm thành công
                                showToast("Thêm tour thành công!");
                                finish();
                            } else {
                                showToast("Thêm tour thất bại!");
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Lỗi đọc dữ liệu: " + error.getMessage());
            }
        });
    }

    private String generateTourId(long currentCount) {
        Random random = new Random();
        int randomNum = Math.abs(random.nextInt()) + 1;
        return String.valueOf(currentCount + randomNum);
    }

    private ItemDomain createTourItem(String imageUrl, String id) {
        ItemDomain tour = new ItemDomain();
        tour.setId(id);
        tour.setTitle(binding.titleTour.getText().toString().trim());
        tour.setAddress(binding.addressTour.getText().toString().trim());
        tour.setDescription(binding.descriptionTour.getText().toString().trim());
        tour.setDistance(binding.distanceTour.getText().toString().trim());
        tour.setPic(imageUrl);
        tour.setDuration(binding.durationTour.getText().toString().trim());
        tour.setTimeTour(binding.timeTour.getText().toString().trim());
        tour.setDateTour(binding.dateTour.getText().toString().trim());
        tour.setTourGuideName(binding.tourGuideName.getText().toString().trim());
        tour.setTourGuidePhone(binding.tourGuidePhone.getText().toString().trim());
        tour.setPrice(Integer.parseInt(binding.priceTour.getText().toString().trim()));
        tour.setBed(Integer.parseInt(binding.bedNum.getText().toString().trim()));
        tour.setScore(Double.parseDouble(binding.scoreTour.getText().toString().trim()));
        tour.setCategoryId(Integer.parseInt(binding.CategoryId.getText().toString().trim()));
        tour.setRecommended(binding.recommendCheckBox.isChecked());
        tour.setPopular(binding.popularCheckBox.isChecked());
        tour.setTourGuidePic(DEFAULT_GUIDE_PIC);
        return tour;
    }

    private boolean isTourInputValid() {
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

        boolean isValid = true;

        if (title.isEmpty() || !title.matches("[a-zA-ZÀ-ỹ\\s]+")) {
            binding.titleTour.setError("Vui lòng nhập tiêu đề hợp lệ");
            isValid = false;
        }
        if (address.isEmpty() || !address.matches("[a-zA-ZÀ-ỹ0-9\\s,.]+")) {
            binding.addressTour.setError("Vui lòng nhập địa chỉ hợp lệ");
            isValid = false;
        }
        if (description.isEmpty()) {
            binding.descriptionTour.setError("Vui lòng nhập mô tả");
            isValid = false;
        }
        if (duration.isEmpty()) {
            binding.durationTour.setError("Vui lòng nhập thời lượng tour");
            isValid = false;
        }
        if (timeTour.isEmpty()) {
            binding.timeTour.setError("Vui lòng nhập thời gian khởi hành");
            isValid = false;
        }
        if (dateTour.isEmpty()) {
            binding.dateTour.setError("Vui lòng nhập ngày khởi hành");
            isValid = false;
        } else if (!dateTour.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
            binding.dateTour.setError("Ngày không hợp lệ (định dạng dd/MM/yyyy)");
            isValid = false;
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                sdf.setLenient(false);
                Date tourDate = sdf.parse(dateTour);
                Date currentDate = new Date();

                Calendar tourCal = Calendar.getInstance();
                tourCal.setTime(tourDate);
                Calendar currentCal = Calendar.getInstance();
                currentCal.setTime(currentDate);

                tourCal.set(Calendar.HOUR_OF_DAY, 0);
                tourCal.set(Calendar.MINUTE, 0);
                tourCal.set(Calendar.SECOND, 0);
                tourCal.set(Calendar.MILLISECOND, 0);
                currentCal.set(Calendar.HOUR_OF_DAY, 0);
                currentCal.set(Calendar.MINUTE, 0);
                currentCal.set(Calendar.SECOND, 0);
                currentCal.set(Calendar.MILLISECOND, 0);

                if (tourCal.getTime().compareTo(currentCal.getTime()) <= 0) {
                    binding.dateTour.setError("Ngày khởi hành phải sau ngày hiện tại (" + sdf.format(currentDate) + ")");
                    isValid = false;
                }
            } catch (ParseException e) {
                binding.dateTour.setError("Ngày không hợp lệ (định dạng dd/MM/yyyy)");
                isValid = false;
            }
        }
        if (tourGuideName.isEmpty()) {
            binding.tourGuideName.setError("Vui lòng nhập tên hướng dẫn viên");
            isValid = false;
        }
        if (tourGuidePhone.isEmpty() || !tourGuidePhone.matches("^0\\d{9}$")) {
            binding.tourGuidePhone.setError("Số điện thoại không hợp lệ (bắt đầu bằng 0, đủ 10 số)");
            isValid = false;
        }
        isValid &= validateNumericField(binding.priceTour, priceStr, "Giá tour", 0);
        isValid &= validateNumericField(binding.bedNum, bedNumStr, "Số giường", 0);
        isValid &= validateScoreField(binding.scoreTour, scoreStr);
        isValid &= validateNumericField(binding.CategoryId, categoryIdStr, "Category ID", Integer.MIN_VALUE);

        if (!isValid) showToast("Vui lòng kiểm tra lại thông tin!");
        return isValid;
    }

    private boolean validateNumericField(android.widget.EditText field, String value, String fieldName, int minValue) {
        try {
            int number = Integer.parseInt(value);
            if (number < minValue) {
                field.setError(fieldName + " phải lớn hơn hoặc bằng " + minValue);
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            field.setError(fieldName + " phải là số");
            return false;
        }
    }

    private boolean validateScoreField(android.widget.EditText field, String value) {
        try {
            double score = Double.parseDouble(value);
            if (score < 0 || score > 5) {
                field.setError("Điểm đánh giá phải từ 0 đến 5");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            field.setError("Điểm đánh giá phải là số");
            return false;
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveInputData(); // Lưu dữ liệu khi activity tạm dừng
    }
}