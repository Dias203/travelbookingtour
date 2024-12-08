package com.example.travel_app.Activity.admin;

import android.content.DialogInterface;
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
import com.example.travel_app.Adapter.AllTourAdapter;
import com.example.travel_app.Adapter.BookmarkAdapter;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.R;
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
    ActivityAllTourBinding binding;
    private ArrayList<ItemDomain> itemList = new ArrayList<>();
    private AllTourAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllTourBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(v -> finish());
        checkAdminAndLoadData();

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadTourData();
    }

    // Kiểm tra quyền admin và tải dữ liệu
    private void checkAdminAndLoadData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        db.collection("Users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
            if (Boolean.TRUE.equals(isAdmin)) {
                loadTourData();
            } else {
                Toast.makeText(this, "Bạn không có quyền truy cập vào chức năng này", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi kiểm tra quyền admin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Lấy và cập nhật dữ liệu từ Firebase
    private void loadTourData() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Item");
        binding.progressBarListItem.setVisibility(View.VISIBLE);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<ItemDomain> newList = new ArrayList<>();
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        ItemDomain item = issue.getValue(ItemDomain.class);
                        if (item != null) {
                            newList.add(item);
                        }
                    }

                    // Cập nhật dữ liệu cho RecyclerView
                    if (adapter == null) {
                        itemList = newList;
                        adapter = new AllTourAdapter(itemList);
                        binding.recyclerView.setLayoutManager(new LinearLayoutManager(AllTourActivity.this,
                                LinearLayoutManager.VERTICAL, false));
                        binding.recyclerView.setAdapter(adapter);
                        setupSwipeToDelete(binding.recyclerView, adapter, itemList);
                    } else {
                        adapter.updateItems(newList);  // Cập nhật adapter với danh sách mới
                    }
                } else {
                    Toast.makeText(AllTourActivity.this, "Dữ liệu không tồn tại", Toast.LENGTH_SHORT).show();
                }
                binding.progressBarListItem.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarListItem.setVisibility(View.GONE);
                Toast.makeText(AllTourActivity.this, "Lỗi khi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Thiết lập tính năng xoá mục trong RecyclerView
    private void setupSwipeToDelete(RecyclerView recyclerView, AllTourAdapter adapter, List<ItemDomain> list) {
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

                // Hiển thị AlertDialog xác nhận
                new AlertDialog.Builder(AllTourActivity.this)
                        .setTitle("Xóa mục")
                        .setMessage("Bạn có chắc chắn muốn xóa mục này không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            String itemId = item.getId(); // Lấy ID của item từ ItemDomain

                            // Xóa item khỏi danh sách và cập nhật RecyclerView
                            list.remove(position);
                            adapter.notifyItemRemoved(position);

                            // Cập nhật dữ liệu trong Firebase
                            DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("Item").child(itemId);
                            itemRef.removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AllTourActivity.this, "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AllTourActivity.this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Không", (dialog, which) -> adapter.notifyItemChanged(position))
                        .create()
                        .show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}