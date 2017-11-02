package com.c0114573.broadcastapp;

import android.Manifest;
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
import android.os.IBinder;
import android.support.v4.app.AppLaunchChecker;
import android.util.Base64;
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

    boolean appNew = true;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 現在の端末内アプリから特定アプリをリストに追加,更新
        // get~Extra  キーがないとき第二引数の値となる
        if (intent != null && intent.getIntExtra("AppFirstStart", 0) == 10) {
            NewAppList(); // 特定アプリ一覧を作成
        }

        // インストール時
        else if (intent != null && intent.getIntExtra("INSTALL", 0) == 20) {
            String appName = intent.getStringExtra("APPNAME");
            InstallAppList(appName);
        }

        // アンインストール時
        else if (intent != null && intent.getIntExtra("UNINSTALL", 0) == 30) {
            String appName = intent.getStringExtra("APPNAME");
            UnInstallAppList(appName);
        }

        // 更新時
        else if (intent != null && intent.getIntExtra("UPDATE", 0) == 40) {
            String appName = intent.getStringExtra("APPNAME");
            UpdateAppList(appName);
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
        int i=0;
        appInfo:
        for (ApplicationInfo info : applicationInfo) {
            pSMS = -1;
            pLocation = -1;

            // 自分自身を除外
            if (info.packageName.equals(this.getPackageName())) continue;
//
//            // 安全なアプリを除外
            if (info.packageName.equals("com.android.settings")) continue;

            // 起動不可能なアプリを除外
            for (String app : appList) {
                if ((info.packageName.equals(app))) continue appInfo;
            }

            // パーミッションを持っているか
            int pNetwork = getPackageManager().checkPermission(Manifest.permission.INTERNET, info.packageName);
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
                data.packageID = i;
                i++;
                data.packageLabel = info.loadLabel(packageManager).toString();
                data.packageName = info.packageName;
                data.icon = info.loadIcon(packageManager);
                data.pNetwork = pNetwork;
                data.pCamera = pCamera;
                data.pSMS = pSMS;
                data.pLocation = pLocation;
                data.isUsed = false;
                data.useCount = 0;
                data.lock = true;

                // アプリ情報クラスをリストに追加
                dataListNew.add(data);
            }
        }

        // アイコン情報を保存
        Drawable icon;
        int num = 0;
        for (AppData info : dataListNew) {
            num++;
            icon = info.getIcon();
            // ビットマップに変換
            Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
            // バイト配列出力を扱う
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // ビットマップを圧縮する
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos); // 100でいい
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
            FileOutputStream outFile = openFileOutput("appData.file", Context.MODE_PRIVATE);
            ObjectOutputStream outObject = new ObjectOutputStream(outFile);
            if (appNew == true) {
                outObject.writeObject(dataListNew);
            } else {
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
    public void InstallAppList(String installApp) {

        // パッケージマネージャーの作成
        PackageManager packageManager = getPackageManager();

        // 起動不可能なアプリをリストに格納
        List<PackageInfo> pckInfoList = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        for (PackageInfo pckInfo : pckInfoList) {
            if (packageManager.getLaunchIntentForPackage(pckInfo.packageName) == null) {
                appList.add(pckInfo.packageName);
            }
        }
        // インストール済みアプリの情報を取得
        List<ApplicationInfo> applicationInfo = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        try {
            // デシリアライズ(読み込み)
            FileInputStream inFile = openFileInput("appData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            List<AppData> dataList2 = (ArrayList<AppData>) inObject.readObject();
            inObject.close();
            inFile.close();

            // リストにアプリデータを格納
            appInfo:
            for (ApplicationInfo info : applicationInfo) {
                // 自分自身を除外
                if (info.packageName.equals(this.getPackageName())) continue;

                // 起動不可能なアプリを除外
                for (String app : appList) {
                    if ((info.packageName.equals(app))) continue appInfo;
                }

                // 新しくインストールしたアプリをリストに追加
                if (info.packageName.equals(installApp)) {
                    // アプリ情報クラスにアプリ情報を追加
                    AppData data = new AppData();
                    data.packageLabel = info.loadLabel(packageManager).toString();
                    data.packageName = info.packageName;
                    data.icon = info.loadIcon(packageManager);
                    data.pNetwork = -1;
                    data.pCamera = -1;
                    data.pSMS = -1;
                    data.pLocation = -1;
                    data.isUsed = false;
                    data.useCount = 0;
                    data.lock = true;

                    // アプリ情報クラスをリストに追加
                    dataList2.add(data);
                }
            }

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
                    // 新しくインストールしたアプリをリストの最後に追加
                    if (dataList2.size() == num) {
                        appData.setIcon(appData.getIcon());
                    } else {
                        appData.setIcon(new BitmapDrawable(bitmap));
                    }
                }
            }

            // アイコン情報を上書き保存
            num = 0;
            Drawable icon;
            for (AppData info : dataList2) {
                num++;
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

            // シリアライズしてファイルに保存
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

    // アンインストール時
    public void UnInstallAppList(String deleteApp) {
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
            boolean isfound = false;
//            deleteApp = "com.nianticlabs.pokemongo";

            for (AppData appData : dataList2) {
                appcnt++;
                // 削除したいアプリのリスト番号を取得
                if (appData.getpackageName().equals(deleteApp)) {
                    appnum = appcnt;
                    appnum2 = appcnt;
                    isfound = true; //見つかった
                }
            }

            if (isfound == true) {

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
                FileOutputStream outFile = openFileOutput("appData.file", Context.MODE_PRIVATE);
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


    // アプリ使用更新
    public void UpdateAppList(String updateApp) {
        try {
            // 読み込み
            FileInputStream inFile = openFileInput("appData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            List<AppData> dataList2 = (ArrayList<AppData>) inObject.readObject();
            inObject.close();
            inFile.close();

//            deleteApp = "com.nianticlabs.pokemongo";

            for (AppData appData : dataList2) {
                if (appData.getpackageName().equals(updateApp)) {
                    if(appData.getIsUsed()){
                     appData.setIsUsed(false);
                    } else {
                        appData.setIsUsed(true);
                    }
                }
            }

            // 保存
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


}
