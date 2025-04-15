package com.example.travel_app.Activity.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Adapter.UserAllTourAdapter;
import com.example.travel_app.Adapter.CategoryAdapter;
import com.example.travel_app.Adapter.PopularAdapter;
import com.example.travel_app.Adapter.RecommendedAdapter;
import com.example.travel_app.Adapter.SliderAdapter;
import com.example.travel_app.Domain.Category;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.Domain.Location;
import com.example.travel_app.Domain.SliderItems;
import com.example.travel_app.R;
import com.example.travel_app.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private SliderAdapter sliderAdapter;
    private final ArrayList<ItemDomain> itemsList = new ArrayList<>();
    private UserAllTourAdapter searchAdapter;
    private final ArrayList<ItemDomain> searchList = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService mainExecutor = Executors.newFixedThreadPool(4);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initComponents();
        loadAllData();
    }

    /**
     * Khởi tạo tất cả các thành phần UI và chức năng
     */
    private void initComponents() {
        initSearchView();
        initBottomNavigation();
    }

    /**
     * Khởi chạy tải dữ liệu từ Firebase
     */
    private void loadAllData() {
        // Tải dữ liệu vị trí trước tiên
        initLocation();

        // Tải dữ liệu tìm kiếm
        fetchDataForSearch();

        // Tải dữ liệu song song cho các phần khác
        mainExecutor.execute(this::initBanner);
        mainExecutor.execute(this::initCategory);
        mainExecutor.execute(this::initRecommended);
        mainExecutor.execute(this::initPopular);
    }

    /**
     * Khởi tạo thành phần tìm kiếm
     */
    private void initSearchView() {
        // Thiết lập RecyclerView kết quả tìm kiếm
        setupSearchRecyclerView();

        // Thiết lập TextWatcher cho ô tìm kiếm
        setupSearchTextWatcher();
    }

    /**
     * Thiết lập RecyclerView cho kết quả tìm kiếm
     */
    private void setupSearchRecyclerView() {
        binding.progressBarSearch.setVisibility(View.GONE);
        binding.searchResults.setVisibility(View.GONE);
        searchAdapter = new UserAllTourAdapter(searchList);
        binding.searchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.searchResults.setAdapter(searchAdapter);
    }

    /**
     * Thiết lập TextWatcher cho ô tìm kiếm
     */
    private void setupSearchTextWatcher() {
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần thực hiện gì
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (!s.toString().isEmpty()) {
                        filterSearchResults(s.toString());
                    } else {
                        binding.searchResults.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Lỗi khi tìm kiếm: " + e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Đã xử lý trong onTextChanged
            }
        });
    }

    /**
     * Lọc kết quả tìm kiếm dựa trên văn bản nhập vào
     * @param query Văn bản tìm kiếm
     */
    private void filterSearchResults(String query) {
        binding.progressBarSearch.setVisibility(View.VISIBLE);
        try {
            ArrayList<ItemDomain> filteredList = new ArrayList<>();

            // Lọc các item có tiêu đề chứa query
            for (ItemDomain item : itemsList) {
                if (item.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }

            // Cập nhật adapter với kết quả lọc
            updateSearchResults(filteredList);

        } catch (Exception e) {
            e.printStackTrace();
            showToast("Lỗi khi lọc kết quả: " + e.getMessage());
        } finally {
            binding.progressBarSearch.setVisibility(View.GONE);
        }
    }

    /**
     * Cập nhật kết quả tìm kiếm lên UI
     * @param filteredList Danh sách đã lọc
     */
    private void updateSearchResults(ArrayList<ItemDomain> filteredList) {
        searchList.clear();
        if (!filteredList.isEmpty()) {
            searchList.addAll(filteredList);
            searchAdapter.notifyDataSetChanged();
            binding.searchResults.setVisibility(View.VISIBLE);
        } else {
            binding.searchResults.setVisibility(View.GONE);
        }
    }

    /**
     * Tải dữ liệu cho tính năng tìm kiếm
     */
    private void fetchDataForSearch() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            DatabaseReference myRef = database.getReference("Item");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        itemsList.clear();
                        for (DataSnapshot issue : snapshot.getChildren()) {
                            ItemDomain item = issue.getValue(ItemDomain.class);
                            if (item != null) {
                                itemsList.add(item);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    handler.post(() -> showToast("Lỗi khi lấy dữ liệu: " + error.getMessage()));
                }
            });
        });
        executor.shutdown();
    }

    /**
     * Khởi tạo thanh điều hướng phía dưới
     */
    private void initBottomNavigation() {
        binding.bottomnav.setOnItemSelectedListener(i -> {
            if (i == R.id.explorer) {
                openWebBrowser("http://www.google.com");
            } else if (i == R.id.cart) {
                startActivity(new Intent(MainActivity.this, PurchasedActivity.class));
            } else if (i == R.id.profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });
    }

    /**
     * Mở trình duyệt web với URL chỉ định
     * @param url URL cần mở
     */
    private void openWebBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage("com.android.chrome");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            intent.setPackage(null);
            startActivity(intent);
        }
    }

    /**
     * Khởi tạo và tải dữ liệu cho phần Popular
     */
    private void initPopular() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        binding.progressBarPopular.setVisibility(View.VISIBLE);
        ArrayList<ItemDomain> list = new ArrayList<>();

        executor.execute(() -> {
            DatabaseReference myRef = database.getReference("Item");
            Query query = myRef.orderByChild("popular").equalTo(true);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        list.clear();
                        for (DataSnapshot issue : snapshot.getChildren()) {
                            ItemDomain item = issue.getValue(ItemDomain.class);
                            if (item != null) {
                                list.add(item);
                            }
                        }

                        handler.post(() -> {
                            setupHorizontalRecyclerView(binding.recyclerViewPopular, new PopularAdapter(list));
                            binding.progressBarPopular.setVisibility(View.GONE);
                        });
                    } else {
                        handler.post(() -> binding.progressBarPopular.setVisibility(View.GONE));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    handler.post(() -> binding.progressBarPopular.setVisibility(View.GONE));
                }
            });
        });
        executor.shutdown();
    }

    /**
     * Khởi tạo và tải dữ liệu cho phần Recommended
     */
    private void initRecommended() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        binding.progressBarRecommended.setVisibility(View.VISIBLE);
        ArrayList<ItemDomain> list = new ArrayList<>();

        executor.execute(() -> {
            DatabaseReference myRef = database.getReference("Item");
            Query query = myRef.orderByChild("recommended").equalTo(true);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        list.clear();
                        for (DataSnapshot issue : snapshot.getChildren()) {
                            ItemDomain item = issue.getValue(ItemDomain.class);
                            if (item != null) {
                                list.add(item);
                            }
                        }

                        handler.post(() -> {
                            setupHorizontalRecyclerView(binding.recyclerViewRecommended, new RecommendedAdapter(list));
                            binding.progressBarRecommended.setVisibility(View.GONE);
                        });
                    } else {
                        handler.post(() -> binding.progressBarRecommended.setVisibility(View.GONE));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    handler.post(() -> binding.progressBarRecommended.setVisibility(View.GONE));
                }
            });
        });
        executor.shutdown();
    }

    /**
     * Khởi tạo và tải dữ liệu cho phần Category
     */
    private void initCategory() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        ArrayList<Category> list = new ArrayList<>();

        executor.execute(() -> {
            DatabaseReference myRef = database.getReference("Category");

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        list.clear();
                        for (DataSnapshot issue : snapshot.getChildren()) {
                            Category category = issue.getValue(Category.class);
                            if (category != null) {
                                list.add(category);
                            }
                        }

                        handler.post(() -> {
                            setupHorizontalRecyclerView(binding.recyclerViewCategory, new CategoryAdapter(list));
                            binding.progressBarCategory.setVisibility(View.GONE);
                        });
                    } else {
                        handler.post(() -> binding.progressBarCategory.setVisibility(View.GONE));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    handler.post(() -> binding.progressBarCategory.setVisibility(View.GONE));
                }
            });
        });
        executor.shutdown();
    }

    /**
     * Khởi tạo và tải dữ liệu vị trí cho spinner
     */
    private void initLocation() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ArrayList<Location> list = new ArrayList<>();

        executor.execute(() -> {
            DatabaseReference myRef = database.getReference("Location");

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        list.clear();
                        for (DataSnapshot issue : snapshot.getChildren()) {
                            Location location = issue.getValue(Location.class);
                            if (location != null) {
                                list.add(location);
                            }
                        }

                        handler.post(() -> {
                            setupLocationSpinner(list);
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    handler.post(() -> showToast("Lỗi khi lấy location: " + error.getMessage()));
                }
            });
        });
        executor.shutdown();
    }

    /**
     * Thiết lập spinner vị trí
     * @param locations Danh sách vị trí
     */
    private void setupLocationSpinner(ArrayList<Location> locations) {
        ArrayAdapter<Location> adapter = new ArrayAdapter<>(
                MainActivity.this,
                R.layout.sp_item,
                locations
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.locationSp.setAdapter(adapter);
    }

    /**
     * Thiết lập và cấu hình banner slider
     * @param items Danh sách các mục banner
     */
    private void setupBannerSlider(ArrayList<SliderItems> items) {
        sliderAdapter = new SliderAdapter(items, binding.viewPagerSlider);
        binding.viewPagerSlider.setAdapter(sliderAdapter);
        binding.viewPagerSlider.setClipToPadding(false);
        binding.viewPagerSlider.setClipChildren(false);
        binding.viewPagerSlider.setOffscreenPageLimit(3);
        binding.viewPagerSlider.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        binding.viewPagerSlider.setPageTransformer(compositePageTransformer);
    }

    /**
     * Khởi tạo và tải dữ liệu cho banner
     */
    private void initBanner() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        binding.progressBarBanner.setVisibility(View.VISIBLE);
        ArrayList<SliderItems> items = new ArrayList<>();

        executor.execute(() -> {
            DatabaseReference myRef = database.getReference("Banner");

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        items.clear();
                        for (DataSnapshot issue : snapshot.getChildren()) {
                            SliderItems item = issue.getValue(SliderItems.class);
                            if (item != null) {
                                items.add(item);
                            }
                        }

                        handler.post(() -> {
                            setupBannerSlider(items);
                            binding.progressBarBanner.setVisibility(View.GONE);
                        });
                    } else {
                        handler.post(() -> binding.progressBarBanner.setVisibility(View.GONE));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    handler.post(() -> binding.progressBarBanner.setVisibility(View.GONE));
                }
            });
        });
        executor.shutdown();
    }

    /**
     * Thiết lập RecyclerView theo chiều ngang
     * @param recyclerView RecyclerView cần thiết lập
     * @param adapter Adapter để hiển thị dữ liệu
     */
    private void setupHorizontalRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(
                MainActivity.this,
                LinearLayoutManager.HORIZONTAL,
                false
        ));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Hiển thị thông báo Toast
     * @param message Thông báo cần hiển thị
     */
    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Giải phóng tài nguyên
        if (sliderAdapter != null) {
            sliderAdapter.stopSlider();
        }

        // Đóng ExecutorService
        if (!mainExecutor.isShutdown()) {
            mainExecutor.shutdown();
        }
    }
}