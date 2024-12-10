package com.example.travel_app.Activity.user;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.databinding.ActivityTicketBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.github.cdimascio.dotenv.Dotenv;

public class TicketActivity extends BaseActivity {
    private ActivityTicketBinding binding;
    private ItemDomain object;

    // Hiding Key
//    private static final Dotenv dotenv = Dotenv.load();
//    private static final String PublishableKey = dotenv.get("PUBLISHABLE_KEY");
//    private static final String SecretKey = dotenv.get("SECRET_KEY");

    private static final String PublishableKey = "pk_test_51QUTviEDU5x4BEa1f5hyip7KOhkb4DQbCF6P42lm42tWdaSnoFIRYKk3xaHqa66dmQhIpQRftLMM1Ny9oKx3Llih00KsRjS6JH";
    private static final String SecretKey = "sk_test_51QUTviEDU5x4BEa1RnYHZfgxmvg3CBn66K02w3OfWBaBCWYEaPw5tujRAobBlk6B4AFeMBBiUWLZg0zZCDVUoTHc00523u5yqo";

    private String CustomerId;
    private String EphemeralKey;
    private String ClientSecret;
    private PaymentSheet paymentSheet;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    Calendar today = Calendar.getInstance();
    int startDay = today.get(Calendar.DATE);
    int startMonth = today.get(Calendar.MONTH);
    int startYear = today.get(Calendar.YEAR);

    int startHour = today.get(Calendar.HOUR_OF_DAY);
    int startMinute = today.get(Calendar.MINUTE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        getIntentExtra();
        setVariable();

        // Payment
        PaymentConfiguration.init(this, PublishableKey);
        paymentSheet = new PaymentSheet(this, paymentSheetResult -> {
            onPaymentResult(paymentSheetResult);
        });
    }

    private void createCustomer() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/customers",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        CustomerId = object.getString("id");
                        Toast.makeText(TicketActivity.this,
                                "Customer ID: " + CustomerId,
                                Toast.LENGTH_SHORT).show();
                        getEphemeralKey(); // Sau khi tạo khách hàng, lấy khóa tạm thời
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(TicketActivity.this,
                error.getLocalizedMessage(),
                Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);
                return header;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getEphemeralKey() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/ephemeral_keys",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        EphemeralKey = object.getString("id");
                        Toast.makeText(TicketActivity.this, "Ephemeral Key: " + EphemeralKey, Toast.LENGTH_SHORT).show();
                        getClientSecret(); // Sau khi lấy khóa tạm thời, lấy Client Secret
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(TicketActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);
                header.put("Stripe-Version", "2024-06-20");
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getClientSecret() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        ClientSecret = object.getString("client_secret");
                        Log.d("Stripe", "Client Secret: " + ClientSecret);
                        paymentFlow(); // Gọi paymentFlow() sau khi nhận được ClientSecret mới
                    } catch (JSONException e) {
                        Log.e("StripeError", "JSON Parsing Error: " + e.getMessage());
                    }
                }, error -> {
            Log.e("StripeError", "Volley Error: " + error.getLocalizedMessage());
            Toast.makeText(TicketActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);
                params.put("amount", String.valueOf(object.getPrice() * 100)); // Giá trị amount cần là cent
                params.put("currency", "USD");
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    // Khởi động quy trình thanh toán
    private void paymentFlow() {
        paymentSheet.presentWithPaymentIntent(ClientSecret, new PaymentSheet.Configuration(
                "", new PaymentSheet.CustomerConfiguration(
                CustomerId,
                EphemeralKey
        )));
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();

            // lưu thông tin vào firebase
            savePurchaseToFirebase();
            savePurchaseToRealtime();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Thanh toán thất bại!", Toast.LENGTH_SHORT).show();
        }
    }


    private void savePurchaseToRealtime() {
        // Khởi tạo Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Purchased");

        // Lấy số lượng các đối tượng hiện tại trong node "Popular"
        myRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long index = task.getResult().getChildrenCount(); // Lấy số lượng đối tượng hiện có

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

                // Lưu đối tượng mới với key là "index" (0, 1, 2,...)
                myRef.child(String.valueOf(index)).setValue(item)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(TicketActivity.this, "Dữ liệu đã được thêm vào Firebase", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(TicketActivity.this, "Thêm dữ liệu thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(TicketActivity.this, "Không thể lấy dữ liệu Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void savePurchaseToFirebase() {
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(TicketActivity.this, "Vui lòng đăng nhập để mua vé", Toast.LENGTH_SHORT).show();
            return;
        }

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
        item.setUserId(user.getUid());

        // Kiểm tra dữ liệu trước khi gửi
        Log.d("Firestore", "Item: " + new Gson().toJson(item));

        fStore.collection("Purchased")
                .add(item)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(TicketActivity.this, "Dữ liệu đã được thêm vào Firestore", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TicketActivity.this, "Thêm dữ liệu thất bại", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Lỗi: " + e.getMessage());
                    Toast.makeText(TicketActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void setVariable() {
        Random random = new Random();
        StringBuilder orderId = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int digit = random.nextInt(10); // Random digit from 0 to 9
            orderId.append(digit);
        }

        Glide.with(TicketActivity.this)
                .load(object.getPic())
                .into(binding.pic);

        Glide.with(TicketActivity.this)
                .load(object.getTourGuidePic())
                .into(binding.tourGuidePic);
        binding.backBtn.setOnClickListener(view -> finish());
        binding.titleTxt.setText(object.getTitle());
        binding.durationTxt.setText(object.getDuration());
        binding.tourGuideTxt.setText(object.getDateTour());
        binding.timeTxt.setText(object.getTimeTour());
        binding.tourGuideNameTxt.setText(object.getTourGuideName());
        binding.orderIdTxt.setText("Order Id: " + orderId.toString());

        binding.messageBtn.setOnClickListener(view -> {
            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.setData(Uri.parse("sms:" + object.getTourGuidePhone()));
            sendIntent.putExtra("sms_body", "type your message");
            startActivity(sendIntent);
        });

        binding.callBtn.setOnClickListener(view -> {
            String phone = object.getTourGuidePhone();
            Intent intent = new Intent(Intent.ACTION_DIAL,
                    Uri.fromParts("tel", phone, null));
            startActivity(intent);
        });


        binding.calendarBtn.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    binding.tourGuideTxt.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                }
            }, startYear, startMonth, startDay);
            datePickerDialog.show();

        });
        binding.timeBtn.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    if (hourOfDay < 10) {
                        if (minute < 10) {
                            binding.timeTxt.setText("0" + hourOfDay + ":" + "0" + minute);
                        }
                        binding.timeTxt.setText("0" + hourOfDay + ":" + minute);
                    } else {
                        if (minute < 10) {
                            binding.timeTxt.setText(hourOfDay + ":" + "0" + minute);
                        }
                        binding.timeTxt.setText(hourOfDay + ":" + minute);
                    }
                }
            }, startHour, startMinute, false);
            timePickerDialog.show();
        });

        binding.paymentBtn.setOnClickListener(view -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this,
                        "Bạn phải đăng nhập ứng dụng!",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            } else {
                // Người dùng đã đăng nhập, thực hiện thanh toán
                //createCustomer

                savePurchaseToFirebase();
                savePurchaseToRealtime();

                new AlertDialog.Builder(this)
                        .setTitle("Thông báo")
                        .setMessage("Thanh toán thành công!")
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Chuyển về MainActivity
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        })
                        .show();
            }
        });



        binding.downloadTicketBtn.setOnClickListener(view -> {
            // Chụp lại hình ảnh của LinearLayout và lưu nó
            Bitmap bitmap = getBitmapFromView(binding.inforTicket); // Chuyển đổi LinearLayout thành Bitmap
            if (bitmap != null) {
                try {
                    saveImage(bitmap, "ticket_image"); // Lưu hình ảnh
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(TicketActivity.this, "Lưu hình ảnh thất bại!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(TicketActivity.this, "Không có layout ticket để lưu!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private Bitmap getBitmapFromView(View view) {
        // Chụp lại view (LinearLayout) thành Bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void saveImage(Bitmap bitmap, String fileName) {
        OutputStream fos;
        try {
            // Kiểm tra phiên bản Android để chọn phương thức lưu ảnh phù hợp
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Lưu ảnh vào MediaStore cho Android 10 trở lên
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TravelApp");
                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = getContentResolver().openOutputStream(imageUri);
            } else {
                // Lưu ảnh vào bộ nhớ cho các phiên bản Android thấp hơn
                File imageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "TravelApp");
                if (!imageDir.exists()) {
                    imageDir.mkdirs();
                }
                File image = new File(imageDir, fileName + ".jpg");
                fos = new FileOutputStream(image);
            }

            // Nén và lưu bitmap thành tệp hình ảnh
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            Toast.makeText(this, "Lưu hình ảnh thành công!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lưu hình ảnh thất bại!", Toast.LENGTH_SHORT).show();
        }
    }


    private void getIntentExtra() {
        object = (ItemDomain) getIntent().getSerializableExtra("object");
        if (object == null) {
            Toast.makeText(this,
                    "Error: Object data is missing.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }


}
