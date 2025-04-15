package com.example.travel_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travel_app.Activity.user.TicketActivity;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.databinding.ViewholderCartBinding;

import java.util.ArrayList;

public class PurchasedAdapter extends RecyclerView.Adapter<PurchasedAdapter.ViewHolder> {
    private static final String TAG = "PurchasedAdapter";

    private final ArrayList<ItemDomain> items;
    private Context context;

    public PurchasedAdapter(ArrayList<ItemDomain> items) {
        this.items = items;
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
        logItemDetails(position, item);
    }

    /**
     * Gán dữ liệu của item vào các view trong ViewHolder
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
     * Ghi log thông tin item để kiểm tra
     */
    private void logItemDetails(int position, ItemDomain item) {
        Log.d(TAG, "Position: " + position + ", Title: " + item.getTitle());
    }

    /**
     * Trả về số lượng item trong danh sách
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Lấy item tại vị trí cụ thể
     */
    public ItemDomain getItem(int position) {
        return items.get(position);
    }

    /**
     * Cập nhật danh sách item và thông báo thay đổi
     */
    public void updateItems(ArrayList<ItemDomain> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
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