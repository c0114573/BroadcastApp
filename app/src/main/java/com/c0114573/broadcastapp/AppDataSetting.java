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
import android.graphics.BitmapFactory;
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

        // get〇〇Extra  キーがないとき第二引数の値となる
        if (intent != null && intent.getIntExtra("AppFirstStart",0) == 10){
//            LoadAppList();
            NewAppList(); // 特定アプリ一覧を作成
            SaveAppList(); // 特定アプリを保存/更新
        }

        else if (intent != null && intent.getIntExtra("INSTALL",0) == 20){
            LoadAppList();

        }
        else  if(intent != null && intent.getIntExtra("UNINSTALL",0) == 30){
            String appname = intent.getStringExtra("APPNAME");
            DeleteAppList(appname);
        }

        // サービスの終了
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

    // インストール時
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

    // アンインストール時
    public void DeleteAppList(String deleteApp) {
        try {
            // デシリアライズ(読み込み)
            FileInputStream inFile = openFileInput("appData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            List<AppData> dataList2 = (ArrayList<AppData>) inObject.readObject();
            inObject.close();
            inFile.close();

            int appcnt = 0;
            int appnum = 0;
            int appnum2 = 0;
            boolean found = false;
//            deleteApp = "com.nianticlabs.pokemongo";

            for (AppData appData : dataList2) {
                appcnt++;
                // 削除したいアプリのリスト番号を取得
                if (appData.getpackageName().equals(deleteApp)) {
                    appnum = appcnt;
                    appnum2 = appcnt;
                    found=true; //見つかった
                }
            }

            if(found==true) {

                int num = 0;
                // アイコン情報を読み取り
                for (AppData appData : dataList2) {
                    num++;
                    // SharedPreferenceのインスタンスを生成
                    SharedPreferences pref = getSharedPreferences("DATA" + num, Context.MODE_PRIVATE);
                    String s = pref.getString("ICON", "");
                    if (!s.equals("")) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        byte[] b = Base64.decode(s, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length).copy(Bitmap.Config.ARGB_8888, true);
                        // AppDataにアイコン情報を格納
                        appData.setIcon(new BitmapDrawable(bitmap));
                    }
                }

                // アイコン情報を上書き保存
                num = 0;
                Drawable icon;
                for (AppData info : dataList2) {
                    num++;
                    // 削除したいアプリのリスト番号は保存しない
                    if (num == appnum) {
                        num -= 1;
                        appnum = -1;
                    } else {
                        icon = info.getIcon();
                        Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        String bitmapStr = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                        SharedPreferences pref = getSharedPreferences("DATA" + num, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("ICON", bitmapStr);
                        editor.apply();
                    }
                }
                // 削除したいアプリをリストから削除
                dataList2.remove(appnum2 - 1);

                // シリアライズしてファイルに保存
                FileOutputStream outFile = openFileOutput("appData.file", 0);
                ObjectOutputStream outObject = new ObjectOutputStream(outFile);
                outObject.writeObject(dataList2);
                outObject.close();
                outFile.close();
            }

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

}
