package com.example.travel_app.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travel_app.Activity.user.TicketActivity;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.databinding.ViewholderCartBinding;

import java.util.ArrayList;

public class PurchaseOrderAdapter extends RecyclerView.Adapter<PurchaseOrderAdapter.ViewHolder> {
    private final ArrayList<ItemDomain> items;
    private Context context;

    public PurchaseOrderAdapter(ArrayList<ItemDomain> items) {
        this.items = items != null ? items : new ArrayList<>(); // Khởi tạo danh sách rỗng nếu null
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
        ItemDomain item = items.get(position);
        bindDataToViewHolder(holder.binding, item);
        setupItemClickListener(holder, item);
    }

    /**
     * Gán dữ liệu của đơn hàng vào các view trong ViewHolder
     */
    private void bindDataToViewHolder(ViewholderCartBinding binding, ItemDomain item) {
        binding.titleTxt.setText(item.getTitle());
        binding.priceTxt.setText(item.getPrice() + "VND");
        binding.addressTxt.setText(item.getAddress());
        binding.scoreTxt.setText(String.valueOf(item.getScore()));
        Glide.with(context)
                .load(item.getPic())
                .into(binding.pic);
    }

    /**
     * Thiết lập sự kiện click để chuyển sang màn hình TicketActivity
     */
    private void setupItemClickListener(ViewHolder holder, ItemDomain item) {
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, TicketActivity.class);
            intent.putExtra("object", item);
            context.startActivity(intent);
        });
    }

    /**
     * Trả về số lượng đơn hàng trong danh sách
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder để ánh xạ các view trong layout của mỗi đơn hàng
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ViewholderCartBinding binding;

        public ViewHolder(@NonNull ViewholderCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /**
     * Cập nhật danh sách đơn hàng và thông báo thay đổi
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateItems(ArrayList<ItemDomain> newItems) {
        items.clear();
        items.addAll(newItems != null ? newItems : new ArrayList<>());
        notifyDataSetChanged();
    }

    /**
     * Lấy đơn hàng tại vị trí cụ thể
     */
    public ItemDomain getItem(int position) {
        return items.get(position);
    }
}