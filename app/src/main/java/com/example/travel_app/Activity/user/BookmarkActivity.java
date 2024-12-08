package com.example.travel_app.Activity.user;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Adapter.BookmarkAdapter;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.R;
import com.example.travel_app.databinding.ActivityBookmarkBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.List;

public class BookmarkActivity extends BaseActivity {
    ActivityBookmarkBinding binding;
    private ArrayList<ItemDomain> itemList = new ArrayList<>();
    private BookmarkAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookmarkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initBottomNav();
        initPurchased();
    }

    /*private void initPurchased() {
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
                        binding.recyclerView.setLayoutManager(new LinearLayoutManager(BookmarkActivity.this,
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
    }*/
    private void initPurchased() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử mua", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
        binding.progressBarListItem.setVisibility(View.VISIBLE);

        // Lấy dữ liệu từ collection "Purchased"
        db.collection("Purchased").whereEqualTo("userId", user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                itemList.clear(); // Xóa dữ liệu cũ trước khi thêm dữ liệu mới
                for (DocumentSnapshot document : task.getResult()) {
                    ItemDomain item = document.toObject(ItemDomain.class);
                    if (item != null) {
                        itemList.add(item);
                    }
                }
                if (!itemList.isEmpty()) {
                    binding.recyclerView.setLayoutManager(new LinearLayoutManager(BookmarkActivity.this,
                            LinearLayoutManager.VERTICAL, false));
                    adapter = new BookmarkAdapter(itemList);
                    binding.recyclerView.setAdapter(adapter);
                    setupSwipeToDelete(binding.recyclerView, adapter, itemList);
                }
            } else {
                Toast.makeText(BookmarkActivity.this, "Không thể tải dữ liệu từ Firestore", Toast.LENGTH_SHORT).show();
            }
            binding.progressBarListItem.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            binding.progressBarListItem.setVisibility(View.GONE);
            Toast.makeText(BookmarkActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                new AlertDialog.Builder(BookmarkActivity.this)
                        .setTitle("Xóa mục")
                        .setMessage("Bạn có chắc chắn muốn xóa mục này không?")
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String itemId = item.getId(); // Lấy ID của item từ ItemDomain

                                // Xóa item khỏi danh sách và cập nhật RecyclerView
                                list.remove(position);
                                adapter.notifyItemRemoved(position);

                                // Cập nhật dữ liệu trong Firebase
                                DatabaseReference myRef = database.getReference("Purchased");
                                myRef.child(String.valueOf(itemId)).removeValue(); // Xóa item khỏi Firebase bằng ID
                            }
                        })
                        .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Khôi phục lại item nếu người dùng chọn không xóa
                                adapter.notifyItemChanged(position);
                            }
                        })
                        .create()
                        .show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void initBottomNav() {
        binding.bottomnav.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                if (i == R.id.home) {
                    Intent intent = new Intent(BookmarkActivity.this, MainActivity.class);
                    startActivity(intent);
                } else if (i == R.id.profile) {
                    Intent intent = new Intent(BookmarkActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } else if (i == R.id.explorer) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                    intent.setPackage("com.android.chrome"); // Đảm bảo rằng Chrome được sử dụng
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        // Xử lý trường hợp Chrome không được cài đặt
                        intent.setPackage(null); // Khôi phục về trình duyệt mặc định
                        startActivity(intent);
                    }
                }
            }
        });
    }
}
