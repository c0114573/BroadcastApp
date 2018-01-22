package com.c0114573.broadcastapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import static android.view.Gravity.TOP;

/**
 * Created by member on 2017/06/20.
 */

public class WindowService extends Service {

    public View player_view;
    private WindowManager wm;

    private LocationManager locationmanager;

    static final String TAG = "ExampleService";

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
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//                |  WindowManager.LayoutParams.FLAG_FULLSCREEN
//                |  WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                ,
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

        // 表示位置を決める
        params.gravity = Gravity.LEFT|TOP;


        player_view = inflater.inflate(R.layout.window_test, null);

        // ボタンをつくる
        final Button button1 = (Button) player_view.findViewById(R.id.wm_button1);
        final Button button2 = (Button) player_view.findViewById(R.id.wm_button2);
        final Button button3 = (Button) player_view.findViewById(R.id.wm_button3);
        final Button button4 = (Button) player_view.findViewById(R.id.wm_button4);


        // ボタンが押されたかどうか監視する
        button1.setOnClickListener( new View.OnClickListener() {
            //ボタンが押されたら何かする
            @Override
            public void onClick(View v) {

                if (v == button1) {
                    switch (button2.getVisibility()) {
                        case View.VISIBLE:
                            button2.setVisibility(View.GONE);
                            button3.setVisibility(View.GONE);
                            button4.setVisibility(View.GONE);
                            break;
                        case View.GONE:
                            button2.setVisibility(View.VISIBLE);
                            button3.setVisibility(View.VISIBLE);
                            button4.setVisibility(View.VISIBLE);
                            break;
                    }
                }
            }
        });

        button2.setOnClickListener( new View.OnClickListener() {
            //ボタンが押されたら何かする
            @Override
            public void onClick(View v) {
                Log.d("TEST", "22222222");

                if (v == button2) {
                    switch (button2.getVisibility()) {
                        case View.VISIBLE:
                            button2.setVisibility(View.GONE);
                            button3.setVisibility(View.GONE);
                            button4.setVisibility(View.GONE);
                            break;
                        case View.GONE:
                            button2.setVisibility(View.VISIBLE);
                            button3.setVisibility(View.VISIBLE);
                            button4.setVisibility(View.VISIBLE);
                            break;
                    }
                }

                Intent intent = new Intent(getApplication(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });

        button3.setOnClickListener( new View.OnClickListener() {
            //ボタンが押されたら何かする
            @Override
            public void onClick(View v) {
                Log.d("TEST", "33333333");

                if (v == button3) {
                    switch (button2.getVisibility()) {
                        case View.VISIBLE:
                            button2.setVisibility(View.GONE);
                            button3.setVisibility(View.GONE);
                            button4.setVisibility(View.GONE);
                            break;
                        case View.GONE:
                            button2.setVisibility(View.VISIBLE);
                            button3.setVisibility(View.VISIBLE);
                            button4.setVisibility(View.VISIBLE);
                            break;
                    }
                }

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        });

        button4.setOnClickListener( new View.OnClickListener() {
            //ボタンが押されたら何かする
            @Override
            public void onClick(View v) {
                Log.d("TEST", "444444444");

                locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
                String provider = "";
                if (locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    //GPSが利用可能
                    provider = "gps";

                } else if (locationmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    //ネットワークが利用可能
                    provider = "network";
                } else {
                    //位置情報の利用不可能
                    provider = "使えません";


                    //この画面を終了
                    onDestroy();
                }
                Log.d("TEST", provider);
//                Toast.makeText(this, provider, Toast.LENGTH_SHORT).show();

            }
        });

        wm.addView(player_view, params);

        //明示的にサービスの起動、停止が決められる場合の返り値
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        Toast.makeText(this, "ウィンドウ終了します。", Toast.LENGTH_SHORT).show();
//        mLocationManager.removeUpdates(this);
        try{
            wm.removeView(player_view);
        }catch (IllegalArgumentException e){

        }

        Log.i(TAG, "onDestroy");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
