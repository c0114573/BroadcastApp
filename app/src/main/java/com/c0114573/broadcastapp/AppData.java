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

    int useCount;

    boolean appuse;       // 一時的な制限の有無 有:true 無:false

    public AppData(){
    }

    public  void setIcon(Drawable i) { this.icon = i; }

    public void setLock(boolean b) {
        this.lock = b;
    }

    public  void setUseCountPlus() { this.useCount += 1; }

    public String getpackageName() {
        return this.packageName;
    }

    public String getpackageLabel() {
        return this.packageLabel;
    }

    public Drawable getIcon() { return this.icon;}

    public int getUseCount() { return this.useCount; }

    public String getPermission() {
        String spCamera = String.valueOf(pCamera);
        String spSMS = String.valueOf(pSMS);
        String spLocation = String.valueOf(pLocation);

        return spCamera + "," + spSMS + "," + spLocation;
    }

    public String getPermissionName() {
        String spCamera = String.valueOf(pCamera);
        String spSMS = String.valueOf(pSMS);
        String spLocation = String.valueOf(pLocation);

        String resultPermission = "";

        if (spCamera.equals("0")) {
            resultPermission += "    カメラ";
        }
        if (spSMS.equals("0")) {
            resultPermission += "    SMS";
        }
        if (spLocation.equals("0")) {
            resultPermission += "   位置情報";
        }
        return resultPermission;
    }

    public boolean getIsNotPermission() {
        if (pSMS==-1 && pLocation==-1 && pCamera==-1) return true;
        else return false;
    }

    public String getLocationPermission() {
        return String.valueOf(this.pLocation);
    }

    // 制限ありtrue 制限なしfalse
    public boolean getLock() {return  lock; }

}

