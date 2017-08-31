package com.c0114573.broadcastapp;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.AppLaunchChecker;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by member on 2017/08/02.
 */

public class AppDataSetting extends Service {

    // リスト作成
    // 起動不可アプリ名リスト
    ArrayList<String> appList = new ArrayList<String>();
    // 端末内特定アプリ情報リスト
    List<AppData> dataListNew = new ArrayList<AppData>();
    // 読み込み特定アプリ情報リスト
    List<AppData> dataListLoad = new ArrayList<AppData>();

    // 新規追加特定アプリ
    List<AppData> dataListAdd = new ArrayList<AppData>();

    boolean appNew = true;

//    @Override
//    protected void onCreate( {
//        super.onCreate(savedInstanceState);
//
//        NewAppList();
//
//        /*
//        if(){
//           LoadAppList();
//        }
//        */
//
//        SaveAppList();
//
////        finish();
//    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NewAppList();

        if (intent != null && 10 == intent.getIntExtra("INSTALL",0)){
            LoadAppList();

            for (AppData info : dataListNew) {
                    if(!(dataListLoad.contains(info))){
                        appNew=false;

                        AppData data = new AppData();
                        data.packageLabel = info.getpackageLabel();
                        data.packageName = info.getpackageName();
                        data.icon = info.getIcon();
                        String[] perm =info.getPermission().split(",", 0);
                        data.pCamera = Integer.parseInt(perm[0]);
                        data.pSMS = Integer.parseInt(perm[1]);
                        data.pLocation = Integer.parseInt(perm[2]);
                        data.setLock(true);

                        dataListLoad.add(data);



                    }
            }


        }
        /*
        if(){
           LoadAppList();
           hgfrtttyud
        }
        */

        SaveAppList();

        stopSelf();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "読み込み終わり", Toast.LENGTH_SHORT).show();
    }

    // 特定アプリ一覧を作成
    public void NewAppList() {
        // パッケージマネージャーの作成
        PackageManager packageManager = getPackageManager();

        // 起動不可能なアプリをリストに格納
        List<PackageInfo> pckInfoList = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        for (PackageInfo pckInfo : pckInfoList) {
            // インテントで起動できないものを格納
            if (packageManager.getLaunchIntentForPackage(pckInfo.packageName) == null) {
                appList.add(pckInfo.packageName);
            }
        }

        // インストール済みアプリの情報を取得
        List<ApplicationInfo> applicationInfo = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        int pSMS = -1;
        int pLocation = -1;

        // リストにアプリデータを格納
        appInfo:
        for (ApplicationInfo info : applicationInfo) {
            pSMS = -1;
            pLocation = -1;

            // 自分自身を除外
            if (info.packageName.equals(this.getPackageName())) continue;

            // 安全なアプリを除外
            if (info.packageName.equals("klb.android.lovelive")) continue;

            // 起動不可能なアプリを除外
            for (String app : appList) {
                if ((info.packageName.equals(app))) continue appInfo;
            }

            // パーミッションを持っているか
            int pCamera = getPackageManager().checkPermission(Manifest.permission.CAMERA, info.packageName);
            int pSMS1 = getPackageManager().checkPermission(Manifest.permission.RECEIVE_SMS, info.packageName);
            int pSMS2 = getPackageManager().checkPermission(Manifest.permission.SEND_SMS, info.packageName);
            int pSMS3 = getPackageManager().checkPermission(Manifest.permission.READ_SMS, info.packageName);
            int pSMS4 = getPackageManager().checkPermission(Manifest.permission.RECEIVE_WAP_PUSH, info.packageName);
            int pSMS5 = getPackageManager().checkPermission(Manifest.permission.RECEIVE_MMS, info.packageName);
            int pLocation1 = getPackageManager().checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, info.packageName);
            int pLocation2 = getPackageManager().checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, info.packageName);

            if (pSMS1 == 0 || pSMS2 == 0 || pSMS3 == 0 || pSMS4 == 0 || pSMS5 == 0) pSMS = 0;

            if (pLocation1 == 0 || pLocation2 == 0) pLocation = 0;

            // カメラ,SNS,位置権限を持ったアプリであるか
            if (pCamera == PackageManager.PERMISSION_GRANTED ||
                    pLocation == PackageManager.PERMISSION_GRANTED ||
                    pSMS == PackageManager.PERMISSION_GRANTED) {

                // アプリ情報クラスにアプリ情報を追加
                AppData data = new AppData();
                data.packageLabel = info.loadLabel(packageManager).toString();
                data.packageName = info.packageName;
                data.icon = info.loadIcon(packageManager);
                data.pCamera = pCamera;
                data.pSMS = pSMS;
                data.pLocation = pLocation;
                data.setLock(true);

                // アプリ情報クラスをリストに追加
                dataListNew.add(data);
            }
        }

        // アイコン情報を保存
        Drawable icon ;
        int num = 0;
        for (AppData info : dataListNew) {
            num++;
            icon = info.getIcon();
            // ビットマップに変換
            Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
            // バイト配列出力を扱う
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // ビットマップを圧縮する
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            // 文字列型に直す
            String bitmapStr = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            // SharedPreferenceのインスタンスを生成
            SharedPreferences pref = getSharedPreferences("DATA" + num, Context.MODE_PRIVATE);
            // データの書き込み
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("ICON", bitmapStr);
            editor.apply();
        }

    }

    // 特定アプリを取得
    public void LoadAppList() {
        // AppData読み込み
        try {
            //FileInputStream inFile = new FileInputStream(FILE_NAME);
            FileInputStream inFile = openFileInput("appData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            dataListLoad = (ArrayList<AppData>) inObject.readObject();
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

    }

    // 特定アプリを保存/更新
    public void SaveAppList() {
        // シリアライズしてAppDataをファイルに保存
        try {
            //FileOutputStream outFile = new FileOutputStream(FILE_NAME);
            FileOutputStream outFile = openFileOutput("appData.file", 0);
            ObjectOutputStream outObject = new ObjectOutputStream(outFile);
            if(appNew==true) {
                outObject.writeObject(dataListNew);
            }else {
                outObject.writeObject(dataListLoad);
            }
            outObject.close();
            outFile.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
