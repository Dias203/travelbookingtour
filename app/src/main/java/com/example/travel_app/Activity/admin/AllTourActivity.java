package com.example.travel_app.Activity.admin;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Activity.user.LoginActivity;
import com.example.travel_app.Adapter.AdminAllTourAdapter;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.databinding.ActivityAllTourBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AllTourActivity extends BaseActivity {
    private ActivityAllTourBinding binding;
    private ArrayList<ItemDomain> itemList = new ArrayList<>();
    private AdminAllTourAdapter adapter;
    private FirebaseFirestore firestore;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllTourBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFirebaseInstances();
        setupUI();
        checkAdminAndLoadData();
    }

    /**
     * Khởi tạo các instance Firebase
     */
    private void initFirebaseInstances() {
        firestore = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Item");
    }

    /**
     * Cài đặt giao diện người dùng
     */
    private void setupUI() {
        binding.backBtn.setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isUserAuthenticated() && adapter != null) {
            loadTourData();
        }
    }

    /**
     * Kiểm tra xem người dùng đã đăng nhập chưa
     * @return true nếu đã đăng nhập, false nếu chưa
     */
    private boolean isUserAuthenticated() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            redirectToLogin("Vui lòng đăng nhập");
            return false;
        }
        return true;
    }

    /**
     * Chuyển hướng người dùng đến màn hình đăng nhập
     * @param message Thông báo để hiển thị
     */
    private void redirectToLogin(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    /**
     * Kiểm tra quyền admin và tải dữ liệu
     */
    private void checkAdminAndLoadData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            redirectToLogin("Vui lòng đăng nhập");
            return;
        }

        firestore.collection("Users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                    if (Boolean.TRUE.equals(isAdmin)) {
                        loadTourData();
                    } else {
                        showToast("Bạn không có quyền truy cập vào chức năng này");
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Lỗi kiểm tra quyền admin: " + e.getMessage());
                    finish();
                });
    }

    /**
     * Hiển thị thông báo toast
     * @param message Thông báo cần hiển thị
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Lấy và cập nhật dữ liệu từ Firebase
     */
    private void loadTourData() {
        if (!isNetworkAvailable()) {
            showToast("Không có kết nối Internet. Vui lòng kiểm tra lại!");
            return;
        }

        showProgressBar(true);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<ItemDomain> newList = extractItemsFromSnapshot(snapshot);
                    updateRecyclerView(newList);
                } else {
                    showToast("Dữ liệu không tồn tại");
                }
                showProgressBar(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showProgressBar(false);
                showToast("Lỗi khi tải dữ liệu: " + error.getMessage());
            }
        });
    }

    /**
     * Trích xuất danh sách các mục từ DataSnapshot
     * @param snapshot DataSnapshot từ Firebase
     * @return ArrayList chứa các ItemDomain
     */
    private ArrayList<ItemDomain> extractItemsFromSnapshot(DataSnapshot snapshot) {
        ArrayList<ItemDomain> newList = new ArrayList<>();
        for (DataSnapshot issue : snapshot.getChildren()) {
            ItemDomain item = issue.getValue(ItemDomain.class);
            if (item != null) {
                newList.add(item);
            }
        }
        return newList;
    }

    /**
     * Hiển thị hoặc ẩn thanh tiến trình
     * @param show true để hiển thị, false để ẩn
     */
    private void showProgressBar(boolean show) {
        binding.progressBarListItem.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Cập nhật RecyclerView với danh sách mới
     * @param newList Danh sách mới các ItemDomain
     */
    private void updateRecyclerView(ArrayList<ItemDomain> newList) {
        if (adapter == null) {
            itemList = newList;
            adapter = new AdminAllTourAdapter(itemList);
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(
                    AllTourActivity.this, LinearLayoutManager.VERTICAL, false));
            binding.recyclerView.setAdapter(adapter);
            setupSwipeToDelete();
        } else {
            adapter.updateItems(newList);
        }
    }

    /**
     * Thiết lập tính năng xoá mục trong RecyclerView
     */
    private void setupSwipeToDelete() {
        if (!isNetworkAvailable()) {
            showToast("Không có kết nối Internet. Vui lòng kiểm tra lại!");
            return;
        }

        ItemTouchHelper.SimpleCallback swipeCallback = createSwipeCallback();
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recyclerView);
    }

    /**
     * Tạo callback để xử lý sự kiện vuốt để xóa
     * @return ItemTouchHelper.SimpleCallback
     */
    private ItemTouchHelper.SimpleCallback createSwipeCallback() {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ItemDomain item = adapter.getItem(position);
                showDeleteConfirmationDialog(position, item);
            }
        };
    }

    /**
     * Hiển thị hộp thoại xác nhận xóa mục
     * @param position Vị trí của mục trong danh sách
     * @param item Mục cần xóa
     */
    private void showDeleteConfirmationDialog(int position, ItemDomain item) {
        new AlertDialog.Builder(AllTourActivity.this)
                .setTitle("Xóa mục")
                .setMessage("Bạn có chắc chắn muốn xóa mục này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    deleteItem(position, item);
                })
                .setNegativeButton("Không", (dialog, which) -> adapter.notifyItemChanged(position))
                .create()
                .show();
    }

    /**
     * Xóa mục khỏi danh sách và Firebase
     * @param position Vị trí của mục trong danh sách
     * @param item Mục cần xóa
     */
    private void deleteItem(int position, ItemDomain item) {
        String itemId = item.getId();

        // Xóa khỏi danh sách cục bộ và cập nhật RecyclerView
        itemList.remove(position);
        adapter.notifyItemRemoved(position);

        // Xóa từ Firebase
        databaseReference.child(itemId).removeValue()
                .addOnSuccessListener(aVoid -> showToast("Đã xóa thành công"))
                .addOnFailureListener(e -> showToast("Lỗi khi xóa: " + e.getMessage()));
    }

    /**
     * Kiểm tra kết nối mạng
     * @return true nếu có kết nối mạng, false nếu không
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}