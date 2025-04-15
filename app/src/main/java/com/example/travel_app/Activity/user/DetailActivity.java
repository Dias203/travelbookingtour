package com.example.travel_app.Activity.user;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.Utils.NetworkConnectionMonitor;
import com.example.travel_app.databinding.ActivityDetailBinding;

public class DetailActivity extends BaseActivity {
    private static final String TAG = "DetailActivity";
    private ActivityDetailBinding binding;
    private ItemDomain tourItem;
    private NetworkConnectionMonitor networkMonitor;
    private boolean wasDisconnected = false; // Biến cờ để theo dõi trạng thái trước đó


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeViews();
        retrieveTourData();
        setupUI();
        setupNetworkMonitoring();
    }

    private void initializeViews() {
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void retrieveTourData() {
        tourItem = (ItemDomain) getIntent().getSerializableExtra("object");
        if (tourItem == null) {
            Log.e(TAG, "Không thể lấy dữ liệu tour từ intent");
            finish();
        }
    }

    private void setupUI() {
        displayTourDetails();
        loadTourImage();
        setupClickListeners();
        updateButtonState(isNetworkConnected()); // Cập nhật trạng thái button ban đầu
    }

    private void displayTourDetails() {
        binding.titleTxt.setText(tourItem.getTitle());
        binding.priceTxt.setText(formatPrice(tourItem.getPrice()));
        binding.bedTxt.setText(String.valueOf(tourItem.getBed()));
        binding.addressTxt.setText(tourItem.getAddress());
        binding.durationTxt.setText(tourItem.getDuration());
        binding.distanceTxt.setText(tourItem.getDistance());
        binding.descriptionTxt.setText(tourItem.getDescription());
        binding.ratingBar.setRating((float) tourItem.getScore());
    }

    private String formatPrice(double price) {
        return price + " VND";
    }

    private void loadTourImage() {
        String imageUrl = tourItem.getPic();
        Log.d(TAG, "Đang tải hình ảnh cho: " + tourItem.getTitle() + ", URL: " + imageUrl);

        Glide.with(this)
                .load(imageUrl)
                .into(binding.pic);
    }

    private void setupClickListeners() {
        binding.backBtn.setOnClickListener(view -> finish());
        binding.bookTour.setOnClickListener(view -> {
            if (isNetworkConnected()) {
                navigateToTicketActivity();
            } else {
                showToast("Không có kết nối Internet. Vui lòng kiểm tra lại!");
            }
        });
    }

    private void navigateToTicketActivity() {
        Intent intent = new Intent(DetailActivity.this, TicketActivity.class);
        intent.putExtra("object", tourItem);
        startActivity(intent);
    }

    /** Thiết lập theo dõi trạng thái mạng */
    private void setupNetworkMonitoring() {
        wasDisconnected = !isNetworkConnected(); // Khởi tạo trạng thái ban đầu

        networkMonitor = new NetworkConnectionMonitor(this, new NetworkConnectionMonitor.NetworkChangeListener() {
            @Override
            public void onNetworkConnected() {
                runOnUiThread(() -> {
                    updateButtonState(true);

                    if (wasDisconnected) { // Chỉ thông báo khi trước đó mất mạng
                        showToast("Kết nối mạng đã được khôi phục");
                    }

                    wasDisconnected = false; // Đặt lại cờ
                });
            }

            @Override
            public void onNetworkDisconnected() {
                runOnUiThread(() -> {
                    updateButtonState(false);
                    showToast("Mất kết nối Internet");
                    wasDisconnected = true;
                });
            }
        });
    }


    /** Cập nhật trạng thái button bookTour */
    private void updateButtonState(boolean isConnected) {
        binding.bookTour.setEnabled(isConnected);
        binding.bookTour.setAlpha(isConnected ? 1.0f : 0.5f);
    }

    /**
     * Kiểm tra kết nối mạng
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkMonitor != null) {
            networkMonitor.unregisterNetworkReceiver();
        }
    }
}