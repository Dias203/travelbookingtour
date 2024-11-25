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
    private ArrayList<SliderItems> sliderItems;
    private ViewPager2 viewPager2;
    private Context context;
    private Handler sliderHandler = new Handler();

    public SliderAdapter(ArrayList<SliderItems> sliderItems, ViewPager2 viewPager2) {
        this.sliderItems = sliderItems;
        this.viewPager2 = viewPager2;
    }

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            int currentItem = viewPager2.getCurrentItem();
            int nextItem = currentItem + 1;
            if (nextItem >= sliderItems.size()) {
                nextItem = 0;
            }
            viewPager2.setCurrentItem(nextItem, true);
            sliderHandler.postDelayed(this, 3000); // Chuyển đổi sau 3 giây
        }
    };

    @NonNull
    @Override
    public SliderViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Khởi tạo SliderViewholder bằng cách inflate layout của từng item trong slider
        context = parent.getContext();
        return new SliderViewholder(LayoutInflater.from(context).inflate(R.layout.slider_item_container, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewholder holder, int position) {
        // Gán hình ảnh tương ứng với mỗi vị trí từ sliderItems vào ImageView.
        holder.setImage(sliderItems.get(position));

        // Bắt đầu chạy slider tự động từ vị trí đầu tiên
        if (position == 0) {
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }

    @Override
    public int getItemCount() {
        return sliderItems.size();
    }

    public class SliderViewholder extends RecyclerView.ViewHolder{
        private ImageView imageView;

        public SliderViewholder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlide);
        }

        void setImage(SliderItems sliderItems) {
            Glide.with(context)
                    .load(sliderItems.getUrl())
                    .into(imageView);
        }
    }

    // Gọi hàm này trong Activity hoặc Fragment để ngừng slider khi không cần thiết
    public void stopSlider() {
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}
