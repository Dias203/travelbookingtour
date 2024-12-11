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
    ArrayList<ItemDomain> items;
    Context context;
    ViewholderListItemBinding binding;

    public ListItemsAdapter(ArrayList<ItemDomain> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ViewholderListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        context = parent.getContext();
        return new ListItemsAdapter.Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemsAdapter.Viewholder holder, int position) {
        binding.title.setText(items.get(position).getTitle());
        binding.priceTxt.setText(items.get(position).getPrice() + "VND");
        binding.addressTxt.setText(items.get(position).getAddress());
        binding.scoreTxt.setText(""+items.get(position).getScore());

        Glide.with(context)
                .load(items.get(position).getPic())
                .into(binding.pic);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", items.get(position));
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public class Viewholder extends RecyclerView.ViewHolder {
        public Viewholder(ViewholderListItemBinding binding) {
            super(binding.getRoot());
        }
    }
}
