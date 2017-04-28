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

import java.math.BigDecimal;

public class ExampleService extends Service implements LocationListener {

    static final String TAG = "ExampleService";
    private LocationManager mLocationManager;
    NotificationManager nm;

    BigDecimal x;
    BigDecimal y;

    String xx;
    String yy;


    static double latitude1;
    static double longitude1;

    double latitude2;
    double longitude2;

    private final int REQUEST_PERMISSION = 1000;

    static double ido;
    static double idoido;

    static double keido;
    static double keidokeido;


    @Override
    public void onCreate() {
        Toast.makeText(this, "バックグラウンドサービスを開始しました。", Toast.LENGTH_SHORT).show();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
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
//
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

        // 重要：requestLocationUpdatesしたままアプリを終了すると挙動がおかしくなる。
        mLocationManager.removeUpdates(this);
        Log.i(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
//        Toast.makeText(this, "位置情報を更新", Toast.LENGTH_LONG).show();

        // 緯度の表示
//        TextView tv_lat = (TextView) findViewById(R.id.Latitude);
//        TextView tv_lat = new TextView(this);
//        tv_lat.setText("緯度:" + location.getLatitude());

        Toast.makeText(this, "緯度"+ location.getLatitude()+"経度"+location.getLongitude(), Toast.LENGTH_LONG).show();

//        ido = location.getLatitude();
//        Log.e("GPS", String.valueOf(ido));
//
//
//        x = new BigDecimal(ido);
//        x = x.setScale(7, BigDecimal.ROUND_HALF_UP);
//        Log.e("keta", String.valueOf(x));
//
//        xx = String.valueOf(x);
//        Log.e("xx", xx);
//
//        idoido = Double.parseDouble(xx);
//        Log.e("idoido", String.valueOf(idoido));
//
//        latitude1 = idoido;
//
//        // 経度の表示
////        TextView tv_lng = (TextView) findViewById(R.id.Longitude);
////        tv_lng.setText("経度:" + location.getLongitude());
//
////        int m = r.nextInt(str.length);
////        Toast.makeText(this, str[m], Toast.LENGTH_LONG).show();
//
//        keido = location.getLongitude();
//        Log.e("GPS", String.valueOf(keido));
//
//        y = new BigDecimal(keido);
//        y = y.setScale(7, BigDecimal.ROUND_HALF_UP);
//        Log.e("keta", String.valueOf(y));
//
//        yy = String.valueOf(y);
//        Log.e("yy", yy);
//
//        keidokeido = Double.parseDouble(yy);
//        longitude1 = keidokeido;
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