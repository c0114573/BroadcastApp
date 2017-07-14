package com.c0114573.broadcastapp;

import android.graphics.drawable.Drawable;

/**
 * Created by member on 2017/07/11.
 */

// アプリケーションデータ格納クラス
public class AppData {
    String packageLabel;    // パッケージラベル
    String packageName;     // パッケージ名
    Drawable icon;          // アイコン

    int pCamera;    // カメラ権限 有:0,無:-1
    int pSMS;       // SMS権限
    int pLocation;  // 位置情報権限
    int lock;       // 制限の有無

    public AppData(){
        lock = 0;
    }

    public void setLock(int n) {
        this.lock = n;
    }

    public String getpackageName() {
        return this.packageName;
    }

    public String getpackageLabel() {
        return this.packageLabel;
    }

    public Drawable getIcon() { return this.icon;}

    public String getPermission() {
        String spCamera = String.valueOf(pCamera);
        String spSMS = String.valueOf(pSMS);
        String spLocation = String.valueOf(pLocation);

        return spCamera + "," + spSMS + "," + spLocation;
    }

    public String getLock() {return  String.valueOf(lock); }

}

