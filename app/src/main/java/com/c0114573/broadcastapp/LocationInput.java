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
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;
import static com.c0114573.broadcastapp.R.layout.activity_locationinput;

/**
 * Created by member on 2017/06/07.
 */

public class LocationInput extends Activity implements LocationListener, OnMapReadyCallback {
//    implements View.OnClickListener

    private GoogleMap mMap;
    LocationManager lm;
    private MapView mv;
    MapFragment mf;

    private final int REQUEST_PERMISSION = 1000;

    TextView tv;
    TextView tv2;
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

        tv = new TextView(this);
        tv.setText("");



        setContentView(activity_locationinput);

        //setContentView(R.layout.activity_locationinput);

//        mf = MapFragment.newInstance();
//
//        FragmentManager fm = getFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//        ft.add(android.R.id.content, mf);
//        ft.commit();


//      /  mv = (MapView)findViewByID(R.id.mapView2);

        Fragment mapFragment = (Fragment)getFragmentManager().findFragmentById(R.id.fragment);
//        mapFragment.getMapAsync(this);

        // 読み込み
        targetStr = "";
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

        tv2 = (TextView) findViewById(R.id.latlng_text);
        tv2.setText("");
        // ファイルはあるけど読み込みが正しくできてないっぽい
        try {
            confLatitude = Double.parseDouble(splitStr[0]);
            confLongitude = Double.parseDouble(splitStr[1]);

            tv2.setText("現在の設定:" + confLatitude + "," + confLongitude);
        } catch (Exception e) {
            tv2.setText("現在の設定:");
        }


        // GPS
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

    @Override
    public void onMapReady(GoogleMap googleMap){

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

                // 読み込み
                targetStr = "";
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

                tv2 = (TextView) findViewById(R.id.latlng_text);
                tv2.setText("");
                // ファイルはあるけど読み込みが正しくできてないっぽい
                try {
                confLatitude = Double.parseDouble(splitStr[0]);
                confLongitude = Double.parseDouble(splitStr[1]);

                Toast.makeText(this, "緯度" + confLatitude + "経度" + confLongitude,
                        Toast.LENGTH_LONG).show();
                tv2.setText("現在の設定:" + confLatitude + "," + confLongitude);
                } catch (Exception e) {
                    Toast.makeText(this, "ファイル読み込み失敗", Toast.LENGTH_LONG).show();
                }

                break;


            case R.id.location_input_button:
                try {
                    FileOutputStream out = openFileOutput("test.txt", MODE_PRIVATE);

                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
//                    bw.write(tv.getText().toString());

                    str1 = Double.toString(35.625122);
                    str2 = Double.toString(139.342143);
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

                // 読み込み
                targetStr = "";
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

                Pattern pattern2 = Pattern.compile(",");
                splitStr = pattern2.split(targetStr);
                for (int i = 0; i < splitStr.length; i++) {
                    System.out.println(splitStr[i]);
                }

                tv2 = (TextView) findViewById(R.id.latlng_text);
                tv2.setText("");
                // ファイルはあるけど読み込みが正しくできてないっぽい
                try {
                    confLatitude = Double.parseDouble(splitStr[0]);
                    confLongitude = Double.parseDouble(splitStr[1]);

                    Toast.makeText(this, "緯度" + confLatitude + "経度" + confLongitude,
                            Toast.LENGTH_LONG).show();
                    tv2.setText("現在の設定:" + confLatitude + "," + confLongitude);
                } catch (Exception e) {
                    Toast.makeText(this, "ファイル読み込み失敗", Toast.LENGTH_LONG).show();
                }

                break;


            // 削除
            case R.id.location_delete_button:
                deleteFile("test.txt");
                tv2 = (TextView) findViewById(R.id.latlng_text);
                tv2.setText("現在の設定:");
        }
    }


    // GPS関係
    @Override
    public void onLocationChanged(Location location) {
//        Toast.makeText(this, "緯度" + location.getLatitude() + "経度" + location.getLongitude(), Toast.LENGTH_LONG).show();
        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();
//        Toast.makeText(this, "緯度" + myLatitude + "経度" + myLongitude, Toast.LENGTH_LONG).show();
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