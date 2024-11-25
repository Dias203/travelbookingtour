package com.example.travel_app.Activity.user;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Adapter.ListItemsAdapter;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.databinding.ActivityListItemsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListItemsActivity extends BaseActivity {
    ActivityListItemsBinding binding;
    private int categoryId;
    private String CategoryName;
    private String searchTxt;
    private boolean isSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        initList();
    }

    private void initList() {
        DatabaseReference myRef = database.getReference("Item");
        binding.progressBar.setVisibility(View.VISIBLE);
        ArrayList<ItemDomain> list = new ArrayList<>();
        Query query;
        if(isSearch){
            query = myRef.orderByChild("Title").startAt(searchTxt).endAt(searchTxt+'\uf8ff');
        }
        else{
            query = myRef.orderByChild("categoryId").equalTo(categoryId);
        }
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot issue: snapshot.getChildren()){
                        list.add(issue.getValue(ItemDomain.class));
                    }
                    if(!list.isEmpty()){
                        binding.itemListView.setLayoutManager(new GridLayoutManager(ListItemsActivity.this, 2));
                        RecyclerView.Adapter adapter = new ListItemsAdapter(list);
                        binding.itemListView.setAdapter(adapter);
                    }
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getIntentExtra() {
        categoryId = getIntent().getIntExtra("categoryId", 0);
        CategoryName = getIntent().getStringExtra("CategoryName");
        searchTxt = getIntent().getStringExtra("text");
        isSearch = getIntent().getBooleanExtra("isSearch", false);

        binding.textView6.setText(""+CategoryName);
        binding.backBtn.setOnClickListener(view -> finish());
    }
}