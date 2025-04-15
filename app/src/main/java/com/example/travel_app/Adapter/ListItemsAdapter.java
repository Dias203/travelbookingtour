package com.example.travel_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travel_app.Activity.user.DetailActivity;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.databinding.ViewholderListItemBinding;

import java.util.ArrayList;

public class ListItemsAdapter extends RecyclerView.Adapter<ListItemsAdapter.Viewholder> {
    private final ArrayList<ItemDomain> items;
    private Context context;

    public ListItemsAdapter(ArrayList<ItemDomain> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderListItemBinding binding = ViewholderListItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        ItemDomain item = items.get(position);
        bindDataToViewHolder(holder.binding, item);
        setupItemClickListener(holder, item);
    }

    /**
     * Gán dữ liệu của item vào các view trong ViewHolder
     */
    private void bindDataToViewHolder(ViewholderListItemBinding binding, ItemDomain item) {
        binding.title.setText(item.getTitle());
        binding.priceTxt.setText(item.getPrice() + "VND");
        binding.addressTxt.setText(item.getAddress());
        binding.scoreTxt.setText(String.valueOf(item.getScore()));
        Glide.with(context)
                .load(item.getPic())
                .into(binding.pic);
    }

    /**
     * Thiết lập sự kiện click để chuyển sang màn hình chi tiết
     */
    private void setupItemClickListener(Viewholder holder, ItemDomain item) {
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", item);
            context.startActivity(intent);
        });
    }

    /**
     * Trả về số lượng item trong danh sách
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder để ánh xạ các view trong layout của mỗi item
     */
    public static class Viewholder extends RecyclerView.ViewHolder {
        final ViewholderListItemBinding binding;

        public Viewholder(ViewholderListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}