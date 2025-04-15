package com.example.travel_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travel_app.Activity.user.ListItemsActivity;
import com.example.travel_app.Domain.Category;
import com.example.travel_app.R;
import com.example.travel_app.databinding.ViewholderCategoryBinding;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.Viewholder> {
    private final List<Category> items;
    private int selectedPosition = -1;
    private int lastSelectedPosition = -1;
    private Context context;

    public CategoryAdapter(List<Category> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderCategoryBinding binding = ViewholderCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        Category item = items.get(position);
        bindDataToViewHolder(holder, item);
        setupItemClickListener(holder, position);
        updateItemAppearance(holder, position);
    }

    /**
     * Gán dữ liệu của category vào các view trong ViewHolder
     */
    private void bindDataToViewHolder(Viewholder holder, Category item) {
        holder.binding.title.setText(item.getName());
        Glide.with(context)
                .load(item.getImagePath())
                .into(holder.binding.pic);
        holder.binding.title.setTextColor(context.getResources().getColor(R.color.white));
    }

    /**
     * Thiết lập sự kiện click để chuyển sang danh sách item theo category
     */
    private void setupItemClickListener(Viewholder holder, int position) {
        holder.binding.getRoot().setOnClickListener(view -> {
            lastSelectedPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(lastSelectedPosition);
            notifyItemChanged(selectedPosition);
            navigateToListItemsActivity(position);
        });
    }

    /**
     * Chuyển đến ListItemsActivity với thông tin category được chọn
     */
    private void navigateToListItemsActivity(int position) {
        Intent intent = new Intent(context, ListItemsActivity.class);
        intent.putExtra("categoryId", items.get(position).getId());
        intent.putExtra("CategoryName", items.get(position).getName());
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }

    /**
     * Cập nhật giao diện của item dựa trên trạng thái được chọn
     */
    private void updateItemAppearance(Viewholder holder, int position) {
        if (selectedPosition == position) {
            holder.binding.pic.setBackgroundResource(0);
            holder.binding.mainLayout.setBackgroundResource(R.drawable.blue_bg);
            holder.binding.title.setVisibility(View.VISIBLE);
        } else {
            holder.binding.pic.setBackgroundResource(R.drawable.grey_bg);
            holder.binding.mainLayout.setBackgroundResource(0);
            holder.binding.title.setVisibility(View.GONE);
        }
    }

    /**
     * Trả về số lượng item trong danh sách
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder để ánh xạ các view trong layout của mỗi category
     */
    public static class Viewholder extends RecyclerView.ViewHolder {
        final ViewholderCategoryBinding binding;

        public Viewholder(ViewholderCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}