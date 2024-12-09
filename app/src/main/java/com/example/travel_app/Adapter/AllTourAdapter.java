package com.example.travel_app.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travel_app.Activity.admin.AdminEditActivity;
import com.example.travel_app.Activity.user.BookmarkActivity;
import com.example.travel_app.Activity.user.DetailActivity;
import com.example.travel_app.Activity.user.TicketActivity;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.R;
import com.example.travel_app.databinding.ViewholderCartBinding;

import java.util.ArrayList;
import java.util.List;

public class AllTourAdapter extends RecyclerView.Adapter<AllTourAdapter.ViewHolder> {
    Context context;
    private ArrayList<ItemDomain> itemList;

    public AllTourAdapter(ArrayList<ItemDomain> itemList) {
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

        holder.binding.titleTxt.setText(item.getTitle());
        holder.binding.priceTxt.setText("$" + item.getPrice());
        holder.binding.addressTxt.setText(item.getAddress());
        holder.binding.scoreTxt.setText("" + item.getScore());

        Glide.with(context)
                .load(item.getPic())
                .into(holder.binding.pic);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", itemList.get(position));
            context.startActivity(intent);
        });


        /*// Chuyển sang màn hình sửa thông tin sản phẩm
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, AdminEditActivity.class);
            intent.putExtra("id", item.getId());
            intent.putExtra("title", item.getTitle());
            intent.putExtra("price", item.getPrice());
            intent.putExtra("address", item.getAddress());
            intent.putExtra("score", item.getScore());
            context.startActivity(intent);
        });*/
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // Phương thức để cập nhật dữ liệu trong adapter khi có thay đổi
    @SuppressLint("NotifyDataSetChanged")
    public void updateItems(ArrayList<ItemDomain> newItems) {
        this.itemList = newItems;
        notifyDataSetChanged();
    }

    public ItemDomain getItem(int position) {
        return itemList.get(position);
    }

    // ViewHolder để ánh xạ các view trong item layout
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderCartBinding binding;

        public ViewHolder(@NonNull ViewholderCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
