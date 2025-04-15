package com.example.travel_app.Activity.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Adapter.PurchasedAdapter;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.R;
import com.example.travel_app.databinding.ActivityPurchasedBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PurchasedActivity extends BaseActivity {
    private ActivityPurchasedBinding binding;
    private final ArrayList<ItemDomain> itemList = new ArrayList<>();
    private PurchasedAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPurchasedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation();
        loadPurchasedData();
    }

    /**
     * Thiết lập bottom navigation và xử lý sự kiện chuyển trang
     */
    private void setupBottomNavigation() {
        binding.bottomnav.setOnItemSelectedListener(i -> {
            if (i == R.id.home) {
                navigateTo(MainActivity.class);
            } else if (i == R.id.profile) {
                navigateTo(ProfileActivity.class);
            } else if (i == R.id.explorer) {
                openGoogleInChrome();
            }
        });
    }

    /**
     * Chuyển đến Activity được chỉ định
     */
    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    /**
     * Mở trang Google trong Chrome, fallback nếu không có Chrome
     */
    private void openGoogleInChrome() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
        intent.setPackage("com.android.chrome");

        if (intent.resolveActivity(getPackageManager()) == null) {
            intent.setPackage(null);
        }

        startActivity(intent);
    }

    /**
     * Tải dữ liệu tour đã mua từ Firestore
     */
    private void loadPurchasedData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            redirectToLogin();
            return;
        }

        showLoading(true);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Purchased")
                    .whereEqualTo("userId", user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            handleDataLoadSuccess(task.getResult().getDocuments());
                        } else {
                            showError("Không thể tải dữ liệu từ Firestore");
                        }
                        showLoading(false);
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        showError("Lỗi: " + e.getMessage());
                    });

            executor.shutdown();
        });
    }

    /**
     * Xử lý khi tải dữ liệu thành công
     */
    private void handleDataLoadSuccess(List<DocumentSnapshot> documents) {
        itemList.clear();
        for (DocumentSnapshot doc : documents) {
            ItemDomain item = doc.toObject(ItemDomain.class);
            if (item != null) itemList.add(item);
        }

        handler.post(() -> {
            if (!itemList.isEmpty()) {
                setupRecyclerView();
            }
        });
    }

    /**
     * Thiết lập RecyclerView và adapter
     */
    private void setupRecyclerView() {
        adapter = new PurchasedAdapter(itemList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        setupSwipeToDelete();
    }

    /**
     * Hiển thị hoặc ẩn loading bar
     */
    private void showLoading(boolean show) {
        handler.post(() -> binding.progressBarListItem.setVisibility(show ? View.VISIBLE : View.GONE));
    }

    /**
     * Hiển thị thông báo lỗi
     */
    private void showError(String message) {
        handler.post(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    /**
     * Chuyển về màn hình đăng nhập nếu chưa đăng nhập
     */
    private void redirectToLogin() {
        handler.post(() -> {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử mua", Toast.LENGTH_SHORT).show();
            navigateTo(LoginActivity.class);
            finish();
        });
    }

    /**
     * Thiết lập tính năng vuốt để xóa mục đã mua
     */
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                confirmDelete(position);
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerView);
    }

    /**
     * Hiển thị dialog xác nhận xóa mục
     */
    private void confirmDelete(int position) {
        ItemDomain item = adapter.getItem(position);

        new AlertDialog.Builder(this)
                .setTitle("Xóa mục")
                .setMessage("Bạn có chắc chắn muốn xóa mục này không?")
                .setPositiveButton("Có", (dialog, which) -> deleteItemFromFirestore(position, item))
                .setNegativeButton("Không", (dialog, which) -> adapter.notifyItemChanged(position))
                .create()
                .show();
    }

    /**
     * Xóa mục khỏi Firestore và cập nhật UI
     */
    private void deleteItemFromFirestore(int position, ItemDomain item) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("Purchased")
                    .whereEqualTo("userId", user.getUid())
                    .whereEqualTo("Title", item.getTitle()) // Giả định "Title" là duy nhất
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String docId = task.getResult().getDocuments().get(0).getId();
                            db.collection("Purchased")
                                    .document(docId)
                                    .delete()
                                    .addOnSuccessListener(unused -> handler.post(() -> {
                                        itemList.remove(position);
                                        adapter.notifyItemRemoved(position);
                                        Toast.makeText(this, "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                                    }))
                                    .addOnFailureListener(e -> handler.post(() -> {
                                        adapter.notifyItemChanged(position);
                                        Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                        } else {
                            handler.post(() -> {
                                adapter.notifyItemChanged(position);
                                Toast.makeText(this, "Không tìm thấy mục cần xóa", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });

            executor.shutdown();
        });
    }
}
