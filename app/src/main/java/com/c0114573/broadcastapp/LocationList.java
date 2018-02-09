package com.c0114573.broadcastapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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
 * 登録位置情報設定画面
 * リストアダプター:LocationListAdapter
 * レイアウト:activity_locationlist
 */

public class LocationList extends Activity implements AdapterView.OnItemClickListener {

    List<LocationData> locationData = new ArrayList<LocationData>();

    LocationListAdapter adapter;

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locationlist);

        // 独自のadapterを利用
        adapter = new LocationListAdapter(getApplicationContext());

        // ListViewにadapterを設定
        listView = (ListView) findViewById(R.id.card_list);
        listView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
        listView.setDivider(null);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        try {
            // デシリアライズ(読み込み)
            FileInputStream inFile = openFileInput("locationData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            locationData = (ArrayList<LocationData>) inObject.readObject();
            inObject.close();
            inFile.close();

            // 登録内容をadapterに追加
            for (LocationData ld : locationData) {
                adapter.add(ld);
            }
            adapter.notifyDataSetChanged();

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

    // 新規作成
    public void onClick(View v) {
        if (locationData.size() < 10) {
            newCreateDialog();
        } else {
            Toast.makeText(this,"登録できる位置情報は10件までです",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
        String validText = locationData.get(position).selectValidText();

        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        String[] items = {validText, "編集", "削除"};
        dlg.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    // 有効,無効設定
                    case 0:
                        if (locationData.get(position).valid) {
                            locationData.get(position).setValid(false);

                        } else {
                            locationData.get(position).setValid(true);
                        }
                        adapter.notifyDataSetChanged();

                        UpdateLocationData();
                        break;

                    // 編集
                    case 1:
                        updateDialog(position);
                        break;

                    // 削除
                    case 2:
                        AlertDialog.Builder deldlg = new AlertDialog.Builder(LocationList.this);
                        deldlg.setTitle(locationData.get(position).locationName);
                        deldlg.setMessage("この位置情報を削除しますか？");
                        deldlg.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                LocationData ld = locationData.get(position);

                                // 選択されたアイテムをリストから削除
                                locationData.remove(position);
                                adapter.remove(ld);
                                adapter.notifyDataSetChanged();

                                UpdateLocationData();
                            }});
                        deldlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                            }});
                        deldlg.show();
                        break;

                    default:
                        break;
                }
            }
        });
        dlg.show();
    }

    private void newCreateDialog() {
        // カスタムビューを設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.location_dialog,
                (ViewGroup) findViewById(R.id.layout_root));

        // アラートダイアログを生成
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("位置情報設定");
        builder.setView(layout);
        builder.setPositiveButton("新規登録", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // レイアウトのエディット,スピナーなどのid取得
                EditText location = (EditText) layout.findViewById(R.id.customDlg_location);
                EditText lat = (EditText) layout.findViewById(R.id.customDlg_lat);
                EditText lng = (EditText) layout.findViewById(R.id.customDlg_lng);
                Spinner spinner = (Spinner) layout.findViewById(R.id.spinner);

                if (location.getText().length() == 0 || lat.getText().length() == 0 || lng.getText().length() == 0) {
                    Toast.makeText(LocationList.this, "未記入の項目があります", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 入力された値の取得
                String strLocation = location.getText().toString();
                String strLat = lat.getText().toString();
                String strLng = lng.getText().toString();
                String item = (String) spinner.getSelectedItem();
                int ret = Integer.parseInt(item.replaceAll("[^0-9]", ""));

                // locationData, adapterに追加
                LocationData ld = new LocationData();
                ld.locationName = strLocation;
                ld.lat = Double.parseDouble(strLat);
                ld.lng = Double.parseDouble(strLng);
                ld.distance = ret;
                ld.valid = true;

                locationData.add(ld);
                adapter.add(ld);
                adapter.notifyDataSetChanged();

                UpdateLocationData();

            }
        });
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }

    private void updateDialog(final int position) {
        // カスタムビューを設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.location_dialog,
                (ViewGroup) findViewById(R.id.layout_root));

        // レイアウトのエディット,スピナーなどのid取得
        EditText setLocation = (EditText) layout.findViewById(R.id.customDlg_location);
        EditText setLat = (EditText) layout.findViewById(R.id.customDlg_lat);
        EditText setLng = (EditText) layout.findViewById(R.id.customDlg_lng);
        Spinner setSpinner = (Spinner) layout.findViewById(R.id.spinner);

        // 登録されている値をセット
        setLocation.setText(locationData.get(position).locationName);
        setLat.setText(String.valueOf(locationData.get(position).lat));
        setLng.setText(String.valueOf(locationData.get(position).lng));
        int ret = locationData.get(position).distance;
        setSpinner.setSelection(ret - 1);

        // アラートダイアログを生成
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("位置情報設定");
        builder.setView(layout);
        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                EditText location = (EditText) layout.findViewById(R.id.customDlg_location);
                EditText lat = (EditText) layout.findViewById(R.id.customDlg_lat);
                EditText lng = (EditText) layout.findViewById(R.id.customDlg_lng);
                Spinner spinner = (Spinner) layout.findViewById(R.id.spinner);

                if (location.getText().length() == 0 || lat.getText().length() == 0 || lng.getText().length() == 0) {
                    Toast.makeText(LocationList.this, "未記入の項目があります", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 入力された値の取得
                String strLocation = location.getText().toString();
                String strLat = lat.getText().toString();
                String strLng = lng.getText().toString();
                String item = (String) spinner.getSelectedItem();
                int ret = Integer.parseInt(item.replaceAll("[^0-9]", ""));

                // locationData, adapterを更新
                LocationData ld = locationData.get(position);
                ld.locationName = strLocation;
                ld.lat = Double.parseDouble(strLat);
                ld.lng = Double.parseDouble(strLng);
                ld.distance = ret;
                ld.valid = locationData.get(position).valid;

                adapter.notifyDataSetChanged();

                UpdateLocationData();

            }
        });
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }

    public void UpdateLocationData() {
        try {
            // シリアライズしてファイルに保存
            FileOutputStream outFile = openFileOutput("locationData.file", Context.MODE_PRIVATE);
            ObjectOutputStream outObject = new ObjectOutputStream(outFile);
            outObject.writeObject(locationData);
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

}