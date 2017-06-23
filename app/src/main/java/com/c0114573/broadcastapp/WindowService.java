package com.c0114573.broadcastapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import static android.view.Gravity.TOP;

/**
 * Created by member on 2017/06/20.
 */

public class WindowService extends Service {

    public View player_view;
    private WindowManager wm;

    static final String TAG = "ExampleService";

    @Override
    public void onCreate() {
//        Toast.makeText(this, "バックグラウンドサービスを開始しました。", Toast.LENGTH_SHORT).show();
//        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "onStartCommand Received start id " + startId + ": " + intent);


        wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        //画面に常に表示するビューのレイアウトの設定
        // 横幅
        // 高さ
        // 表示レイヤー指定
        // Viewの挙動/動作の規定
        // ピクセルフォーマット指定

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

//        final Button closeButton
//                = (Button) player_view.findViewById(R.id.close_button);
//        closeButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                onDestroy();
//            }
//        });

        //表示位置を決める
        params.gravity = Gravity.LEFT | TOP;

        //        player_view = inflater.inflate(R.layout.always_show_view, null);
        player_view = inflater.inflate(R.layout.window_test, null);

        wm.addView(player_view, params);

        //明示的にサービスの起動、停止が決められる場合の返り値
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "ウィンドウ終了します。", Toast.LENGTH_SHORT).show();
//        mLocationManager.removeUpdates(this);
        wm.removeView(player_view);

        Log.i(TAG, "onDestroy");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
