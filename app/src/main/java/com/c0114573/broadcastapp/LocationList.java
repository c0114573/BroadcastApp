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
import android.widget.Adapter;
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

    LocationListAdapter adapter;

    ListView listView;

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
//            LocationListAdapter adapter = new LocationListAdapter(getApplicationContext());
            adapter = new LocationListAdapter(getApplicationContext());
            // ListViewにアダプタを設定
            for (LocationData ld : locationDatas) {
                adapter.add(ld);
            }

            // ListView表示
//            final ListView listView = (ListView) findViewById(R.id.card_list);
            listView = (ListView) findViewById(R.id.card_list);
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
    public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
//        Toast.makeText(this, "こんにちは" + position, Toast.LENGTH_SHORT).show();

        String validText = locationDatas.get(position).getNotValidText();

        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        String[] items = {validText, "編集", "削除"};
        dlg.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = null;
                Uri uri;
                PackageManager pManager = getPackageManager();
                switch (which) {

                    case 0:
                        if (locationDatas.get(position).valid) {
                            locationDatas.get(position).setValid(false);

                        } else {
                            locationDatas.get(position).setValid(true);
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
                        adapter.notifyDataSetChanged();

                        // アクティビティ再起動
//                        Intent intent2 = getIntent();
//                        overridePendingTransition(0, 0);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                        finish();
//                        overridePendingTransition(0, 0);
//                        startActivity(intent2);
                        break;

                    case 1:
                        updateDialog(position);
                        break;

                    case 2:
                        LocationData ld = locationDatas.get(position);
                        adapter.remove(ld);
                        adapter.notifyDataSetChanged();

                        // 選択されたアイテムをリストから削除
                        locationDatas.remove(position);

                        try {
                            // シリアライズしてファイルに保存
                            FileOutputStream outFile = openFileOutput("locationData.file", Context.MODE_PRIVATE);
                            ObjectOutputStream outObject = new ObjectOutputStream(outFile);
                            outObject.writeObject(locationDatas);
                            outObject.close();
                            outFile.close();
                            
                            // デシリアライズ(読み込み)
                            FileInputStream inFile = openFileInput("locationData.file");
                            ObjectInputStream inObject = new ObjectInputStream(inFile);
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
        builder.setPositiveButton("新規登録", new DialogInterface.OnClickListener() {
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
                locationData.valid = true;

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

    private void updateDialog(final int position) {

        // カスタムビューを設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.location_dialog,
                (ViewGroup) findViewById(R.id.layout_root));

        // レイアウトのエディットなどのid取得
        EditText setlocation = (EditText) layout.findViewById(R.id.customDlg_location);
        EditText setlat = (EditText) layout.findViewById(R.id.customDlg_lat);
        EditText setlng = (EditText) layout.findViewById(R.id.customDlg_lng);
        Spinner setspinner = (Spinner) layout.findViewById(R.id.spinner);

        // 登録されている値をセット
        setlocation.setText(locationDatas.get(position).locationName);
        setlat.setText(String.valueOf(locationDatas.get(position).lat));
        setlng.setText(String.valueOf(locationDatas.get(position).lng));
        int ret = locationDatas.get(position).distance;
        setspinner.setSelection(ret - 1);

        // アラートダイアログを生成
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("位置情報設定");
        builder.setView(layout);
        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
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
                locationData.valid = locationDatas.get(position).valid;

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
                locationDatas.remove(position);

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