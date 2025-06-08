package com.example.travel_app.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travel_app.Activity.admin.AdminEditActivity;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.databinding.ViewholderCartBinding;

import java.util.ArrayList;

public class AdminAllTourAdapter extends RecyclerView.Adapter<AdminAllTourAdapter.ViewHolder> {
    private final ArrayList<ItemDomain> itemList;
    private Context context;

    public AdminAllTourAdapter(ArrayList<ItemDomain> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderCartBinding binding = ViewholderCartBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemDomain item = itemList.get(position);
        bindDataToViewHolder(holder, item);
        setupItemClickListener(holder, item);
    }

    /**
     * Gán dữ liệu của item vào các view trong ViewHolder
     */
    private void bindDataToViewHolder(ViewHolder holder, ItemDomain item) {
        holder.binding.titleTxt.setText(item.getTitle());
        holder.binding.priceTxt.setText(item.getPrice() + "VND");
        holder.binding.addressTxt.setText(item.getAddress());
        holder.binding.scoreTxt.setText(String.valueOf(item.getScore()));
        Glide.with(context)
                .load(item.getPic())
                .into(holder.binding.pic);
    }

    /**
     * Thiết lập sự kiện click để chuyển sang màn hình chỉnh sửa
     */
    private void setupItemClickListener(ViewHolder holder, ItemDomain item) {
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, AdminEditActivity.class);
            intent.putExtra("id", item.getId() != null ? item.getId() : "");
            intent.putExtra("title", item.getTitle() != null ? item.getTitle() : "");
            intent.putExtra("price", (double) item.getPrice()); // Chuyển int thành double để khớp với AdminEditActivity
            intent.putExtra("address", item.getAddress() != null ? item.getAddress() : "");
            intent.putExtra("score", item.getScore());
            intent.putExtra("duration", item.getDuration() != null ? item.getDuration() : "");
            intent.putExtra("bed", item.getBed());
            intent.putExtra("time", item.getTimeTour() != null ? item.getTimeTour() : "");
            intent.putExtra("date", item.getDateTour() != null ? item.getDateTour() : "");
            context.startActivity(intent);
        });
    }

    /**
     * Trả về số lượng item trong danh sách
     */
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /**
     * Cập nhật danh sách item và thông báo thay đổi
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateItems(ArrayList<ItemDomain> newItems) {
        this.itemList.clear();
        this.itemList.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Lấy item tại vị trí cụ thể
     */
    public ItemDomain getItem(int position) {
        return itemList.get(position);
    }

    /**
     * ViewHolder để ánh xạ các view trong layout của mỗi item
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ViewholderCartBinding binding;

        public ViewHolder(@NonNull ViewholderCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}