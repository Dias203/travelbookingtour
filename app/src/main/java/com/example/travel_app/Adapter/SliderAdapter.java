package com.example.travel_app.Adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.travel_app.Domain.SliderItems;
import com.example.travel_app.R;

import java.util.ArrayList;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewholder> {
    private final ArrayList<SliderItems> sliderItems;
    private final ViewPager2 viewPager2;
    private Context context;
    private final Handler sliderHandler = new Handler();

    public SliderAdapter(ArrayList<SliderItems> sliderItems, ViewPager2 viewPager2) {
        this.sliderItems = sliderItems;
        this.viewPager2 = viewPager2;
    }

    /** Runnable để tự động chuyển slide sau mỗi 3 giây */
    private final Runnable sliderRunnable = this::moveToNextSlide;

    /** Chuyển đến slide tiếp theo, quay về đầu nếu là slide cuối */
    private void moveToNextSlide() {
        int currentItem = viewPager2.getCurrentItem();
        int nextItem = currentItem + 1;
        if (nextItem >= sliderItems.size()) {
            nextItem = 0;
        }
        viewPager2.setCurrentItem(nextItem, true);
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    @NonNull
    @Override
    public SliderViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        initContext(parent);
        return createSliderViewHolder(parent);
    }

    /** Khởi tạo context từ parent */
    private void initContext(ViewGroup parent) {
        context = parent.getContext();
    }

    /** Tạo mới ViewHolder cho slider item */
    private SliderViewholder createSliderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.slider_item_container, parent, false);
        return new SliderViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewholder holder, int position) {
        holder.bindImage(sliderItems.get(position));
        startAutoSliderIfFirstPosition(position);
    }

    /** Bắt đầu auto slider nếu là vị trí đầu tiên */
    private void startAutoSliderIfFirstPosition(int position) {
        if (position == 0) {
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }

    @Override
    public int getItemCount() {
        return sliderItems.size();
    }

    /** Dừng auto slider khi không cần thiết */
    public void stopSlider() {
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    /** ViewHolder cho mỗi item trong slider */
    public class SliderViewholder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        public SliderViewholder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlide);
        }

        /** Hiển thị hình ảnh lên ImageView sử dụng Glide */
        public void bindImage(SliderItems sliderItems) {
            Glide.with(context)
                    .load(sliderItems.getUrl())
                    .into(imageView);
        }
    }
}