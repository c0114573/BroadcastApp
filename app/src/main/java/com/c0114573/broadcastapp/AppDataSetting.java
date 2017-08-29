package com.c0114573.broadcastapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by member on 2017/08/02.
 */

public class AppDataSetting extends Activity {

    public AppDataSetting(){
    }
    private Context mAppContext;

    List<AppData> dataList = new ArrayList<AppData>();

    public void InitialSetting() {
        // リスト作成
        ArrayList<String> appList = new ArrayList<String>();

        mAppContext = mAppContext.getApplicationContext();

        // パッケージマネージャーの作成
        PackageManager packageManager = mAppContext.getPackageManager();

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
                dataList.add(data);
            }
        }

        // アイコン情報を保存
        Drawable icon ;
        int num = 0;
        for (AppData info : dataList) {
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

        // シリアライズしてAppDataをファイルに保存
        try {
            //FileOutputStream outFile = new FileOutputStream(FILE_NAME);
            FileOutputStream outFile = openFileOutput("appData.file", 0);
            ObjectOutputStream outObject = new ObjectOutputStream(outFile);
            outObject.writeObject(dataList);
            outObject.close();
            outFile.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
