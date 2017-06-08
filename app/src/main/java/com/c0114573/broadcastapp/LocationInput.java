package com.c0114573.broadcastapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
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

/**
 * Created by member on 2017/06/07.
 */

public class LocationInput extends Activity implements LocationListener {
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

    }



    // ローカルに文字列を保存
    public void onFileClick(View v) {
        switch (v.getId()) {
            case R.id.location_input_button:
                try {
                    FileOutputStream out = openFileOutput("test.txt", MODE_PRIVATE);

                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
//                    bw.write(tv.getText().toString());

                    str1 = Double.toString(myLatitude);
                    str2 = Double.toString(myLongitude);
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


            case R.id.location_delete_button:

                deleteFile("test.txt");
        }
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