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
import android.graphics.BitmapFactory;
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

import com.c0114573.broadcastapp.AppDataSetting;

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
    String deleteApp = "";
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
        if (!(AppLaunchChecker.hasStartedFromLauncher(this))) {
            Log.d("AppLaunchChecker", "はじめてアプリを起動した");

            Intent startServiceIntent = new Intent(getBaseContext(),AppDataSetting.class);
            startServiceIntent.putExtra("AppFirstStart",10);
            startService(startServiceIntent);

        }
        AppLaunchChecker.onActivityCreate(this);

    }

    public void onFileClick(View v) {
        switch (v.getId()) {

            // クラスの更新テスト
            case R.id.file_save_button:
                Intent startServiceIntent = new Intent(getBaseContext(),AppDataSetting.class);
                startServiceIntent.putExtra("AppFirstStart",10);
                startService(startServiceIntent);

                break;


            // デシリアライズ(読み込み)
            case R.id.file_read_button:
                str = "";
                try {
                    //FileInputStream inFile = new FileInputStream(FILE_NAME);
                    FileInputStream inFile = openFileInput("appData.file");
                    ObjectInputStream inObject = new ObjectInputStream(inFile);
                    List<AppData> dataList2 = (ArrayList<AppData>) inObject.readObject();
                    inObject.close();
                    inFile.close();

                    for (AppData appData : dataList2) {
                        str += appData.getpackageLabel();
                        str += appData.getPermission() + ",";
                        str += String.valueOf(appData.getLock()) + "\n";
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