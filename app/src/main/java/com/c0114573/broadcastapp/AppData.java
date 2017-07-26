package com.c0114573.broadcastapp;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Created by member on 2017/07/11.
 */

// アプリケーションデータ格納クラス
public class AppData implements Serializable {
    String packageLabel;    // パッケージラベル
    String packageName;     // パッケージ名
    transient Drawable icon;          // アイコン

    int pCamera;    // カメラ権限 有:0,無:-1
    int pSMS;       // SMS権限
    int pLocation;  // 位置情報権限
    boolean lock;       // 制限の有無 有:true 無:false

    public AppData(){
    }

    public  void setIcon(Drawable i) { this.icon = i; }

    public void setLock(boolean b) {
        this.lock = b;
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

    // 制限ありtrue 制限なしfalse
    public boolean getLock() {return  lock; }

}

