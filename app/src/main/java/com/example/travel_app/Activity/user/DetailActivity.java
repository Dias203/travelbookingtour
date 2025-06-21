package com.example.travel_app.Activity.user;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Adapter.PurchasedAdapter;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.R;
import com.example.travel_app.Utils.NetworkConnectionMonitor;
import com.example.travel_app.databinding.ActivityDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailActivity extends BaseActivity {
    private static final String TAG = "DetailActivity";
    private ActivityDetailBinding binding;
    private final ArrayList<ItemDomain> itemList = new ArrayList<>();
    private ItemDomain tourItem;
    private PurchasedAdapter adapter;
    private NetworkConnectionMonitor networkMonitor;
    private boolean wasDisconnected = false;
    private boolean isFavClicked = false;
    private String cartDocId = null; // Lưu ID của document trong Cart
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: 1231323");
        initializeViews();
        retrieveTourData();
        setupUI();
        setEnableAddCart();
        setupNetworkMonitoring();
        checkIfInCart(); // Kiểm tra xem item đã có trong giỏ hàng chưa
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
        updateButtonState(isNetworkConnected());
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

        binding.addShoppingCart.setOnClickListener(view -> {
            Log.i(TAG, "setupClickListeners: " + isFavClicked);
            if (!isFavClicked) {
                isFavClicked = true;
                binding.addShoppingCart.setImageResource(R.drawable.remove_shopping_cart);
                showToast("Thêm vào giỏ hàng thành công!");
                addToCart();
            } else {
                isFavClicked = false;
                binding.addShoppingCart.setImageResource(R.drawable.add_shopping_card);
                showToast("Đã xóa khỏi giỏ hàng!");
                removeFromCart();
            }
        });
    }

    private void setEnableAddCart() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            showToast("Bạn chưa đăng nhập tài khoản!");
            binding.addShoppingCart.setEnabled(false);
            binding.addShoppingCart.setAlpha(0.5f);
            return;
        }
        binding.addShoppingCart.setEnabled(true);
        binding.addShoppingCart.setAlpha(1.0f);
    }

    // Kiểm tra xem item đã có trong giỏ hàng chưa
    private void checkIfInCart() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Cart")
                    .whereEqualTo("userId", user.getUid())
                    .whereEqualTo("id", tourItem.getId())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            cartDocId = doc.getId();
                            handler.post(() -> {
                                isFavClicked = true;
                                binding.addShoppingCart.setImageResource(R.drawable.remove_shopping_cart);
                            });
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Lỗi kiểm tra giỏ hàng: " + e.getMessage()));
            executor.shutdown();
        });
    }

    private void addToCart() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                handler.post(() -> showToast("Vui lòng đăng nhập để thêm vào giỏ hàng"));
                return;
            }

            ItemDomain item = createItemDomain();
            item.setUserId(user.getUid());
            Log.d(TAG, "Item: " + new Gson().toJson(item));

            FirebaseFirestore.getInstance().collection("Cart")
                    .add(item)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            cartDocId = task.getResult().getId(); // Lưu ID của document vừa thêm
                            handler.post(() -> showToast("Dữ liệu đã được thêm vào Firestore"));
                        } else {
                            handler.post(() -> showToast("Thêm dữ liệu thất bại"));
                        }
                    })
                    .addOnFailureListener(e -> handler.post(() -> {
                        Log.e(TAG, "Lỗi: " + e.getMessage());
                        showToast("Lỗi: " + e.getMessage());
                    }));
            executor.shutdown();
        });
    }

    private void removeFromCart() {
        if (cartDocId == null) {
            showToast("Không tìm thấy mục trong giỏ hàng");
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Cart")
                    .document(cartDocId)
                    .delete()
                    .addOnSuccessListener(unused -> handler.post(() -> {
                        cartDocId = null; // Xóa ID sau khi xóa thành công
                        showToast("Đã xóa thành công khỏi giỏ hàng");
                    }))
                    .addOnFailureListener(e -> handler.post(() -> {
                        isFavClicked = true; // Khôi phục trạng thái nếu xóa thất bại
                        binding.addShoppingCart.setImageResource(R.drawable.remove_shopping_cart);
                        showToast("Lỗi khi xóa: " + e.getMessage());
                    }));
            executor.shutdown();
        });
    }

    private ItemDomain createItemDomain() {
        ItemDomain item = new ItemDomain();
        item.setTitle(tourItem.getTitle());
        item.setPrice(tourItem.getPrice());
        item.setBed(tourItem.getBed());
        item.setId(tourItem.getId());
        item.setAddress(tourItem.getAddress());
        item.setDuration(tourItem.getDuration());
        item.setDistance(tourItem.getDistance());
        item.setDescription(tourItem.getDescription());
        item.setScore(tourItem.getScore());
        item.setTimeTour(tourItem.getTimeTour());
        item.setDateTour(tourItem.getDateTour());
        item.setPic(tourItem.getPic());
        item.setTourGuideName(tourItem.getTourGuideName());
        item.setTourGuidePhone(tourItem.getTourGuidePhone());
        item.setTourGuidePic(tourItem.getTourGuidePic());
        return item;
    }

    private void navigateToTicketActivity() {
        Intent intent = new Intent(DetailActivity.this, TicketActivity.class);
        intent.putExtra("object", tourItem);
        startActivity(intent);
    }

    private void setupNetworkMonitoring() {
        wasDisconnected = !isNetworkConnected();

        networkMonitor = new NetworkConnectionMonitor(this, new NetworkConnectionMonitor.NetworkChangeListener() {
            @Override
            public void onNetworkConnected() {
                runOnUiThread(() -> {
                    updateButtonState(true);
                    if (wasDisconnected) {
                        showToast("Kết nối mạng đã được khôi phục");
                    }
                    wasDisconnected = false;
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

    private void updateButtonState(boolean isConnected) {
        binding.bookTour.setEnabled(isConnected);
        binding.bookTour.setAlpha(isConnected ? 1.0f : 0.5f);
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
    protected void onDestroy() {
        super.onDestroy();
        if (networkMonitor != null) {
            networkMonitor.unregisterNetworkReceiver();
        }
    }
}