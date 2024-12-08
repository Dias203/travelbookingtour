package com.example.travel_app.Activity.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Activity.user.BookmarkActivity;
import com.example.travel_app.Activity.user.LoginActivity;
import com.example.travel_app.Activity.user.MainActivity;
import com.example.travel_app.Activity.user.ProfileActivity;
import com.example.travel_app.Adapter.BookmarkAdapter;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.R;
import com.example.travel_app.databinding.ActivityAllOderBinding;
import com.example.travel_app.databinding.ActivityBookmarkBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AllOderActivity extends BaseActivity {
    ActivityAllOderBinding binding;
    private ArrayList<ItemDomain> itemList = new ArrayList<>();
    private BookmarkAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllOderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(v -> finish());
        checkAdminAndLoadData();
    }

    // Kiểm tra phân quyền admin
    private void checkAdminAndLoadData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Kiểm tra quyền admin trong collection "Users"
        db.collection("Users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
            if (Boolean.TRUE.equals(isAdmin)) {
                initPurchased();
            } else {
                Toast.makeText(this, "Bạn không có quyền truy cập vào chức năng này", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi kiểm tra quyền admin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Lấy từ realtime database
    private void initPurchased() {
        DatabaseReference myRef = database.getReference("Purchased");
        binding.progressBarListItem.setVisibility(View.VISIBLE);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    itemList.clear(); // Xóa dữ liệu cũ trước khi thêm dữ liệu mới
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        ItemDomain item = issue.getValue(ItemDomain.class);
                        if (item != null) {
                            itemList.add(item);
                        }
                    }
                    if (!itemList.isEmpty()) {
                        binding.recyclerView.setLayoutManager(new LinearLayoutManager(AllOderActivity.this,
                                LinearLayoutManager.VERTICAL, false));
                        adapter = new BookmarkAdapter(itemList);
                        binding.recyclerView.setAdapter(adapter);
                        setupSwipeToDelete(binding.recyclerView, adapter, itemList);
                    }
                }
                binding.progressBarListItem.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarListItem.setVisibility(View.GONE);
            }
        });
    }

    private void setupSwipeToDelete(RecyclerView recyclerView, BookmarkAdapter adapter, List<ItemDomain> list) {
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
                new AlertDialog.Builder(AllOderActivity.this)
                        .setTitle("Xóa mục")
                        .setMessage("Bạn có chắc chắn muốn xóa mục này không?")
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                list.remove(position);
                                adapter.notifyItemRemoved(position);

                                // Xóa dữ liệu trong Realtime Database
                                DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("Purchased").child(item.getId());
                                itemRef.removeValue()
                                        .addOnSuccessListener(aVoid -> {
                                            // Sau khi xóa Realtime Database, xóa trong Firestore
                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                            db.collection("Purchased").document(item.getId()).delete()
                                                    .addOnSuccessListener(aVoid2 -> {
                                                        Toast.makeText(AllOderActivity.this, "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(AllOderActivity.this, "Lỗi khi xóa từ Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(AllOderActivity.this, "Lỗi khi xóa từ Realtime Database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
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
