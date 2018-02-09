package com.c0114573.broadcastapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by C011457331 on 2017/04/19.
 * 位置情報漏洩防止サービス
 * アプリ起動判定を行い,設定状況からそれに応じてアプリ起動ダイアログまたは制限ダイアログを表示する
 * 位置情報判定を行い範囲内であれば範囲内ダイアログを表示する
 */

public class ExampleService extends Service implements LocationListener {

    static final String TAG = "ExampleService";
    private LocationManager mLocationManager;
    NotificationManager nm;

//    double[] confLatitude = new double[10];    // 登録緯度
//    double[] confLongitude = new double[10];   // 登録経度
//    int[] confDistance = new int[10];

    double myLatitude = 0;    // 現在の緯度
    double myLongitude = 0;   // 現在の経度

    String PackageName = "";
    String PackageLabel = "";
    String Permission = "";
    int appWidgetId = 0;

    boolean isPackage = false;      // 起動したアプリが存在する

    boolean isLocked = false;       // 使用制限を持ったアプリである
    boolean isInRange = false;      // 範囲内に入っている
    boolean isLocationUsed = false;     // 位置情報が利用できる
//    boolean isLocationApp = false;  // 位置情報権限を持ったアプリである
    boolean isCameraorSMSApp = false;

    boolean isNotPermission = false;    // 権限を持っていないアプリである
    boolean isUseCount = false;     // 5回以上使用されたアプリである

    boolean windowShowed = false;   // 範囲内Windowが表示されている

    boolean isUsed = false;
    boolean isShowWarning =true;   // warningdialogが出せる状態か

    int appUsedCount = 0;

    private final static String TAG2 = "";

    // Toastを表示させるために使うハンドラ
    private Handler mHandler = new Handler();
    // スレッドを停止するために必要
    private boolean mThreadActive = true;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "□□□□□□□□□□□□□");
        Log.i(TAG, "onStartCommand Received start id " + startId + ": " + intent);
        Log.i(TAG, "onStartCommand Received start isRange:" + isInRange + "isPackage:" + isPackage);

        //ノーティフィケーションの表示
        showNotification(this, "位置情報漏洩防止サービス", "稼働中");

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // 位置情報サービスの利用
        if (mLocationManager != null) {
            Log.d(TAG, "locationManager.requestLocationUpdates");
            // バックグラウンドから戻ってしまうと例外が発生する場合がある
            try {
                // minTime = 1000msec, minDistance = 50m
                // このアプリが位置情報権限を持っていない
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                } else {
                    // このアプリが位置情報権限を持っている
                    // 通知のための最小時間間隔(ミリ秒),通知のための最小距離間隔(メートル)
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, this);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "例外が発生、位置情報のPermissionを許可していますか？", Toast.LENGTH_SHORT).show();
            }
        }
        // スレッドの開始
        this.mThread = new Thread(null, mTask, "NortifyingService");
        this.mThread.start();
        isShowWarning=true;
        isPackage = false;

        // 位置情報と現在範囲内にいるかのデータを取得
//        SharedPreferences data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
//        myLatitude = Double.longBitsToDouble(data.getLong("myLatitude", 0l));
//        myLongitude = Double.longBitsToDouble(data.getLong("myLongitude", 0l));
//        isInRange = data.getBoolean("isRange", false);

//        Log.i(TAG, "取得後,myLatitude:" + myLatitude + ",myLongitude:" + myLongitude + ",isRange:" + isInRange);

//        if (isInRange) {
//            windowShowed = true;
//            Log.i(TAG, "前回範囲内に入っていた");
//            startService(new Intent(getBaseContext(), WindowService.class));
//        }

        //明示的にサービスの起動、停止が決められる場合の返り値
        return START_STICKY_COMPATIBILITY;
    }


    // スレッド処理
    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            // アクティブな間だけ処理をする
            while (mThreadActive) {
                try {
                    Thread.sleep(3000); //3秒待つ
                } catch (InterruptedException e) {
                    // TODO 自動生成された catch ブロック
                    e.printStackTrace();
                }

                // ハンドラーをはさまないとToastでエラーでる
                // UIスレッド内で処理をしないといけない
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        // GPSの状態取得
                        providerCheck();

                        // 起動したアプリがあるか
                        startAppCheck();

                        // ダイアログ表示
                        if (isPackage) {
                            showDialog();
                        }

                        isPackage = false;
//                        isLocationApp = false;
                        isCameraorSMSApp = false;
                        isLocked = false;
                        isUseCount = false;
                        isNotPermission = false;
                        isUsed = false;
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

    //    @TargetApi(21)
    private void startAppCheck() {
        // UsageStats,UsageStatsManager : デバイスの使用履歴、統計情報を扱う
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        int intTime = UsageStatsManager.INTERVAL_BEST;

        Long end = System.currentTimeMillis(); // 現在の時間を取得 (ミリ秒)
//        Long start = end - (1 * 24 * 60 * 60 * 1000); // 現在時刻から1日前の時間(ミリ秒)を引く　1日 = 24*60*60*1000ミリ秒
        Long start = end - (5 * 60 * 1000); // 5分

        // 5分前から現在までの使用履歴をリストで取得
        List<UsageStats> uslData = usm.queryUsageStats(intTime, start, end);

        List<AppData> appData = new ArrayList<AppData>();
        // AppData読み込み (デシリアライズ)
        try {
            FileInputStream inFile = openFileInput("appData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            appData = (ArrayList<AppData>) inObject.readObject();
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

        for (int i = 0; i < uslData.size(); i++) {
            UsageStats us = uslData.get(i);
            // 使用時間が0でない,3秒前に使用されたアプリか
            if (us.getTotalTimeInForeground() != 0 && us.getLastTimeUsed() > end - 3000) {

                for (AppData ad : appData) {
                    // 使用されたアプリがAppDataリストに存在しているか
                    if (ad.getpackageName().equals(us.getPackageName())) {

                        isPackage = true; // 起動したアプリが存在した
                        PackageName = String.valueOf(ad.getpackageName());
                        PackageLabel = String.valueOf(ad.getpackageLabel());
                        Permission = ad.getPermission();
                        appWidgetId = ad.getpackageID();
                        isUsed = ad.isUsed;

                        // 権限を持っていないアプリか
                        if (ad.getIsNotPermission()) {
                            isNotPermission = true; //権限を持っていないアプリ
                            // 5回以上か
                            if (ad.useCount > 4) {
                                isUseCount = true;
                            }
                        }
                        appUsedCount++;

                        // 位置情報権限を持っているアプリか
//                        if (ad.pLocation == 0) {
//                            isLocationApp = true; //位置情報アプリだ
//                        }

                        // カメラまたはSMS権限を持っているか
                        if(ad.pCamera == 0 || ad.pSMS == 0){
                            isCameraorSMSApp = true;
                        }

                        // 制限されているアプリか
                        isLocked = ad.lock;  //制限されたアプリだ
                    }
                }
            }
        }

        // windowServiceを終了するかどうか
        // 範囲内であるか
        if (windowShowed) {
            // 現在位置情報の利用状況取得
//            providerCheck();
            // 現在位置情報の利用不可であるか
            if (!isLocationUsed) {
                comparisonDistance();
//                stopService(new Intent(getBaseContext(), WindowService.class));
            }
        }

    }


    public void showDialog() {
        Log.i("!!showDialog!!","isInRange"+isInRange+",isLocked"+isLocked+",isUsed"+isUsed);
//        Log.i("!!showDialog!!","isLocationApp"+isLocationApp+",isLocationUsed"+isLocationUsed);
        Log.i("!!showDialog!!","isCameraorSMSApp"+isCameraorSMSApp+",isLocationUsed"+isLocationUsed);

        // 権限を持っていない場合
        if (isNotPermission) {
            // 権限チェックを行う
            permissionCheck(PackageName);
            // 5回以上使われていた
            if (isUseCount) {
                Intent startServiceIntent = new Intent(ExampleService.this, AppDataSetting.class);
                startServiceIntent.putExtra("UNINSTALL", 30);
                startServiceIntent.putExtra("APPNAME", PackageName);
                startService(startServiceIntent);
            }

        }

        // 範囲内,制限されているか
        if (isInRange && isLocked && isLocationUsed) {
            // 制限ダイアログ表示
//            if(isShowWarning) {
                warningDialog();
//            }
        // 範囲外,制限されているか
        } else if (isLocked && !isUsed) {
            // GPSの状態取得
//            providerCheck();
            // 位置情報権限を許可しているアプリで,現在位置取得を行っていない場合
            // (地図アプリなどを現在位置取得しないで使う場合)ダイアログ非表示

            // そうでない場合
            // (位置情報の利用が可の場合,またはカメラやSMSのアプリなどの場合)ダイアログ表示
//            if (!(isLocationApp && !isLocationUsed)) {
            if (isCameraorSMSApp || isLocationUsed) {
                startAppDialog();
            }
        }

    }


    public void providerCheck() {
        // 位置情報が利用できるか
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = "";
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPSが利用可能
            provider = "gps";
            isLocationUsed = true;
        } else if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // ネットワークが利用可能
            provider = "network";
            isLocationUsed = true;
        } else {
            // 位置情報の利用不可能
            provider = "使えません";
            isLocationUsed = false;
            windowShowed = false;
            stopService(new Intent(getBaseContext(), WindowService.class));
        }
//        Log.i(TAG, provider);
    }

    public void permissionCheck(String pName) {
        // 権限があるか
        // パーミッションを持っているか
        int pSMS = -1;
        int pLocation = -1;

        int pNetwork = getPackageManager().checkPermission(Manifest.permission.INTERNET, pName);
        int pCamera = getPackageManager().checkPermission(Manifest.permission.CAMERA, pName);
        int pSMS1 = getPackageManager().checkPermission(Manifest.permission.RECEIVE_SMS, pName);
        int pSMS2 = getPackageManager().checkPermission(Manifest.permission.SEND_SMS, pName);
        int pSMS3 = getPackageManager().checkPermission(Manifest.permission.READ_SMS, pName);
        int pSMS4 = getPackageManager().checkPermission(Manifest.permission.RECEIVE_WAP_PUSH, pName);
        int pSMS5 = getPackageManager().checkPermission(Manifest.permission.RECEIVE_MMS, pName);
        int pLocation1 = getPackageManager().checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, pName);
        int pLocation2 = getPackageManager().checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, pName);

        if (pSMS1 == 0 || pSMS2 == 0 || pSMS3 == 0 || pSMS4 == 0 || pSMS5 == 0) pSMS = 0;

        if (pLocation1 == 0 || pLocation2 == 0) pLocation = 0;

        try {
            // 読み込み
            FileInputStream inFile = openFileInput("appData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            List<AppData> dataList2 = (ArrayList<AppData>) inObject.readObject();
            inObject.close();
            inFile.close();

            for (AppData appData : dataList2) {
                if (appData.getpackageName().equals(pName)) {
                    appData.pNetwork = pNetwork;
                    appData.pCamera = pCamera;
                    appData.pSMS = pSMS;
                    appData.pLocation = pLocation;
                    appData.setUseCountPlus();
                }
            }
            // ファイル保存
            FileOutputStream outFile = openFileOutput("appData.file", Context.MODE_PRIVATE);
            ObjectOutputStream outObject = new ObjectOutputStream(outFile);
            outObject.writeObject(dataList2);
            outObject.close();
            outFile.close();

        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startAppDialog() {
        // isUsedの切り替え
        Intent startServiceIntent = new Intent(getBaseContext(), AppDataSetting.class);
        startServiceIntent.putExtra("UPDATE", 40);
        startServiceIntent.putExtra("APPNAME", PackageName);
        startService(startServiceIntent);

        // 指定時間後にisUsedを元に戻すための処理
        Intent i = new Intent(getApplicationContext(), ReceivedActivity.class);
        i.putExtra("APPNAME", PackageName);
        // ブロードキャストを投げるPendingIntentの作成
        PendingIntent sender = PendingIntent.getBroadcast(ExampleService.this, appWidgetId, i, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis()); // 現在時刻を取得
        calendar.add(Calendar.SECOND, 30); // 現時刻より15秒後を設定

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);

        // ダイアログアクティビティに遷移
        Intent intent = new Intent(this, DialogStartAppActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);//新規起動の記述
        intent.putExtra("LABEL", PackageLabel);
        intent.putExtra("PERMISSION", Permission);
        startActivity(intent);

    }

    private void warningDialog() {
        isShowWarning = false;
        Intent intent = new Intent(this, DialogWarningActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("LABEL", PackageLabel);
        intent.putExtra("LABEL", PackageName);
        startActivity(intent);
    }

    private void locationDialog() {
        Intent intent = new Intent(this, DialogInRangeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
    }

    private void showText(Context ctx, final String text) {
        Toast.makeText(this, TAG2 + text, Toast.LENGTH_SHORT).show();
    }

    private void showText(final String text) {
        showText(this, text);
    }

    //ノーティフィケーションの表示
    private void showNotification(Context context,
                                  String title, String text) {
        //ノーティフィケーションオブジェクトの生成(9)
        Notification.Builder builder = new Notification.Builder(context);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        startForeground(10, builder.build());

        //ペンディングインテントの指定(10)
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("net.npaka.serviceex",
                "net.npaka.serviceex.ServiceEx"));
        intent.removeCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        builder.setContentIntent(PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT));

        //ノーティフィケーションの表示(11)
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(1);
        nm.notify(1, builder.build());
    }

    //
//    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//
//    Intent notificationIntent = new Intent(this, MainActivity.class);
//    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//
//    Notification notification = new NotificationCompat.Builder(this)
//            .setSmallIcon(R.drawable.ic_launcher) // アイコン
//            .setTicker("Hello") // 通知バーに表示する簡易メッセージ
//            .setWhen(System.currentTimeMillis()) // 時間
//            .setContentTitle("My notification") // 展開メッセージのタイトル
//            .setContentText("Hello Notification!!") // 展開メッセージの詳細メッセージ
//            .setContentIntent(contentIntent) // PendingIntent
//            .build();
//
//        notificationManager.notify(1, notification);


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        Toast.makeText(this, "バックグラウンドサービスを終了します。", Toast.LENGTH_SHORT).show();
        stopService(new Intent(getBaseContext(), WindowService.class));
        mLocationManager.removeUpdates(this);

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
        Log.d(TAG, "Called_onLocationChanged");
        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();

        comparisonDistance();

        // 判定結果の保存
//        SharedPreferences data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = data.edit();
//        editor.putLong("myLatitude", Double.doubleToRawLongBits(myLatitude));
//        editor.putLong("myLongitude", Double.doubleToRawLongBits(myLongitude));
//        editor.putBoolean("isRange", isInRange);
//        editor.apply();
//        Log.i(TAG, "保存後,myLatitude:" + myLatitude + ",myLongitude:" + myLongitude + ",isRange:" + isInRange);

    }

    public void comparisonDistance() {
        // 範囲内判定
        double[] confLatitude = new double[10];    // 登録緯度
        double[] confLongitude = new double[10];   // 登録経度
        int[] confDistance = new int[10];

        try {
            FileInputStream inFile = openFileInput("locationData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            List<LocationData> locationList2 = (ArrayList<LocationData>) inObject.readObject();
            inObject.close();
            inFile.close();

            int i = 0;
            for (LocationData locationData : locationList2) {
                if (locationData.valid) {
                    confLatitude[i] = locationData.lat;
                    confLongitude[i] = locationData.lng;
                    confDistance[i] = locationData.distance * 1000;
                    i++;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("登録されてないa");
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("登録されてないc");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < confLatitude.length; i++) {
            float results = getDistanceBetween(confLatitude[i], confLongitude[i], myLatitude, myLongitude);
            // 指定範囲内
            if (results < confDistance[i]) {
                Log.i("AAAaa","範囲内" + myLatitude);
                isInRange = true;
                if (!windowShowed && isLocationUsed) {
                    windowShowed = true;
                    startService(new Intent(getBaseContext(), WindowService.class));
                    locationDialog();
                }
                return;
            } else {
                Log.i(TAG, "範囲外" + myLatitude);
                isInRange = false;
                if (windowShowed) {
                    windowShowed = false;
                    stopService(new Intent(getBaseContext(), WindowService.class));
                }
            }
        }
    }


    // 距離判定
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