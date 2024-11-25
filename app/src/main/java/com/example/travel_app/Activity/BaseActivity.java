package com.example.travel_app.Activity;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

// tái sử dụng mã nguồn, quản lý logic một cách tập trung,
// thiết lập và duy trì cấu hình chung,
// và đảm bảo sự nhất quán trong toàn bộ ứng dụng
public class BaseActivity extends AppCompatActivity {
    protected FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();

        // hiển thị full màn
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
}