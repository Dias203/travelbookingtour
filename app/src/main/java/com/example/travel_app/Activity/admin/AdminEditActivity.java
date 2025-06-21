package com.example.travel_app.Activity.admin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AdminEditActivity extends BaseActivity {
    private static final String TAG = "AdminEditActivity";
    private static final String PREF_NAME = "AdminEditPrefs";
    private static final String KEY_TITLE = "title";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_DATE = "date";
    private static final String KEY_TIME = "time";
    private static final String KEY_PRICE = "price";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_BED = "bed";
    private static final String KEY_SCORE = "score";

    private ActivityAdminEditBinding binding;
    private String productId;
    private ItemDomain item;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        initializeItem();
        displayItemData();
        restoreSavedData();
        setupEventListeners();
    }

    private void initializeItem() {
        Intent intent = getIntent();
        item = new ItemDomain();
        item.setTitle(intent.getStringExtra("title") != null ? intent.getStringExtra("title") : "");
        item.setPrice(intent.getDoubleExtra("price", 0.0));
        item.setScore(intent.getDoubleExtra("score", 0.0));
        item.setTimeTour(intent.getStringExtra("time") != null ? intent.getStringExtra("time") : "");
        item.setBed(intent.getIntExtra("bed", 0));
        item.setAddress(intent.getStringExtra("address") != null ? intent.getStringExtra("address") : "");
        item.setDuration(intent.getStringExtra("duration") != null ? intent.getStringExtra("duration") : "");
        item.setDateTour(intent.getStringExtra("date") != null ? intent.getStringExtra("date") : "");
        productId = intent.getStringExtra("id") != null ? intent.getStringExtra("id") : "";
    }

    private void displayItemData() {
        if (item != null) {
            binding.editTitleTour.setText(item.getTitle());
            binding.editPriceTour.setText(String.valueOf(item.getPrice()));
            binding.editScoreTour.setText(String.valueOf(item.getScore()));
            binding.editTimeTour.setText(item.getTimeTour());
            binding.editDateTour.setText(item.getDateTour());
            binding.editDurationTour.setText(item.getDuration());
            binding.editBedNum.setText(String.valueOf(item.getBed()));
            binding.editAddressTour.setText(item.getAddress());
        }
    }

    private void restoreSavedData() {
        // Khôi phục dữ liệu từ SharedPreferences nếu có
        if (prefs.contains(KEY_TITLE)) {
            binding.editTitleTour.setText(prefs.getString(KEY_TITLE, ""));
        }
        if (prefs.contains(KEY_ADDRESS)) {
            binding.editAddressTour.setText(prefs.getString(KEY_ADDRESS, ""));
        }
        if (prefs.contains(KEY_DATE)) {
            binding.editDateTour.setText(prefs.getString(KEY_DATE, ""));
        }
        if (prefs.contains(KEY_TIME)) {
            binding.editTimeTour.setText(prefs.getString(KEY_TIME, ""));
        }
        if (prefs.contains(KEY_PRICE)) {
            binding.editPriceTour.setText(prefs.getString(KEY_PRICE, ""));
        }
        if (prefs.contains(KEY_DURATION)) {
            binding.editDurationTour.setText(prefs.getString(KEY_DURATION, ""));
        }
        if (prefs.contains(KEY_BED)) {
            binding.editBedNum.setText(prefs.getString(KEY_BED, ""));
        }
        if (prefs.contains(KEY_SCORE)) {
            binding.editScoreTour.setText(prefs.getString(KEY_SCORE, ""));
        }
    }

    private void saveInputData() {
        // Lưu dữ liệu vào SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TITLE, binding.editTitleTour.getText().toString().trim());
        editor.putString(KEY_ADDRESS, binding.editAddressTour.getText().toString().trim());
        editor.putString(KEY_DATE, binding.editDateTour.getText().toString().trim());
        editor.putString(KEY_TIME, binding.editTimeTour.getText().toString().trim());
        editor.putString(KEY_PRICE, binding.editPriceTour.getText().toString().trim());
        editor.putString(KEY_DURATION, binding.editDurationTour.getText().toString().trim());
        editor.putString(KEY_BED, binding.editBedNum.getText().toString().trim());
        editor.putString(KEY_SCORE, binding.editScoreTour.getText().toString().trim());
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
        binding.saveButton.setOnClickListener(v -> saveProductData());
    }

    private void saveProductData() {
        if (!isNetworkConnected()) {
            showToast("Không có kết nối Internet. Vui lòng kiểm tra lại!");
            return;
        }

        String newTitle = binding.editTitleTour.getText().toString().trim();
        String address = binding.editAddressTour.getText().toString().trim();
        String dateTour = binding.editDateTour.getText().toString().trim();
        String timeTour = binding.editTimeTour.getText().toString().trim();
        String priceStr = binding.editPriceTour.getText().toString().trim();
        String durationStr = binding.editDurationTour.getText().toString().trim();
        String bedNumStr = binding.editBedNum.getText().toString().trim();
        String scoreStr = binding.editScoreTour.getText().toString().trim();

        if (!isInputValid(newTitle, address, priceStr, scoreStr, timeTour, dateTour, durationStr, bedNumStr)) return;

        double price = Double.parseDouble(priceStr);
        double score = Double.parseDouble(scoreStr);
        int bedNum = Integer.parseInt(bedNumStr);

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Item");

        if (newTitle.equals(item.getTitle())) {
            updateProduct(productId, newTitle, price, score, address, timeTour, dateTour, durationStr, bedNum);
        } else {
            checkTitleExists(databaseRef, newTitle, () ->
                    updateProduct(productId, newTitle, price, score, address, timeTour, dateTour, durationStr, bedNum));
        }
    }

    private boolean isInputValid(String title, String address, String priceStr, String scoreStr, String timeStr, String dateStr, String durationStr, String bedNumStr) {
        boolean isValid = true;

        if (title.isEmpty() || !title.matches("[a-zA-ZÀ-ỹ\\s]+")) {
            showToast("Tiêu đề không chứa ký tự đặc biệt");
            return false;
        }

        if (address.isEmpty() || !address.matches("[a-zA-ZÀ-ỹ0-9\\s,.]+")) {
            showToast("Địa chỉ không chứa ký tự đặc biệt");
            return false;
        }

        if (priceStr.isEmpty()) {
            showToast("Vui lòng nhập giá tour");
            return false;
        }
        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                showToast("Giá tour phải lớn hơn 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showToast("Sai định dạng giá. Vui lòng nhập số!");
            return false;
        }

        if (scoreStr.isEmpty()) {
            showToast("Vui lòng nhập điểm đánh giá");
            return false;
        }
        try {
            double score = Double.parseDouble(scoreStr);
            if (score < 0 || score > 5) {
                showToast("Điểm đánh giá phải từ 0 đến 5");
                return false;
            }
        } catch (NumberFormatException e) {
            showToast("Sai định dạng đánh giá. Vui lòng nhập số!");
            return false;
        }

        if (timeStr.isEmpty()) {
            showToast("Vui lòng nhập thời gian khởi hành");
            return false;
        } else if (!timeStr.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            showToast("Thời gian không hợp lệ (định dạng HH:mm)");
            return false;
        }

        if (dateStr.isEmpty()) {
            showToast("Vui lòng nhập ngày khởi hành");
            return false;
        } else if (!dateStr.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
            showToast("Ngày không hợp lệ (định dạng dd/MM/yyyy)");
            return false;
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                sdf.setLenient(false);
                Date tourDate = sdf.parse(dateStr);
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

                if (tourCal.getTime().compareTo(currentCal.getTime()) < 0) {
                    showToast("Ngày khởi hành phải sau ngày hiện tại (" + sdf.format(currentDate) + ")");
                    return false;
                } else if (tourCal.getTime().compareTo(currentCal.getTime()) == 0) {
                    SimpleDateFormat stf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    stf.setLenient(false);
                    Date tourTime = stf.parse(timeStr);
                    Calendar tourTimeCal = Calendar.getInstance();
                    tourTimeCal.setTime(tourTime);

                    Calendar currentTimeCal = Calendar.getInstance();
                    currentTimeCal.setTime(currentDate);

                    int tourHour = tourTimeCal.get(Calendar.HOUR_OF_DAY);
                    int tourMinute = tourTimeCal.get(Calendar.MINUTE);
                    int currentHour = currentTimeCal.get(Calendar.HOUR_OF_DAY);
                    int currentMinute = currentTimeCal.get(Calendar.MINUTE);

                    if (tourHour < currentHour || (tourHour == currentHour && tourMinute <= currentMinute)) {
                        showToast("Thời gian khởi hành phải sau thời gian hiện tại (" + stf.format(currentDate) + ")");
                        return false;
                    }
                }
            } catch (ParseException e) {
                showToast("Ngày hoặc giờ không hợp lệ");
                return false;
            }
        }

        if (durationStr.isEmpty()) {
            showToast("Vui lòng nhập thời lượng tour");
            return false;
        } else if (!durationStr.matches("^\\d+N/\\d+Đ$")) {
            showToast("Thời lượng tour không hợp lệ (định dạng numN/numĐ, ví dụ: 4N/3Đ)");
            return false;
        } else {
            try {
                String[] parts = durationStr.split("/");
                int days = Integer.parseInt(parts[0].replace("N", ""));
                int nights = Integer.parseInt(parts[1].replace("Đ", ""));
                if (days < nights) {
                    showToast("Số ngày phải lớn hơn hoặc bằng số đêm");
                    return false;
                }
                if (days <= 0 || nights < 0) {
                    showToast("Số ngày phải lớn hơn 0 và số đêm phải >= 0");
                    return false;
                }
            } catch (NumberFormatException e) {
                showToast("Sai định dạng thời lượng tour");
                return false;
            }
        }

        if (bedNumStr.isEmpty()) {
            showToast("Vui lòng nhập chất lượng khách sạn");
            return false;
        }
        try {
            int bedNum = Integer.parseInt(bedNumStr);
            if (bedNum < 1 || bedNum > 5) {
                showToast("Chất lượng khách sạn phải từ 1 đến 10");
                return false;
            }
        } catch (NumberFormatException e) {
            showToast("Sai định dạng chất lượng khách sạn. Vui lòng nhập số nguyên!");
            return false;
        }

        return true;
    }

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

    private void updateProduct(String id, String title, double price, double score, String address, String timeTour, String dateTour, String duration, int bedNum) {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Item").child(id);

        productRef.child("title").setValue(title);
        productRef.child("price").setValue(price);
        productRef.child("date").setValue(dateTour);
        productRef.child("time").setValue(timeTour);
        productRef.child("score").setValue(score);
        productRef.child("duration").setValue(duration);
        productRef.child("bed").setValue(bedNum);
        productRef.child("address").setValue(address)
                .addOnSuccessListener(aVoid -> {
                    clearSavedData(); // Xóa dữ liệu tạm thời sau khi lưu thành công
                    showToast("Cập nhật thành công");
                    finish();
                })
                .addOnFailureListener(e -> showToast("Lỗi: " + e.getMessage()));
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