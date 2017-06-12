package com.c0114573.broadcastapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;

import static android.app.Service.START_STICKY;
import static android.content.ContentValues.TAG;

/**
 * Created by C011457331 on 2017/04/19.
 */

public class MainActivity extends Activity implements LocationListener {
//    implements View.OnClickListener

    LocationManager lm;

    private final int REQUEST_PERMISSION = 1000;

    TextView tv;
    String str1 = "GPS読み取れてないよ";
    String str2 = "結合";


    //学校 35.625122, 139.342143
    double confLatitude = 35.625122;    // 設定緯度
    double confLongitude = 139.342143;   // 設定経度

    double myLatitude = 0;    // 現在の緯度
    double myLongitude = 0;   // 現在の経度


    String targetStr = new String("");    // 緯度経度を持ってくる

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_boot_receiver);

        tv = (TextView)findViewById(R.id.textView3);
        tv.setText("テスト");

        // Android 6, API 23以上でパーミッシンの確認
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        } else {
        }
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        if (lm != null) {
            Log.d("LocationActivity", "locationManager.requestLocationUpdates");
            // バックグラウンドから戻ってしまうと例外が発生する場合がある

            try {
                // minTime = 1000msec, minDistance = 50m
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, this);

            } catch (Exception e) {
                e.printStackTrace();

                Toast toast = Toast.makeText(this, "例外が発生、位置情報のPermissionを許可していますか？", Toast.LENGTH_SHORT);
                toast.show();

//                //MainActivityに戻す
//                finish();
            }
        }
    }




    // 位置情報許可の確認
    public void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        }
        // 位置情報権限がなかった場合
        else {
            requestLocationPermission();
        }
    }

    // 位置情報許可を求める通知表示
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);

        } else {
            Toast toast = Toast.makeText(this, "許可されないとアプリが実行できません", Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, REQUEST_PERMISSION);
        }
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this, "これ以上なにもできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }




    // ローカルに文字列を保存
    public void onFileClick(View v) {
        switch (v.getId()) {
            case R.id.file_save_button:
                try {
                    FileOutputStream out = openFileOutput("test.txt", MODE_PRIVATE);

                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
//                    bw.write(tv.getText().toString());

                    str1 =  Double.toString(myLatitude);
                    str2 =  Double.toString(myLongitude);
                    StringBuffer bf = new StringBuffer();
                    bf.append(str1);
                    bf.append(",");
                    bf.append(str2);

                    bw.write(bf.toString());

                    bw.flush();

//                    out.write(str.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.file_read_button:
                try {
                    FileInputStream fis = openFileInput("test.txt");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
                    String tmp;
                    tv.setText("");
                    while ((tmp = reader.readLine()) != null) {
                        tv.append(tmp + "\n");

                        targetStr = tmp;
                    }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                Toast.makeText(this, targetStr,Toast.LENGTH_LONG).show();

                Pattern pattern = Pattern.compile(",");
                String[] splitStr = pattern.split(targetStr);
                for (int i = 0; i < splitStr.length; i++) {
                    System.out.println(splitStr[i]);
                }

                // ファイルはあるけど読み込みが正しくできてないっぽい
                try {
                    confLatitude = Double.parseDouble(splitStr[0]);
                    confLongitude = Double.parseDouble(splitStr[1]);

                    Toast.makeText(this, "緯度" + confLatitude + "経度" + confLongitude,
                            Toast.LENGTH_LONG).show();
                } catch (Exception e){
                    Toast.makeText(this, "ファイル読み込み失敗",Toast.LENGTH_LONG).show();
                }

                break;

            case R.id.file_delete_button:

                deleteFile("test.txt");
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startButton:
                startService(new Intent(getBaseContext(), ExampleService.class));
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

    // GPS関係
    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "緯度" + location.getLatitude() + "経度" + location.getLongitude(),
                Toast.LENGTH_LONG).show();

        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

}