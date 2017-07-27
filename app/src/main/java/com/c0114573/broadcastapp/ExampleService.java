package com.c0114573.broadcastapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;
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


    public ArrayList<String> array = new ArrayList<String>();
    String result = "";
    String sendPackageLabel = "";
    String sendPermission = "";

    boolean isPackage = false;
    boolean isLocationApp = false;
    boolean isLocked = false;
    boolean locationInto = false;

    boolean windowShowed = false;

    int appUsedCount = 0;

    private final static String TAG2 = "";

    // Toastを何回表示されたか数えるためのカウント
    private int mCount = 0;

    // Toastを表示させるために使うハンドラ
    private Handler mHandler = new Handler();

    // スレッドを停止するために必要
    private boolean mThreadActive = true;


    // スレッド処理
    private Runnable mTask = new Runnable() {

        @Override
        public void run() {

            // アクティブな間だけ処理をする
            while (mThreadActive) {

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // TODO 自動生成された catch ブロック
                    e.printStackTrace();
                }

                // ハンドラーをはさまないとToastでエラーでる
                // UIスレッド内で処理をしないといけないらしい
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mCount++;
//                        showText("カウント:" + mCount);

                        usagestatsmanager();

//                        setToast();
//                        showText(result);

                        if(windowShowed==true) {
                            providerCheck();
                        }



                        if (isLocked==true) {

                            if (isLocationApp == true && windowShowed==true) {
                                warningDialog();
                            } else if (isPackage == true) {
                                appPlayDialog();
                            }
                        }

                        isPackage = false;
                        isLocationApp = false;
//                        locationInto = false;
                        isLocked = false;

                    }
                });
            }

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    showText("スレッド終了");
                }
            });
        }
    };
    private Thread mThread;


    public void onActivityResult(int req, int result, Intent it) {

    }


    @TargetApi(21)
    private void usagestatsmanager() {
        // UsageStats,UsageStatsManager : デバイスの使用履歴、統計情報を扱う
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        int intTime = UsageStatsManager.INTERVAL_BEST;
        Long end = System.currentTimeMillis(); // 現在の時間を取得 (ミリ秒)
//        Long start = end - (1 * 24 * 60 * 60 * 1000); // 現在時刻から1日前の時間(ミリ秒)を引く　1日 = 24*60*60*1000ミリ秒
        Long start = end - (5 * 60 * 1000); // 5分
        List<UsageStats> usl = usm.queryUsageStats(intTime, start, end); //

        List<AppData> dataList = new ArrayList<AppData>();

        // AppData読み込み
        try {
            //FileInputStream inFile = new FileInputStream(FILE_NAME);
            FileInputStream inFile = openFileInput("appData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            dataList = (ArrayList<AppData>) inObject.readObject();
            inObject.close();
            inFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < usl.size(); i++) {
            UsageStats us = usl.get(i);

            // 使用時間が0でない
            if (us.getTotalTimeInForeground() != 0) {

                if (us.getLastTimeUsed() > end - 4000) {

                    for (AppData info : dataList) {
                        // インストール済みアプリであるか
                        if (info.getpackageName().equals(us.getPackageName())) {

                            // 位置情報アプリか
                            if(info.getLocationPermission().equals("0")){
                                isLocationApp=true; //位置情報アプリだ
                            }

                            // 制限されているか
                            if (info.getLock() == true) {
                                isLocked = true;   //制限されたアプリだ
                            }

                            isPackage = true; //アプリが使用された
//                            text = "パッケージ名:" + String.valueOf(info.getpackageLabel());
                                sendPackageLabel = String.valueOf(info.getpackageLabel());
                                sendPermission = info.getPermission();
                                appUsedCount++;

                        }
                    }
                }
            }
        }
    }


    public void providerCheck(){
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        String provider = "";
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //GPSが利用可能
//            provider = "gps";

        } else if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //ネットワークが利用可能
//            provider = "network";
        } else {
            //位置情報の利用不可能
//            provider = "使えません";

            stopService(new Intent(getBaseContext(), WindowService.class));
            windowShowed=false;


        }
//        Toast.makeText(this, ""+provider, Toast.LENGTH_SHORT).show();
    }

    private void appPlayDialog() {
        Intent intent = new Intent(this, CallDialogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);//新規起動の記述

        intent.putExtra("LABEL", sendPackageLabel);
        intent.putExtra("PERMISSION", sendPermission);

        startActivity(intent);
    }
    private void warningDialog() {
        Intent intent = new Intent(this, WarningDialogActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);//新規起動の記述
            startActivity(intent);
    }


    private void showText(Context ctx, final String text) {
        Toast.makeText(this, TAG2 + text, Toast.LENGTH_SHORT).show();
    }

    private void showText(final String text) {
        if (isPackage == true) {
            showText(this, text);
        }
        isPackage = false;
    }

    @Override
    public void onCreate() {
//        Toast.makeText(this, "バックグラウンドサービスを開始しました。", Toast.LENGTH_SHORT).show();
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

        // 位置情報サービスの利用
        if (mLocationManager != null) {
            Log.d("LocationActivity", "locationManager.requestLocationUpdates");
            // バックグラウンドから戻ってしまうと例外が発生する場合がある
            try {
                // minTime = 1000msec, minDistance = 50m
                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
//                    return;
                }
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, this);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "例外が発生、位置情報のPermissionを許可していますか？", Toast.LENGTH_SHORT).show();
            }

//            startService(new Intent(getBaseContext(), WindowService.class));
        }





        this.mThread = new Thread(null, mTask, "NortifyingService");
        this.mThread.start();



        //明示的にサービスの起動、停止が決められる場合の返り値
        return START_STICKY;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "バックグラウンドサービスを終了します。", Toast.LENGTH_SHORT).show();
        stopService(new Intent(getBaseContext(), WindowService.class));
        mLocationManager.removeUpdates(this);
        Log.i(TAG, "onDestroy");

        // スレッド停止
        this.mThread.interrupt();
        this.mThreadActive = false;

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

//        Toast.makeText(this, "登録" + confLatitude + "," + confLongitude
//                + "\n現在" + myLatitude + "," + myLongitude, Toast.LENGTH_LONG).show();

        float results2 = getDistanceBetween(confLatitude, confLongitude, myLatitude, myLongitude);

        // 3km 以内
        if (results2 < 3000) {
            message = "範囲内";
//            locationInto=true; // 範囲内だ
            if(windowShowed==false) {
                startService(new Intent(getBaseContext(), WindowService.class));
            }
            windowShowed=true;


//            Intent intent = new Intent(this, WarningDialogActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);//新規起動の記述
//            startActivity(intent);

        } else {
            message = "範囲外";
//            locationInto=false; // 範囲外だ

            stopService(new Intent(getBaseContext(), WindowService.class));
            windowShowed=false;
        }
//        Toast.makeText(this, "距離" + results2 + "m" + message, Toast.LENGTH_LONG).show();
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