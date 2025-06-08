package com.example.travel_app.Activity.user;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.ZaloPaySdk.Api.CreateOrder;
import com.example.travel_app.databinding.ActivityTicketBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class TicketActivity extends BaseActivity {
    private static final String TAG = "TicketActivity";
    private static final String[] ZALOPAY_PACKAGES = {
            "com.vng.zalo",        // Bản chính thức
            "com.vng.zalopay.sdk",
            "vn.com.vng.zalopay.sb1" // Bản sandbox
    };

    private ActivityTicketBinding binding;
    private ItemDomain object;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        initZaloPaySDK();
        setVariable();
    }

    /**
     * Khởi tạo SDK ZaloPay với cấu hình sandbox
     */
    private void initZaloPaySDK() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ZaloPaySDK.init(2553, vn.zalopay.sdk.Environment.SANDBOX);
    }

    /**
     * Lấy dữ liệu từ Intent và kiểm tra tính hợp lệ
     */
    private void getIntentExtra() {
        object = (ItemDomain) getIntent().getSerializableExtra("object");
        if (object == null) {
            showToast("Error: Object data is missing.");
            finish();
            return;
        }

        // KIỂM TRA VÀ HIỂN THỊ SỐ LƯỢNG NGƯỜI ĐÃ MUA
        if (object.getNumOfPeople() > 0) {
            // Nếu có dữ liệu số lượng người đã lưu, hiển thị nó
            binding.numOfPeople.setText(String.valueOf(object.getNumOfPeople()));
        } else {
            // Mặc định là 1 nếu chưa có dữ liệu
            binding.numOfPeople.setText("1");
        }
    }

    /**
     * Thiết lập các sự kiện và hiển thị dữ liệu lên giao diện
     */
    private void setVariable() {
        String orderId = generateOrderId();
        setupUI(orderId);
        setupEventListeners();
    }

    /**
     * Tạo mã đơn hàng ngẫu nhiên 8 chữ số
     */
    private String generateOrderId() {
        Random random = new Random();
        StringBuilder orderId = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            orderId.append(random.nextInt(10));
        }
        return orderId.toString();
    }

    /**
     * Cài đặt giao diện với dữ liệu từ object
     */
    private void setupUI(String orderId) {
        Glide.with(this).load(object.getPic()).into(binding.pic);
        Glide.with(this).load(object.getTourGuidePic()).into(binding.tourGuidePic);
        binding.titleTxt.setText(object.getTitle());
        binding.durationTxt.setText(object.getDuration());
        binding.tourGuideTxt.setText(object.getDateTour());
        binding.timeTxt.setText(object.getTimeTour());
        binding.tourGuideNameTxt.setText(object.getTourGuideName());
        binding.orderIdTxt.setText("Order Id: " + orderId);
        binding.orderIdBarcode.setText(orderId);
    }

    /**
     * Thiết lập các sự kiện click cho các nút
     */
    private void setupEventListeners() {
        if(!isNetworkConnected()) {
            showToast("Mất kết nối internet! Kiểm tra lại kết nối");
        }
        binding.backBtn.setOnClickListener(v -> finish());
        binding.messageBtn.setOnClickListener(v -> sendSMS());
        binding.callBtn.setOnClickListener(v -> makeCall());
        binding.calendarBtn.setOnClickListener(v -> showDatePicker());
        binding.timeBtn.setOnClickListener(v -> showTimePicker());
        binding.paymentBtn.setOnClickListener(v -> processPayment());
        binding.downloadTicketBtn.setOnClickListener(v -> downloadTicket());
    }

    /**
     * Gửi tin nhắn SMS đến hướng dẫn viên
     */
    private void sendSMS() {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:" + object.getTourGuidePhone()));
        sendIntent.putExtra("sms_body", "type your message");
        startActivity(sendIntent);
    }

    /**
     * Thực hiện cuộc gọi đến hướng dẫn viên
     */
    private void makeCall() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", object.getTourGuidePhone(), null));
        startActivity(intent);
    }

    /**
     * Hiển thị dialog chọn ngày
     */
    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) ->
                        binding.tourGuideTxt.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear),
                year, month, day);
        datePickerDialog.show();
    }

    /**
     * Hiển thị dialog chọn giờ
     */
    private void showTimePicker() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) ->
                        binding.timeTxt.setText(String.format("%02d:%02d", selectedHour, selectedMinute)),
                hour, minute, false);
        timePickerDialog.show();
    }

    /**
     * Xử lý thanh toán qua ZaloPay
     */
    private void processPayment() {
        if (!isZaloPayInstalled()) {
            showZaloPayInstallDialog();
            return;
        }

        String numOfPeople = binding.numOfPeople.getText().toString();
        if (numOfPeople.isEmpty()) {
            showToast("Vui lòng nhập số lượng người");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            showToast("Vui lòng đăng nhập để thực hiện thanh toán!");
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        String totalString = calculateTotal(numOfPeople);
        executePayment(totalString);
    }

    /**
     * Tính tổng tiền dựa trên số lượng người
     */
    private String calculateTotal(String numOfPeople) {
        return String.format("%.0f", object.getPrice() * Integer.parseInt(numOfPeople) * 1000);
    }

    /**
     * Thực thi thanh toán trên luồng background
     */
    private void executePayment(String totalString) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                JSONObject data = new CreateOrder().createOrder(totalString);
                String code = data.getString("return_code");

                if ("1".equals(code)) {
                    String token = data.getString("zp_trans_token");
                    handler.post(() -> ZaloPaySDK.getInstance().payOrder(
                            this, token, "demozpdk://app", createPayOrderListener()));
                }
            } catch (Exception e) {
                handler.post(() -> showToast("Lỗi thanh toán: " + e.getMessage()));
            } finally {
                executor.shutdown();
            }
        });
    }

    /**
     * Lưu vé dưới dạng hình ảnh
     */
    private void downloadTicket() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Bitmap bitmap = getBitmapFromView(binding.inforTicket);
            if (bitmap != null) {
                try {
                    saveImage(bitmap, "ticket_image");
                    handler.post(() -> showToast("Lưu hình ảnh thành công!"));
                } catch (Exception e) {
                    handler.post(() -> showToast("Lưu hình ảnh thất bại!"));
                }
            } else {
                handler.post(() -> showToast("Không có layout ticket để lưu!"));
            }
            executor.shutdown();
        });
    }

    /**
     * Chuyển view thành bitmap
     */
    private Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * Lưu bitmap thành file hình ảnh
     */
    private void saveImage(Bitmap bitmap, String fileName) throws Exception {
        OutputStream fos;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName + ".jpg");
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TravelApp");
            Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            fos = getContentResolver().openOutputStream(imageUri);
        } else {
            File imageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "TravelApp");
            if (!imageDir.exists()) imageDir.mkdirs();
            File image = new File(imageDir, fileName + ".jpg");
            fos = new FileOutputStream(image);
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
    }

    /**
     * Lưu thông tin mua vé vào Realtime Database
     */
    private void savePurchaseToRealtime() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Purchased");
            ref.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    long index = task.getResult().getChildrenCount();
                    ItemDomain item = createItemDomain();
                    ref.child(String.valueOf(index)).setValue(item)
                            .addOnCompleteListener(t -> handler.post(() ->
                                    showToast(t.isSuccessful() ?
                                            "Dữ liệu đã được thêm vào Firebase" :
                                            "Thêm dữ liệu thất bại")));
                } else {
                    handler.post(() -> showToast("Không thể lấy dữ liệu Firebase"));
                }
            });
            executor.shutdown();
        });
    }

    /**
     * Lưu thông tin mua vé vào Firestore
     */
    private void savePurchaseToFirebase() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                handler.post(() -> showToast("Vui lòng đăng nhập để mua vé"));
                return;
            }

            ItemDomain item = createItemDomain();
            item.setUserId(user.getUid());
            Log.d(TAG, "Item: " + new Gson().toJson(item));

            FirebaseFirestore.getInstance().collection("Purchased")
                    .add(item)
                    .addOnCompleteListener(task -> handler.post(() ->
                            showToast(task.isSuccessful() ?
                                    "Dữ liệu đã được thêm vào Firestore" :
                                    "Thêm dữ liệu thất bại")))
                    .addOnFailureListener(e -> handler.post(() -> {
                        Log.e(TAG, "Lỗi: " + e.getMessage());
                        showToast("Lỗi: " + e.getMessage());
                    }));
            executor.shutdown();
        });
    }

    /**
     * XÓA TOUR KHỎI GIỎ HÀNG SAU KHI THANH TOÁN THÀNH CÔNG
     */
    private void removeFromCart() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                handler.post(() -> showToast("Không thể xóa khỏi giỏ hàng: Chưa đăng nhập"));
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Tìm và xóa tour khỏi collection Cart
            db.collection("Cart")
                    .whereEqualTo("userId", user.getUid())
                    .whereEqualTo("title", object.getTitle()) // Sử dụng title để identify tour
                    .whereEqualTo("id", object.getId()) // Thêm id để đảm bảo chính xác
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Xóa tất cả documents match (thường chỉ có 1)
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("Cart")
                                        .document(document.getId())
                                        .delete()
                                        .addOnSuccessListener(unused -> {
                                            Log.d(TAG, "Tour đã được xóa khỏi giỏ hàng thành công");
                                            handler.post(() -> showToast("Đã xóa tour khỏi giỏ hàng"));
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Lỗi khi xóa tour khỏi giỏ hàng: " + e.getMessage());
                                            handler.post(() -> showToast("Lỗi khi xóa khỏi giỏ hàng: " + e.getMessage()));
                                        });
                            }
                        } else {
                            Log.w(TAG, "Không tìm thấy tour trong giỏ hàng để xóa");
                            handler.post(() -> showToast("Không tìm thấy tour trong giỏ hàng"));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi tìm kiếm tour trong giỏ hàng: " + e.getMessage());
                        handler.post(() -> showToast("Lỗi khi tìm kiếm trong giỏ hàng: " + e.getMessage()));
                    });

            executor.shutdown();
        });
    }

    /**
     * Tạo đối tượng ItemDomain từ dữ liệu hiện tại
     */
    private ItemDomain createItemDomain() {
        ItemDomain item = new ItemDomain();
        item.setTitle(object.getTitle());
        item.setPrice(object.getPrice());
        item.setBed(object.getBed());
        item.setId(object.getId());
        item.setAddress(object.getAddress());
        item.setDuration(object.getDuration());
        item.setDistance(object.getDistance());
        item.setDescription(object.getDescription());
        item.setScore(object.getScore());
        item.setTimeTour(object.getTimeTour());
        item.setDateTour(object.getDateTour());
        item.setPic(object.getPic());
        item.setTourGuideName(object.getTourGuideName());
        item.setTourGuidePhone(object.getTourGuidePhone());
        item.setTourGuidePic(object.getTourGuidePic());
        String numOfPeopleStr = binding.numOfPeople.getText().toString();
        int numOfPeople = numOfPeopleStr.isEmpty() ? 1 : Integer.parseInt(numOfPeopleStr);
        item.setNumOfPeople(numOfPeople);
        return item;
    }

    /**
     * Kiểm tra xem ZaloPay đã được cài đặt chưa
     */
    private boolean isZaloPayInstalled() {
        PackageManager pm = getPackageManager();
        for (String packageName : ZALOPAY_PACKAGES) {
            try {
                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException ignored) {}
        }
        return false;
    }

    /**
     * Hiển thị dialog yêu cầu cài đặt ZaloPay
     */
    private void showZaloPayInstallDialog() {
        new AlertDialog.Builder(this)
                .setTitle("ZaloPay chưa được cài đặt")
                .setMessage("Bạn cần cài đặt ZaloPay để tiếp tục thanh toán")
                .setPositiveButton("Cài đặt", (dialog, which) -> openZaloPayInstallPage())
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Mở trang cài đặt ZaloPay trên Play Store hoặc trình duyệt
     */
    private void openZaloPayInstallPage() {
        try {
            for (String packageName : ZALOPAY_PACKAGES) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + packageName)));
                    return;
                } catch (ActivityNotFoundException ignored) {}
            }
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.vng.zalo")));
        } catch (Exception e) {
            showToast("Không thể mở cửa hàng ứng dụng");
        }
    }

    /**
     * Tạo listener cho kết quả thanh toán ZaloPay
     */
    private PayOrderListener createPayOrderListener() {
        return new PayOrderListener() {
            @Override
            public void onPaymentSucceeded(String transactionId, String transToken, String appTransId) {
                // Lưu vào Purchased
                savePurchaseToFirebase();
                savePurchaseToRealtime();

                // XÓA KHỎI GIỎ HÀNG SAU KHI THANH TOÁN THÀNH CÔNG
                removeFromCart();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onPaymentError(ZaloPayError error, String transactionId, String transToken) {
                Intent intent = new Intent(getApplicationContext(), TicketActivity.class);
                intent.putExtra("payment_error", error.toString());
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }

            @Override
            public void onPaymentCanceled(String transactionId, String transToken) {
                Intent intent = new Intent(getApplicationContext(), TicketActivity.class);
                intent.putExtra("payment_canceled", "true");
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        };
    }

    /**
     * Xử lý kết quả từ ZaloPay khi quay lại activity
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);

        // Kiểm tra nếu có thông báo lỗi từ thanh toán
        String error = intent.getStringExtra("payment_error");
        String canceled = intent.getStringExtra("payment_canceled");

        if (error != null) {
            showToast("Lỗi thanh toán: " + error);
        } else if (canceled != null) {
            showToast("Bạn đã hủy thanh toán");
        }
    }

    /**
     * Hiển thị thông báo Toast
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Kiểm tra kết nối mạng
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}