package com.example.travel_app.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * Utility class để theo dõi trạng thái kết nối mạng
 */
public class NetworkConnectionMonitor {
    private final Context context;
    private BroadcastReceiver networkReceiver;
    private final NetworkChangeListener listener;

    public interface NetworkChangeListener {
        void onNetworkConnected();
        void onNetworkDisconnected();
    }

    public NetworkConnectionMonitor(Context context, NetworkChangeListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        initializeNetworkMonitor();
    }

    /** Khởi tạo màn hình theo dõi mạng */
    private void initializeNetworkMonitor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerNetworkReceiver();
        }
    }

    /** Đăng ký BroadcastReceiver để lắng nghe thay đổi mạng */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void registerNetworkReceiver() {
        if (networkReceiver != null) return;

        networkReceiver = createNetworkReceiver();
        IntentFilter filter = createNetworkIntentFilter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(networkReceiver, filter,
                    Context.RECEIVER_NOT_EXPORTED);
        }
    }

    /** Tạo BroadcastReceiver để xử lý thay đổi mạng */
    private BroadcastReceiver createNetworkReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleNetworkChange();
            }
        };
    }

    /** Tạo IntentFilter cho thay đổi kết nối mạng */
    private IntentFilter createNetworkIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        return filter;
    }

    /** Xử lý khi có thay đổi trạng thái mạng */
    private void handleNetworkChange() {
        if (isNetworkConnected()) {
            listener.onNetworkConnected();
        } else {
            listener.onNetworkDisconnected();
        }
    }

    /** Hủy đăng ký BroadcastReceiver */
    public void unregisterNetworkReceiver() {
        if (networkReceiver != null) {
            try {
                context.unregisterReceiver(networkReceiver);
                networkReceiver = null;
            } catch (IllegalArgumentException e) {
                // Receiver chưa được đăng ký
            }
        }
    }

    /** Kiểm tra thiết bị có kết nối mạng không */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}