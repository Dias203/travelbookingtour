package com.example.travel_app.Activity.admin;

import android.content.Intent;
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
import com.example.travel_app.Adapter.PurchaseOrderAdapter;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.databinding.ActivityAllOderBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AllOderActivity extends BaseActivity {
    private static final String TAG = "AllOderActivity";

    private ActivityAllOderBinding binding;
    private ArrayList<ItemDomain> itemList = new ArrayList<>();
    private PurchaseOrderAdapter adapter;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Purchased");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllOderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupEventListeners();
        checkAdminAndLoadData();
    }

    /**
     * Thiết lập các sự kiện click cho các nút
     */
    private void setupEventListeners() {
        binding.backBtn.setOnClickListener(v -> finish());
    }

    /**
     * Kiểm tra quyền admin và tải dữ liệu nếu hợp lệ
     */
    private void checkAdminAndLoadData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            showToast("Vui lòng đăng nhập");
            navigateTo(LoginActivity.class);
            finish();
            return;
        }

        firestore.collection("Users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                    if (Boolean.TRUE.equals(isAdmin)) {
                        loadPurchasedData();
                    } else {
                        showToast("Bạn không có quyền truy cập vào chức năng này");
                        finish();
                    }
                })
                .addOnFailureListener(e -> showToast("Lỗi kiểm tra quyền admin: " + e.getMessage()));
    }

    /**
     * Tải dữ liệu đơn hàng từ Realtime Database
     */
    private void loadPurchasedData() {
        binding.progressBarListItem.setVisibility(View.VISIBLE);
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    itemList.clear();
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        ItemDomain item = issue.getValue(ItemDomain.class);
                        if (item != null) {
                            itemList.add(item);
                        }
                    }
                    if (!itemList.isEmpty()) {
                        setupRecyclerView();
                    }
                }
                binding.progressBarListItem.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarListItem.setVisibility(View.GONE);
                showToast("Lỗi tải dữ liệu: " + error.getMessage());
            }
        });
    }

    /**
     * Thiết lập RecyclerView để hiển thị danh sách đơn hàng
     */
    private void setupRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new PurchaseOrderAdapter(itemList);
        binding.recyclerView.setAdapter(adapter);
        //setupSwipeToDelete();
    }

    /**
     * Thiết lập chức năng vuốt để xóa đơn hàng
     */
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
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
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.recyclerView);
    }

    /**
     * Hiển thị dialog xác nhận xóa đơn hàng
     */
    private void showDeleteConfirmationDialog(int position, ItemDomain item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa mục")
                .setMessage("Bạn có chắc chắn muốn xóa mục này không?")
                .setPositiveButton("Có", (dialog, which) -> deleteItem(position, item))
                .setNegativeButton("Không", (dialog, which) -> adapter.notifyItemChanged(position))
                .create()
                .show();
    }

    /**
     * Xóa đơn hàng từ cả Realtime Database và Firestore
     */
    private void deleteItem(int position, ItemDomain item) {
        itemList.remove(position);
        adapter.notifyItemRemoved(position);

        databaseRef.child(item.getId()).removeValue()
                .addOnSuccessListener(aVoid -> deleteFromFirestore(item))
                .addOnFailureListener(e -> showToast("Lỗi khi xóa từ Realtime Database: " + e.getMessage()));
    }

    /**
     * Xóa đơn hàng từ Firestore sau khi xóa thành công từ Realtime Database
     */
    private void deleteFromFirestore(ItemDomain item) {
        firestore.collection("Purchased").document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> showToast("Đã xóa thành công"))
                .addOnFailureListener(e -> showToast("Lỗi khi xóa từ Firestore: " + e.getMessage()));
    }

    /**
     * Điều hướng đến một activity khác
     */
    private void navigateTo(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    /**
     * Hiển thị thông báo Toast
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}