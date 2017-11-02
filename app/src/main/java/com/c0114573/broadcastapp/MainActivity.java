package com.c0114573.broadcastapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.AppLaunchChecker;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by C011457331 on 2017/04/19.
 */

public class MainActivity extends Activity {

    private static final int REQUEST_SETTINGS = 1;

    // テスト用テキスト
    TextView tv1;
    TextView tv2;
    String str = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_boot_receiver);

        tv1 = (TextView) findViewById(R.id.textView3);
        tv1.setText("テスト");

        tv2 = (TextView) findViewById(R.id.textView5);
        tv2.setText("テスト");

        // 初期起動時処理
        if (!(AppLaunchChecker.hasStartedFromLauncher(this))) {
            Log.d("AppLaunchChecker", "はじめてアプリを起動した");

            Intent startServiceIntent = new Intent(getBaseContext(), AppDataSetting.class);
            startServiceIntent.putExtra("AppFirstStart", 10);
            startService(startServiceIntent);

        }
        AppLaunchChecker.onActivityCreate(this);
    }

    // ファイルテスト
    public void onFileClick(View v) {
        switch (v.getId()) {

            // リストの初期化,再読み込み
            case R.id.file_save_button:
                Intent startServiceIntent = new Intent(getBaseContext(), AppDataSetting.class);
                startServiceIntent.putExtra("AppFirstStart", 10);
                startService(startServiceIntent);
                break;


            // リストを読み込みテキストに表示
            case R.id.file_read_button:
                str = "";
                try {
                    FileInputStream inFile = openFileInput("appData.file");
                    ObjectInputStream inObject = new ObjectInputStream(inFile);
                    List<AppData> dataList2 = (ArrayList<AppData>) inObject.readObject();
                    inObject.close();
                    inFile.close();

                    for (AppData appData : dataList2) {
                        str += appData.getpackageID() + ":";
                        str += appData.getpackageLabel();
//                        str += appData.getpackageName();
                        str += appData.getPermission() + ",isUsed:";
                        str += String.valueOf(appData.isUsed)+",";
                        str += appData.getUseCount() + "\n";
                    }
                    tv2.setText(str);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (StreamCorruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.file_delete_button:
                break;
        }
    }

    // サービス起動
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

    // リスト表示画面へ遷移
    public void onListButtonClick(View v) {
        Intent intent = new Intent(MainActivity.this, PermissionList.class);
        startActivity(intent);

    }

    // 位置情報設定画面へ遷移
    public void onMapButtonClick(View v) {
//        Intent intent = new Intent(MainActivity.this, LocationInput.class);
        Intent intent = new Intent(MainActivity.this, LocationList.class);
        startActivity(intent);
    }

    // バージョンが6.0以上であるか
    @TargetApi(Build.VERSION_CODES.M)
    private void startService() {
        Intent intent = null;

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // permissionが許可されていません
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // 許可ダイアログで今後表示しないにチェックされていない場合
            }

            // permissionを許可してほしい理由の表示など

            // 許可ダイアログの表示
            // MY_PERMISSIONS_REQUEST_READ_CONTACTSはアプリ内で独自定義したrequestCodeの値
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

        }else if (!canGetUsageStats()) {  // (1)
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            // 先ほどの独自定義したrequestCodeの結果確認
            case 0: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ユーザーが許可したとき
                    // 許可が必要な機能を改めて実行する
                    Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_SHORT).show();
                } else {
                    // ユーザーが許可しなかったとき
                    // 許可されなかったため機能が実行できないことを表示する
                    Toast.makeText(getApplicationContext(), "許可されていません", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}