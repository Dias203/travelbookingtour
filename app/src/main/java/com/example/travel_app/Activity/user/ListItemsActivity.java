package com.example.travel_app.Activity.user;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Adapter.ListItemsAdapter;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.databinding.ActivityListItemsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListItemsActivity extends BaseActivity {
    private ActivityListItemsBinding binding;
    private int categoryId;
    private String categoryName;
    private String searchText;
    private boolean isSearch;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtras();
        setupUI();
        loadItemList();
    }

    /**
     * Lấy dữ liệu từ Intent đã gửi đến Activity này
     */
    private void getIntentExtras() {
        categoryId = getIntent().getIntExtra("categoryId", 0);
        categoryName = getIntent().getStringExtra("CategoryName");
        searchText = getIntent().getStringExtra("text");
        isSearch = getIntent().getBooleanExtra("isSearch", false);
    }

    /**
     * Thiết lập các thành phần UI
     */
    private void setupUI() {
        // Thiết lập tiêu đề
        binding.textView6.setText(categoryName);

        // Thiết lập nút trở lại
        binding.backBtn.setOnClickListener(view -> finish());
    }

    /**
     * Tải và hiển thị danh sách các mục
     */
    private void loadItemList() {
        showProgressBar(true);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            fetchItemsFromDatabase();
        });
        executor.shutdown();
    }

    /**
     * Truy vấn và lấy dữ liệu từ Firebase database
     */
    private void fetchItemsFromDatabase() {
        DatabaseReference myRef = database.getReference("Item");
        Query query = createQuery(myRef);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                processDataSnapshot(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleDatabaseError(error);
            }
        });
    }

    /**
     * Tạo truy vấn dựa vào loại hiển thị (tìm kiếm hoặc theo danh mục)
     *
     * @param reference Tham chiếu đến database
     * @return Query đã tạo
     */
    private Query createQuery(DatabaseReference reference) {
        if (isSearch) {
            return reference.orderByChild("Title").startAt(searchText).endAt(searchText + "\uf8ff");
        } else {
            return reference.orderByChild("categoryId").equalTo(categoryId);
        }
    }

    /**
     * Xử lý dữ liệu nhận được từ DataSnapshot
     *
     * @param snapshot DataSnapshot từ Firebase
     */
    private void processDataSnapshot(DataSnapshot snapshot) {
        if (snapshot.exists()) {
            ArrayList<ItemDomain> itemList = extractItemsFromSnapshot(snapshot);
            handler.post(() -> {
                updateUI(itemList);
                showProgressBar(false);
            });
        } else {
            handler.post(() -> {
                showProgressBar(false);
                showToast("Không tìm thấy dữ liệu");
            });
        }
    }

    /**
     * Trích xuất các mục từ DataSnapshot và chuyển đổi thành ItemDomain
     *
     * @param snapshot DataSnapshot chứa dữ liệu
     * @return Danh sách các đối tượng ItemDomain
     */
    private ArrayList<ItemDomain> extractItemsFromSnapshot(DataSnapshot snapshot) {
        ArrayList<ItemDomain> itemList = new ArrayList<>();
        for (DataSnapshot issue : snapshot.getChildren()) {
            ItemDomain item = issue.getValue(ItemDomain.class);
            if (item != null) {
                itemList.add(item);
            }
        }
        return itemList;
    }

    /**
     * Xử lý lỗi khi truy vấn database
     *
     * @param error Lỗi database
     */
    private void handleDatabaseError(DatabaseError error) {
        handler.post(() -> {
            showProgressBar(false);
            showToast("Lỗi khi tải dữ liệu: " + error.getMessage());
        });
    }

    /**
     * Cập nhật giao diện người dùng với danh sách mục đã tải
     *
     * @param itemList Danh sách các mục cần hiển thị
     */
    private void updateUI(ArrayList<ItemDomain> itemList) {
        if (!itemList.isEmpty()) {
            setupRecyclerView(itemList);
        } else {
            showToast("Không có mục nào phù hợp");
        }
    }

    /**
     * Thiết lập RecyclerView để hiển thị danh sách mục
     *
     * @param itemList Danh sách các mục cần hiển thị
     */
    private void setupRecyclerView(ArrayList<ItemDomain> itemList) {
        // Sử dụng GridLayoutManager để hiển thị 2 cột
        binding.itemListView.setLayoutManager(new GridLayoutManager(this, 2));

        // Tạo và thiết lập adapter
        RecyclerView.Adapter adapter = new ListItemsAdapter(itemList);
        binding.itemListView.setAdapter(adapter);
    }

    /**
     * Hiển thị hoặc ẩn thanh tiến trình
     *
     * @param show true để hiển thị, false để ẩn
     */
    private void showProgressBar(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Hiển thị thông báo toast
     *
     * @param message Nội dung thông báo
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}