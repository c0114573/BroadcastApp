package com.c0114573.broadcastapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.regex.Pattern;

public class ExampleService extends Service implements LocationListener {

    static final String TAG = "ExampleService";
    private LocationManager mLocationManager;
    NotificationManager nm;

    double confLatitude = 0;    // 設定緯度
    double confLongitude = 0;   // 設定経度

    double myLatitude = 0;    // 現在の緯度
    double myLongitude = 0;   // 現在の経度


    String targetStr = new String("");    // 緯度経度を持ってくる
    String message = "";

    @Override
    public void onCreate() {
        Toast.makeText(this, "バックグラウンドサービスを開始しました。", Toast.LENGTH_SHORT).show();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // 登録位置情報読み取り
        try {
            FileInputStream fis = openFileInput("test.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String tmp;
//            tv.setText("");
            while ((tmp = reader.readLine()) != null) {
//                tv.append(tmp + "\n");
                targetStr += tmp;
            }
            reader.close();


            // 読み取りを行った位置情報をdouble型にそれぞれ格納
            Pattern pattern = Pattern.compile(",");
            String[] splitStr = pattern.split(targetStr);
            for (int i = 0; i < splitStr.length; i++) {
                System.out.println(splitStr[i]);
            }
            // ここでエラー NumberFormatException
            confLatitude = Double.parseDouble(splitStr[0]);
            confLongitude = Double.parseDouble(splitStr[1]);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("登録されてない");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand Received start id " + startId + ": " + intent);

        if (mLocationManager != null) {
            Log.d("LocationActivity", "locationManager.requestLocationUpdates");
            // バックグラウンドから戻ってしまうと例外が発生する場合がある

            try {
                // minTime = 1000msec, minDistance = 50m
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    return;
                }

                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, this);

            } catch (Exception e) {
                e.printStackTrace();

                Toast toast = Toast.makeText(this, "例外が発生、位置情報のPermissionを許可していますか？", Toast.LENGTH_SHORT);
                toast.show();

//                //MainActivityに戻す
//                finish();
            }
        }
        //明示的にサービスの起動、停止が決められる場合の返り値
        return START_STICKY;
    }

    private void showNotification() {
        Log.d("Debug TEST", "showNotification");

        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        Notification nf = new Notification.Builder(this)
                .setContentTitle("サンプル")
                .setContentText("設定画面に移動")
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .build();
        nm.notify(0, nf);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "バックグラウンドサービスを終了します。", Toast.LENGTH_SHORT).show();
        mLocationManager.removeUpdates(this);
        Log.i(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
//        Toast.makeText(this, "緯度" + location.getLatitude() + "経度"+location.getLongitude(),
//                Toast.LENGTH_LONG).show();

        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();

//        // 登録位置情報読み取り
//        try {
//            FileInputStream fis = openFileInput("test.txt");
//            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
//            String tmp;
////            tv.setText("");
//            while ((tmp = reader.readLine()) != null) {
////                tv.append(tmp + "\n");
//                targetStr += tmp;
//            }
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        Pattern pattern = Pattern.compile(",");
//        String[] splitStr = pattern.split(targetStr);
//        for (int i = 0; i < splitStr.length; i++) {
//            System.out.println(splitStr[i]);
//        }
//        confLatitude = Double.parseDouble(splitStr[0]);
//        confLongitude =  Double.parseDouble(splitStr[1]);

        Toast.makeText(this, "登録" + confLatitude + "," + confLongitude
                + "\n現在" + myLatitude + "," + myLongitude, Toast.LENGTH_LONG).show();

        float results2 = getDistanceBetween(confLatitude, confLongitude, myLatitude, myLongitude);

        // 3km 以内
        if (results2 < 3000) {
            message = "範囲内";
        } else {
            message = "範囲外";
        }

        Toast.makeText(this, "距離" + results2 + "m" + message, Toast.LENGTH_LONG).show();
    }


    public float getDistanceBetween(
            double latitude1, double longitude1,
            double latitude2, double longitude2) {

        float[] results = new float[3];
        // 2点間の距離を取得（第5引数にセットされることに注意！）
        Location.distanceBetween(latitude1, longitude1, latitude2, longitude2, results);

        if (results.length < 1) {
            // 取得に失敗した場合のコード
        }

        if (results.length < 3) {
            // 方位角の値を取得した場合のコード
        }

        // ここでは距離（メートル）のみを返却
        return results[0];
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

}