package com.c0114573.broadcastapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.AppLaunchChecker;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

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
 * Created by member on 2017/10/08.
 */

public class LocationList extends Activity implements AdapterView.OnItemClickListener {

    List<LocationData> locationDatas = new ArrayList<LocationData>();

//    String[] str = {"自宅", "職場", "学校"};
//    double[] lat = {139, 139.5, 138};
//    double[] lng = {35, 35.5, 34};
//    int[] dis = {5, 5, 5};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locationlist);

        try {
            // デシリアライズ(読み込み)
            FileInputStream inFile = openFileInput("locationData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            locationDatas = (ArrayList<LocationData>) inObject.readObject();
            inObject.close();
            inFile.close();

            // 独自のアダプタを利用
            LocationListAdapter adapter = new LocationListAdapter(getApplicationContext());
            // ListViewにアダプタを設定
            for (LocationData ld : locationDatas) {
                adapter.add(ld);
            }

            // ListView表示
            final ListView listView = (ListView) findViewById(R.id.card_list);
            listView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
            listView.setDivider(null);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
//        Toast.makeText(this, "こんにちは" + position, Toast.LENGTH_SHORT).show();

        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        String[] items = {"編集", "削除"};
        dlg.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = null;
                Uri uri;
                PackageManager pManager = getPackageManager();
                switch (which) {

                    case 0:
//                        displayDialog();
                        break;

                    case 1:
                        try {
                            // デシリアライズ(読み込み)
                            FileInputStream inFile = openFileInput("locationData.file");
                            ObjectInputStream inObject = new ObjectInputStream(inFile);
                            locationDatas = (ArrayList<LocationData>) inObject.readObject();
                            inObject.close();
                            inFile.close();

                            // 選択されたアイテムをリストから削除
                            locationDatas.remove(position);

                            // ListViewにアダプタを設定
                            LocationListAdapter adapter = new LocationListAdapter(getApplicationContext());
                            for (LocationData ld : locationDatas) {
                                adapter.add(ld);
                            }

                            final ListView listView = (ListView) findViewById(R.id.card_list);
                            listView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
                            listView.setDivider(null);
                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener(LocationList.this);

                        } catch (StreamCorruptedException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            // シリアライズしてファイルに保存
                            FileOutputStream outFile = openFileOutput("locationData.file", Context.MODE_PRIVATE);
                            ObjectOutputStream outObject = new ObjectOutputStream(outFile);
                            outObject.writeObject(locationDatas);
                            outObject.close();
                            outFile.close();

                        } catch (StreamCorruptedException e) {
                            e.printStackTrace();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    default:
                        break;
                }
            }
        });
        dlg.show();
    }


    // 新規作成
    public void onClick(View v) {
        displayDialog();

//        LocationListAdapter adapter = new LocationListAdapter(getApplicationContext());
//
//        LocationData locationData = new LocationData();
//        locationData.locationName = "場所";
//        locationData.lat = 100;
//        locationData.lng = 50;
//        locationData.distance = 10;
//
//        try {
//            // デシリアライズ(読み込み)
//            FileInputStream inFile = openFileInput("appData.file");
//            ObjectInputStream inObject = new ObjectInputStream(inFile);
////            List<AppData> dataList2 = (ArrayList<AppData>) inObject.readObject();
//            locationDatas = (ArrayList<LocationData>) inObject.readObject();
//            inObject.close();
//            inFile.close();
//
//        } catch (StreamCorruptedException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        locationDatas.add(locationData);
//
//        // リストビューにアダプタを設定
//        for (LocationData ld : locationDatas) {
//            adapter.add(ld);
//        }
//        final ListView listView = (ListView) findViewById(R.id.card_list);
//        listView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
//        listView.setDivider(null);
//        listView.setAdapter(adapter);
//        listView.setOnItemClickListener(this);
//
//        try {
//            // シリアライズしてファイルに保存
//            FileOutputStream outFile = openFileOutput("appData.file", 0);
//            ObjectOutputStream outObject = new ObjectOutputStream(outFile);
//            outObject.writeObject(locationDatas);
//            outObject.close();
//            outFile.close();
//
//        } catch (StreamCorruptedException e) {
//            e.printStackTrace();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    private void displayDialog() {
        // カスタムビューを設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.location_dialog,
                (ViewGroup) findViewById(R.id.layout_root));

        // アラーとダイアログ を生成
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("位置情報設定");
        builder.setView(layout);
        builder.setPositiveButton("登録", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // OK ボタンクリック処理
                // ID と PASSWORD を取得
                EditText location = (EditText) layout.findViewById(R.id.customDlg_location);
                EditText lat = (EditText) layout.findViewById(R.id.customDlg_lat);
                EditText lng = (EditText) layout.findViewById(R.id.customDlg_lng);
                Spinner spinner = (Spinner) layout.findViewById(R.id.spinner);

                if (location.getText().length() == 0 || lat.getText().length() == 0 || lng.getText().length() == 0) {
                    Toast.makeText(LocationList.this, "入力されていません", Toast.LENGTH_SHORT).show();
                    return;
                }

                String strLocation = location.getText().toString();
                String strLat = lat.getText().toString();
                String strLng = lng.getText().toString();

                String item = (String) spinner.getSelectedItem();
                int ret = Integer.parseInt(item.replaceAll("[^0-9]", ""));

                LocationListAdapter adapter = new LocationListAdapter(getApplicationContext());

                LocationData locationData = new LocationData();
                locationData.locationName = strLocation;
                locationData.lat = Double.parseDouble(strLat);
                locationData.lng = Double.parseDouble(strLng);
                locationData.distance = ret;

                try {
                    // デシリアライズ(読み込み)
                    FileInputStream inFile = openFileInput("locationData.file");
                    ObjectInputStream inObject = new ObjectInputStream(inFile);
//            List<AppData> dataList2 = (ArrayList<AppData>) inObject.readObject();
                    locationDatas = (ArrayList<LocationData>) inObject.readObject();
                    inObject.close();
                    inFile.close();

                } catch (StreamCorruptedException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                locationDatas.add(locationData);

                // リストビューにアダプタを設定
                for (LocationData ld : locationDatas) {
                    adapter.add(ld);
                }
                final ListView listView = (ListView) findViewById(R.id.card_list);
                listView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
                listView.setDivider(null);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(LocationList.this);

                try {
                    // シリアライズしてファイルに保存
                    FileOutputStream outFile = openFileOutput("locationData.file", Context.MODE_PRIVATE);
                    ObjectOutputStream outObject = new ObjectOutputStream(outFile);
                    outObject.writeObject(locationDatas);
                    outObject.close();
                    outFile.close();

                } catch (StreamCorruptedException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Cancel ボタンクリック処理
            }
        });

        // 表示
        builder.create().show();
    }

}