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

public class PurchaseOrderAdapter extends RecyclerView.Adapter<PurchaseOrderAdapter.ViewHolder> {
    private static final String TAG = "PurchasedAdapter";

    private final ArrayList<ItemDomain> items;
    private Context context;

    public PurchaseOrderAdapter(ArrayList<ItemDomain> items) {
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
        // Kiểm tra bounds trước khi lấy item
        if (position < 0 || position >= items.size()) {
            Log.e(TAG, "Invalid position: " + position + ", list size: " + items.size());
            return;
        }

        ItemDomain item = items.get(position);
        if (item == null) {
            Log.e(TAG, "Item is null at position: " + position);
            return;
        }

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
            //intent.putExtra("numPeople", item.getNumOfPeople()); // thêm dòng này
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
        return items != null ? items.size() : 0;
    }

    /**
     * Lấy item tại vị trí cụ thể - AN TOÀN với kiểm tra bounds
     */
    public ItemDomain getItem(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        Log.e(TAG, "getItem: Invalid position " + position + " for list size " + items.size());
        return null;
    }

    /**
     * Cập nhật danh sách item và thông báo thay đổi
     */
    public void updateItems(ArrayList<ItemDomain> newItems) {
        if (newItems != null) {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }
    }

    /**
     * Xóa item tại vị trí cụ thể
     */
    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
            // Cập nhật lại các position sau khi xóa
            notifyItemRangeChanged(position, items.size());
        }
    }

    /**
     * Lấy danh sách items hiện tại
     */
    public ArrayList<ItemDomain> getItems() {
        return items;
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