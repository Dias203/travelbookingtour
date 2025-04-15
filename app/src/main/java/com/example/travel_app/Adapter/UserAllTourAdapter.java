package com.example.travel_app.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travel_app.Activity.admin.AdminEditActivity;
import com.example.travel_app.Activity.user.DetailActivity;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.databinding.ViewholderCartBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class UserAllTourAdapter extends RecyclerView.Adapter<UserAllTourAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ItemDomain> itemList;

    public UserAllTourAdapter(ArrayList<ItemDomain> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return createViewHolder(parent);
    }

    /** Tạo ViewHolder từ layout binding */
    private ViewHolder createViewHolder(ViewGroup parent) {
        ViewholderCartBinding binding = ViewholderCartBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemDomain item = itemList.get(position);
        bindItemData(holder, item);
        setupItemClickListener(holder, item, position);
    }

    /** Hiển thị dữ liệu item lên view */
    private void bindItemData(ViewHolder holder, ItemDomain item) {
        holder.binding.titleTxt.setText(item.getTitle());
        holder.binding.priceTxt.setText(item.getPrice() + "VND");
        holder.binding.addressTxt.setText(item.getAddress());
        holder.binding.scoreTxt.setText("" + item.getScore());

        Glide.with(context)
                .load(item.getPic())
                .into(holder.binding.pic);
    }

    /** Xử lý sự kiện click vào item */
    private void setupItemClickListener(ViewHolder holder, ItemDomain item, int position) {
        holder.itemView.setOnClickListener(view -> {
            checkUserRoleAndNavigate(item, position);
        });
    }

    /** Kiểm tra role user và chuyển đến màn hình phù hợp */
    private void checkUserRoleAndNavigate(ItemDomain item, int position) {
        DocumentReference df = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(item.getUserId());

        df.get().addOnSuccessListener(documentSnapshot -> {
            if (Boolean.TRUE.equals(documentSnapshot.getBoolean("isAdmin"))) {
                navigateToAdminEdit(item);
            } else {
                navigateToDetail(itemList.get(position));
            }
        }).addOnFailureListener(e -> {
            Log.e("UserAllTourAdapter", "Error checking user role", e);
        });
    }

    /** Chuyển đến màn hình AdminEdit */
    private void navigateToAdminEdit(ItemDomain item) {
        Intent intent = new Intent(context, AdminEditActivity.class);
        intent.putExtra("id", item.getId());
        intent.putExtra("title", item.getTitle());
        intent.putExtra("price", item.getPrice());
        intent.putExtra("address", item.getAddress());
        intent.putExtra("score", item.getScore());
        context.startActivity(intent);
    }

    /** Chuyển đến màn hình Detail */
    private void navigateToDetail(ItemDomain item) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra("object", item);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /** Cập nhật danh sách item mới */
    @SuppressLint("NotifyDataSetChanged")
    public void updateItems(ArrayList<ItemDomain> newItems) {
        this.itemList = newItems;
        notifyDataSetChanged();
    }

    /** Lấy item tại vị trí position */
    public ItemDomain getItem(int position) {
        return itemList.get(position);
    }

    /** ViewHolder cho item */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ViewholderCartBinding binding;

        public ViewHolder(@NonNull ViewholderCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}