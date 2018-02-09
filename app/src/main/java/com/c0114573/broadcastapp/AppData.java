package com.c0114573.broadcastapp;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Created by member on 2017/07/11.
 * 位置情報漏洩の恐れのあるアプリ情報を保存するクラス
 */

// アプリケーションデータ格納クラス
public class AppData implements Serializable {
    int packageID;              // ID
    String packageLabel;        // パッケージラベル
    String packageName;         // パッケージ名
    transient Drawable icon;    // アイコン

    // 権限 有:0,無:-1
    int pNetwork;   // ネットワーク権限
    int pCamera;    // カメラ権限
    int pSMS;       // SMS権限
    int pLocation;  // 位置情報権限

    int pWakeLock; // 端末のスリープの無効化
    int pReceive; // ブロードキャストレシーバを使用しているか

    boolean lock;   // 制限の有無 有:true 無:false
    boolean isUsed; // 現在起動しているか
    int useCount;   // 使われた回数

    public AppData(){
    }

    public  void setIcon(Drawable i) { this.icon = i; }

    public void setLock(boolean b) {
        this.lock = b;
    }

    public  void setUseCountPlus() { this.useCount += 1; }

    public  void setIsUsed(boolean b) {
        this.isUsed = b;
    }


    public String getpackageName() {
        return this.packageName;
    }

    public String getpackageLabel() {
        return this.packageLabel;
    }

    public Drawable getIcon() { return this.icon;}




    // 制限ありtrue 制限なしfalse
    public boolean getLock() {return  lock; }

    public boolean getIsUsed() {return  isUsed; }

    public int getUseCount() { return this.useCount; }

    public int getpackageID() { return this.packageID; }

    public String getPermission() {
        String spCamera = String.valueOf(pCamera);
        String spSMS = String.valueOf(pSMS);
        String spLocation = String.valueOf(pLocation);

        return spCamera + "," + spSMS + "," + spLocation;
    }

    // アプリ一覧画面で使用
    public String getPermissionName() {
        String spCamera = String.valueOf(pCamera);
        String spSMS = String.valueOf(pSMS);
        String spLocation = String.valueOf(pLocation);

        String spWakeLock = String.valueOf(pWakeLock);
        String spReceive = String.valueOf(pReceive);

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
        if (spWakeLock.equals("0")) {
//            resultPermission += "   スリープ無効";
        }
        if (spReceive.equals("0")) {
            resultPermission += "   常時実行";
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

    public String getSelectText() {
        if (lock) {
            return "制限解除";
        } else {
            return "制限登録";
        }
    }
}

