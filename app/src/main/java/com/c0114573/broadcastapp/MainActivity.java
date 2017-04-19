package com.c0114573.broadcastapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by C011457331 on 2017/04/19.
 */

public class MainActivity extends Activity {

    Button bt1,bt2;
    IntentFilter intentFilter;
    BootReceiver receiver;

    private final int REQUEST_PERMISSION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        setContentView(ll);

        bt1 = new Button(this);
        bt2 = new Button(this);

        bt1.setText("開始");
        bt2.setText("停止");

        TextView tv_lat = new TextView(this);
        tv_lat.setText("軽度");

        ll.addView(bt1);
        ll.addView(bt2);
        ll.addView(tv_lat);

        bt1.setOnClickListener(new SampleClickListener());
        bt2.setOnClickListener(new SampleClickListener());


        // Android 6, API 23以上でパーミッシンの確認
        if(Build.VERSION.SDK_INT >= 23){
            checkPermission();
        }
        else{
//            locationActivity();
            startService(new Intent(getBaseContext(), ExampleService.class));

            receiver = new BootReceiver();
            intentFilter = new IntentFilter();
            intentFilter.addAction("BOOT_COMPLETED");
            registerReceiver(receiver, intentFilter);
        }
    }

    // 位置情報許可の確認
    public void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
//            locationActivity();
            startService(new Intent(getBaseContext(), ExampleService.class));

            receiver = new BootReceiver();
            intentFilter = new IntentFilter();
            intentFilter.addAction("BOOT_COMPLETED");
            registerReceiver(receiver, intentFilter);
        }
        // 拒否していた場合
        else{
            requestLocationPermission();
        }
    }

    // 許可を求める
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);

        } else {
            Toast toast = Toast.makeText(this, "許可されないとアプリが実行できません", Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, REQUEST_PERMISSION);

        }
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                locationActivity();
                startService(new Intent(getBaseContext(), ExampleService.class));

                receiver = new BootReceiver();
                intentFilter = new IntentFilter();
                intentFilter.addAction("BOOT_COMPLETED");
                registerReceiver(receiver, intentFilter);
                return;

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this, "これ以上なにもできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }


    }

    class SampleClickListener implements View.OnClickListener {
        public  void onClick(View v){
            if(v == bt1){
                startService(new Intent(getBaseContext(), ExampleService.class));

                receiver = new BootReceiver();
                intentFilter = new IntentFilter();
                intentFilter.addAction("BOOT_COMPLETED");
                registerReceiver(receiver, intentFilter);
            }
            else if(v == bt2){

            }
        }
    }
}