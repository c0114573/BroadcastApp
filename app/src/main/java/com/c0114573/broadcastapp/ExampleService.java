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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

public class ExampleService extends Service implements LocationListener {

    static final String TAG = "ExampleService";
    private LocationManager mLocationManager;
    NotificationManager nm;

    double[] confLatitude = new double[10];    // 設定緯度
    double[] confLongitude = new double[10];   // 設定経度
    int[] confDistance = new int[10];

    double myLatitude = 0;    // 現在の緯度
    double myLongitude = 0;   // 現在の経度

    String targetStr = "";    // 緯度経度を持ってくる
    String message = "";

    String PackageName = "";
    String PackageLabel = "";
    String Permission = "";
    int appWidgetId = 0;

    boolean isPackage = false;      // 起動したアプリが存在する
    boolean isLocked = false;       // 使用制限を持ったアプリである
    boolean isNotPermission = false;    // 権限を持っていないアプリである
    boolean isUseCount = false;     // 5回以上使用されたアプリである
    boolean isLocationApp = false;  // 位置情報権限を持ったアプリである

    boolean isRange = false;        // 範囲内に入っている
    boolean windowShowed = false;   // 範囲内Windowが表示されている

    boolean isUsed = false;

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
                        mCount++;
//                        showText("カウント:" + mCount);
                        // 登録位置情報取得
//                        reqestGPS();

                        // 起動検知
                        usageStatsInfo();

//                        isPackage = false;
                        isLocationApp = false;
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

    @TargetApi(21)
    private void usageStatsInfo() {
        // UsageStats,UsageStatsManager : デバイスの使用履歴、統計情報を扱う
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        int intTime = UsageStatsManager.INTERVAL_BEST;

        Long end = System.currentTimeMillis(); // 現在の時間を取得 (ミリ秒)
//        Long start = end - (1 * 24 * 60 * 60 * 1000); // 現在時刻から1日前の時間(ミリ秒)を引く　1日 = 24*60*60*1000ミリ秒
        Long start = end - (5 * 60 * 1000); // 5分

        // 5分前から現在までの使用履歴をリストで取得
        List<UsageStats> usl = usm.queryUsageStats(intTime, start, end);

        List<AppData> dataList = new ArrayList<AppData>();
        // AppData読み込み (デシリアライズ)
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
                // 3秒前に使用されたアプリ
                if (us.getLastTimeUsed() > end - 3000) {
                    for (AppData info : dataList) {
                        // 使用されたアプリがAppDataリストに存在しているか
                        if (info.getpackageName().equals(us.getPackageName())) {
                            // 権限を持ってない場合
                            if (info.getIsNotPermission()) {
                                isNotPermission = true; //権限を持っていないアプリ
                                // 5回以上か
                                if (info.useCount > 4) {
                                    isUseCount = true;
                                }
                            }
                            // 位置情報アプリか
                            if (info.getLocationPermission().equals("0")) {
                                isLocationApp = true; //位置情報アプリだ
                            }
                            // 制限されているか
                            if (info.getLock() == true) {
                                isLocked = true;   //制限されたアプリだ
                            }

                            isPackage = true; // 起動したアプリが存在した
//                            text = "パッケージ名:" + String.valueOf(info.getpackageLabel());
                            PackageLabel = String.valueOf(info.getpackageLabel());
                            PackageName = String.valueOf(info.getpackageName());
                            Permission = info.getPermission();
                            appWidgetId = info.getpackageID();
                            isUsed = info.isUsed;
                            appUsedCount++;
                        }
                    }
                }
            }
        }

        // windowServiceを終了するかどうか
        if (windowShowed) {
            if (providerCheck()) {
            } else {
                stopService(new Intent(getBaseContext(), WindowService.class));
            }
        }

        // 権限を持っていない場合
        if (isPackage && isNotPermission) {
            // 権限チェックを行う
            permissionCheck(PackageName);
            // 5回以上使われていた
            if (isUseCount) {
                Intent startServiceIntent = new Intent(ExampleService.this, AppDataSetting.class);
                startServiceIntent.putExtra("UNINSTALL", 30);
                startServiceIntent.putExtra("APPNAME", PackageName);
                startService(startServiceIntent);
            }

        } else if (isLocked) {
            // 範囲内で位置情報権限を持つアプリが起動した
            if (isPackage && isLocationApp && isRange && (!isUsed)) {
                // バックグラウンドプロセスの終了
                ActivityManager activityManager = ((ActivityManager) getSystemService(ACTIVITY_SERVICE));
                for (int j = 0; j < dataList.size(); j++) {
                    if (!dataList.get(j).getLock())
                        activityManager.killBackgroundProcesses(dataList.get(j).packageName);
                }

                warningDialog();

             // 位置情報漏洩リストのアプリが起動した
            } else if (isPackage && (!isUsed)) {
                Log.i(TAG, "isRange:" + isRange + ",isLocationApp:" + isLocationApp);

                appPlayDialog();
            }
        }
    }

    public boolean providerCheck() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = "";
        boolean isPrvoider = true;
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPSが利用可能
//            provider = "gps";

        } else if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // ネットワークが利用可能
//            provider = "network";
        } else {
            // 位置情報の利用不可能
            provider = "使えません";
            isPrvoider = false;
            stopService(new Intent(getBaseContext(), WindowService.class));
        }
//        Toast.makeText(this, ""+provider, Toast.LENGTH_SHORT).show();
        Log.i(TAG, provider);
        return isPrvoider;
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

    private void appPlayDialog() {
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
        calendar.add(Calendar.SECOND, 15); // 現時刻より15秒後を設定

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);

        // ダイアログアクティビティに遷移
        Intent intent = new Intent(this, CallDialogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);//新規起動の記述
        intent.putExtra("LABEL", PackageLabel);
        intent.putExtra("PERMISSION", Permission);
        startActivity(intent);

    }

    private void warningDialog() {
        Intent intent = new Intent(this, WarningDialogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("LABEL", PackageLabel);
        intent.putExtra("LABEL", PackageName);
        startActivity(intent);

    }

    private void locationDialog() {
        Intent intent = new Intent(this, LocationDialogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
    }

    private void showText(Context ctx, final String text) {
        Toast.makeText(this, TAG2 + text, Toast.LENGTH_SHORT).show();
    }

    private void showText(final String text) {
//        if (isPackage == true) {
        showText(this, text);
//        }
        isPackage = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "onStartCommand Received start id " + startId + ": " + intent);
        Log.i(TAG, "onStartCommand Received start isRange:" +isRange + "isPackage:"+isPackage);
        Log.i(TAG, "□□□□□□□□□□□□□");
        Log.i(TAG, "□□□□□□□□□□□□□");

        //ノーティフィケーションの表示
        showNotification(this,
                "自作サービス",
                "自作サービスを操作します");

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
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, this);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "例外が発生、位置情報のPermissionを許可していますか？", Toast.LENGTH_SHORT).show();
            }
        }
        this.mThread = new Thread(null, mTask, "NortifyingService");
        this.mThread.start();

        isPackage = false;

        // TODO 位置情報と現在範囲内にいるかのデータを取得
        SharedPreferences data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
        myLatitude = Double.longBitsToDouble(data.getLong("myLatitude", 0l));
        myLongitude = Double.longBitsToDouble(data.getLong("myLongitude", 0l));
        isRange = data.getBoolean("isRange", false);

        Log.i(TAG, "取得後,myLatitude:" + myLatitude + ",myLongitude:" + myLongitude + ",isRange:" + isRange);

        if(isRange){
            windowShowed=true;
            startService(new Intent(getBaseContext(), WindowService.class));
        }

        //明示的にサービスの起動、停止が決められる場合の返り値
        return START_STICKY_COMPATIBILITY;
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
//        Toast.makeText(this, "登録" + confLatitude + "," + confLongitude
//                + "\n現在" + myLatitude + "," + myLongitude, Toast.LENGTH_LONG).show();


        comparisonDistance();

        /*
        // 範囲内判定
        try {
            FileInputStream inFile = openFileInput("locationData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            List<LocationData> locationList2 = (ArrayList<LocationData>) inObject.readObject();
            inObject.close();
            inFile.close();

            int i=0;
            for (LocationData locationData : locationList2) {
                confLatitude[i] = locationData.lat;
                confLongitude[i] = locationData.lng;
                confDistance[i] = locationData.distance * 1000;
                i++;
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

        for (int i = 0; i < confLatitude.length ; i++) {
            float results2 = getDistanceBetween(confLatitude[i], confLongitude[i], myLatitude, myLongitude);
            // 指定範囲内
            if (results2 < confDistance[i]) {
//                message = "範囲内";
                windowShowed = true;
                return;
            } else {
                Log.i(TAG,"範囲外"+ myLatitude);
//                message = "範囲外";
                windowShowed = false;
            }
        }
        */


        // 判定結果の保存
        SharedPreferences data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        editor.putLong("myLatitude", Double.doubleToRawLongBits(myLatitude));
        editor.putLong("myLongitude", Double.doubleToRawLongBits(myLongitude));
        editor.putBoolean("isRange", isRange);
        editor.apply();

        Log.i(TAG, "保存後,myLatitude:" + myLatitude + ",myLongitude:" + myLongitude + ",isRange:" + isRange);


//        if (windowShowed) {
//            startService(new Intent(getBaseContext(), WindowService.class));
//            locationDialog();
//
//        }else {
//            stopService(new Intent(getBaseContext(), WindowService.class));
//        }

        Log.i(TAG, "Location_Info" + isRange);

    }

    public void comparisonDistance() {
        // 範囲内判定
        try {
            FileInputStream inFile = openFileInput("locationData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            List<LocationData> locationList2 = (ArrayList<LocationData>) inObject.readObject();
            inObject.close();
            inFile.close();

            int i = 0;
            for (LocationData locationData : locationList2) {
                confLatitude[i] = locationData.lat;
                confLongitude[i] = locationData.lng;
                confDistance[i] = locationData.distance * 1000;
                i++;
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
            float results2 = getDistanceBetween(confLatitude[i], confLongitude[i], myLatitude, myLongitude);
            // 指定範囲内
            if (results2 < confDistance[i]) {
//                message = "範囲内";
                isRange = true;

                if(!windowShowed){
                    windowShowed=true;
                    startService(new Intent(getBaseContext(), WindowService.class));
                    locationDialog();
                }

                return;
            } else {
                Log.i(TAG, "範囲外" + myLatitude);
//                message = "範囲外";
                isRange = false;

                if(windowShowed){
                    windowShowed=false;
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