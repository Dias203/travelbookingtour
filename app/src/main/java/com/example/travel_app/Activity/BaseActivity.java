package com.example.travel_app.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.travel_app.Utils.NetworkConnectionMonitor;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;

// tái sử dụng mã nguồn, quản lý logic một cách tập trung,
// thiết lập và duy trì cấu hình chung,
// và đảm bảo sự nhất quán trong toàn bộ ứng dụng
public class BaseActivity extends AppCompatActivity implements NetworkConnectionMonitor.NetworkChangeListener {
    protected FirebaseDatabase database;

    private NetworkConnectionMonitor networkMonitor;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();

        // hiển thị full màn
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        // Khởi tạo network monitor
        networkMonitor = new NetworkConnectionMonitor(this, this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy đăng ký network receiver khi activity bị hủy
        if (networkMonitor != null) {
            networkMonitor.unregisterNetworkReceiver();
        }
    }

    @Override
    public void onNetworkConnected() {
        // Ẩn thông báo nếu đang hiển thị
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    @Override
    public void onNetworkDisconnected() {
        // Hiển thị thông báo mất kết nối
        View rootView = findViewById(android.R.id.content);
        snackbar = Snackbar.make(rootView,
                "Mất kết nối internet. Vui lòng kiểm tra lại mạng của bạn.",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
    }
}