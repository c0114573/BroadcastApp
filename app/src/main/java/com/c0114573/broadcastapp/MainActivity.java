package com.c0114573.broadcastapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.AppLaunchChecker;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static android.app.Service.START_STICKY;
import static android.content.ContentValues.TAG;

/**
 * Created by C011457331 on 2017/04/19.
 */

public class MainActivity extends Activity {
//    implements View.OnClickListener

    private final int REQUEST_PERMISSION = 1000;
    private static final int REQUEST_SETTINGS = 1;

    TextView tv;
    TextView tv3;
    String str = "";
    List<AppData> dataList = new ArrayList<AppData>();

    String targetStr = new String("");    // 緯度経度を持ってくる


    public static int OVERLAY_PERMISSION_REQ_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_boot_receiver);

        tv3 = (TextView) findViewById(R.id.textView3);
        tv3.setText("テスト");

        tv = (TextView) findViewById(R.id.textView5);
        tv.setText("テスト");

        // 初期起動時処理
        if(!(AppLaunchChecker.hasStartedFromLauncher(this))){
            Log.d("AppLaunchChecker","はじめてアプリを起動した");
            InitialSetting();
        }
        AppLaunchChecker.onActivityCreate(this);

    }

    public void onFileClick(View v) {
        switch (v.getId()) {

            // クラスの更新テスト
            case R.id.file_save_button:
                try {
                    // デシリアライズ
                    FileInputStream inFile = openFileInput("appData.file");
                    ObjectInputStream inObject = new ObjectInputStream(inFile);
                    List<AppData> dataList2 = (ArrayList<AppData>) inObject.readObject();
                    inObject.close();
                    inFile.close();

                    for (AppData appData : dataList2) {
//                        if(appData.getpackageName().equals("com.nianticlabs.pokemongo")){
//                            appData.setLock(0);
//                        }
                        appData.setLock(true);
                    }

                    // シリアライズしてファイルに保存
                    FileOutputStream outFile = openFileOutput("appData.file", 0);
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
                break;


            // デシリアライズ(読み込み)
            case R.id.file_read_button:
                str="";
                try {
                    //FileInputStream inFile = new FileInputStream(FILE_NAME);
                    FileInputStream inFile = openFileInput("appData.file");
                    ObjectInputStream inObject = new ObjectInputStream(inFile);
                    List<AppData> dataList2 = (ArrayList<AppData>) inObject.readObject();
                    inObject.close();
                    inFile.close();

                    for (AppData appData : dataList2) {
                        str += appData.getpackageLabel();
                        str += appData.getPermission()+",";
                        str += String.valueOf(appData.getLock())+"\n";
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (StreamCorruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                tv.setText(str);

                break;

            case R.id.file_delete_button:
                break;
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startButton:
//                startService(new Intent(getBaseContext(), ExampleService.class));
                startService();
                break;

            case R.id.stopButton:
                stopService(new Intent(getBaseContext(), ExampleService.class));
                break;
        }
    }

    // リスト表示
    public void onListButtonClick(View v) {
        Intent intent = new Intent(MainActivity.this, PermissionList.class);
        startActivity(intent);

    }

    public void onMapButtonClick(View v) {
        Intent intent = new Intent(MainActivity.this, LocationInput.class);
        startActivity(intent);

    }

    // 初期起動時
    public void InitialSetting() {
        // リスト作成
        ArrayList<String> appList = new ArrayList<String>();

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


    // バージョンが6.0以上であるか
    @TargetApi(Build.VERSION_CODES.M)
    private void startService() {
        Intent intent = null;
        if (!canGetUsageStats()) {  // (1)
            intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        } else if (!canDrawOverlays()) {  // (2)
            Uri uri = Uri.parse("package:" + getPackageName());
            intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
        }
        if (intent != null) {  // (3)
            startActivityForResult(intent, REQUEST_SETTINGS);
            Toast.makeText(getApplicationContext(), "Please turn ON", Toast.LENGTH_SHORT).show();
        } else {
            startService(new Intent(getBaseContext(), ExampleService.class));
        }
    }

    // (1)「使用履歴へのアクセス」の権限があるか？
    public boolean canGetUsageStats() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {  // 【1】
            return true;
        }
        AppOpsManager aom = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
        int uid = android.os.Process.myUid();
        int mode = aom.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, uid, getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    // (2)「画面の上に表示」の権限があるか？
    private boolean canDrawOverlays() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {  // 【1】
            return true;
        }
        return Settings.canDrawOverlays(getApplicationContext());
    }


}