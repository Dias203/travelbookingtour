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

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {
    private ArrayList<ItemDomain> items;
    private Context context;

    public BookmarkAdapter(ArrayList<ItemDomain> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public BookmarkAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderCartBinding binding = ViewholderCartBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkAdapter.ViewHolder holder, int position) {
        ItemDomain item = items.get(position);

        holder.binding.titleTxt.setText(item.getTitle());
        holder.binding.priceTxt.setText("$" + item.getPrice());
        holder.binding.addressTxt.setText(item.getAddress());
        holder.binding.scoreTxt.setText("" + item.getScore());

        Glide.with(context)
                .load(item.getPic())
                .into(holder.binding.pic);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, TicketActivity.class);
            intent.putExtra("object", items.get(position));
            context.startActivity(intent);
        });

        // In ra log để kiểm tra giá trị
        Log.d("RecyclerView", "Position: " + position + ", Title: " + item.getTitle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public ItemDomain getItem(int position) {
        return items.get(position);
    }

    public void updateItems(ArrayList<ItemDomain> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderCartBinding binding;

        public ViewHolder(@NonNull ViewholderCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
