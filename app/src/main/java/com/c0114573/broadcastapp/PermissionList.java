package com.c0114573.broadcastapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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
 * Created by C011457331 on 2018/01/21.
 */

public class PermissionList extends Activity implements AdapterView.OnItemClickListener {

    static final int MY_INTENT_BROWSER = 0;
    List<AppData> appData = new ArrayList<AppData>();

    PermissionListAdapter adapter;

    ListView listView;

    int uninstallNum = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissionlist);

        // 独自のadapterを利用
        adapter = new PermissionListAdapter(getApplicationContext());

        // ListViewにadapterを設定
        listView = (ListView) findViewById(R.id.permission_card_list);
        int padding = (int) (getResources().getDisplayMetrics().density * 8);
        listView.setPadding(padding, 0, padding, 0);
        listView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
        listView.setDivider(null);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        try {
            // デシリアライズ(読み込み)
            FileInputStream inFile = openFileInput("appData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            appData = (ArrayList<AppData>) inObject.readObject();
            inObject.close();
            inFile.close();

            int num=0;
            // アイコン情報を読み取り
            for (AppData ad : appData) {
                num++;
                // SharedPreferenceのインスタンスを生成
                SharedPreferences pref = getSharedPreferences("DATA" + num, Context.MODE_PRIVATE);
                String s = pref.getString("ICON", "");
                if (!s.equals("")) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    byte[] b = Base64.decode(s, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length).copy(Bitmap.Config.ARGB_8888, true);
                    // AppDataにアイコン情報を格納
                    ad.setIcon(new BitmapDrawable(bitmap));
                }
            }

            // 登録内容をadapterに追加
            for (AppData ad : appData) {
                adapter.add(ad);
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


    @Override
    public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {

        String title = appData.get(position).packageLabel;
        String lockText = appData.get(position).getSelectText();

        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle(title);
        String[] items = {"アプリを起動", "アプリ情報画面を開く", "GooglePlayストアを開く", "アンインストール", lockText};

        dlg.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = null;
                Uri uri;
                PackageManager pManager = getPackageManager();
                String appName = appData.get(position).packageName;
                switch (which) {
                    case 0:
                        intent = pManager.getLaunchIntentForPackage(appName);
                        startActivity(intent);
                        break;

                    case 1:
                        intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + appName));
                        startActivity(intent);
                        break;

                    case 2:
                        uri = Uri.parse("market://details?id=" + appName);
                        intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        break;

                    case 3:
                        uri = Uri.fromParts("package", appName, null);
                        intent = new Intent(Intent.ACTION_DELETE, uri);
                        startActivity(intent);
//                        startActivityForResult(intent, MY_INTENT_BROWSER);
                        break;

                    case 4:
                        AppLockSwitch(position);
                        Log.d("TEST", "dialog" + position);
//                        switchButton.setChecked(checked);
                        break;

                    default:
                        break;
                }
            }
        });
        dlg.show();
    }


    // appDataクラスのアプリ制限のon/offを切り替える
    public void AppLockSwitch(int position) {
        if (appData.get(position).lock) {
            appData.get(position).setLock(false);

        } else {
            appData.get(position).setLock(true);
        }
        adapter.notifyDataSetChanged();

        UpdateAppData();
    }

    public void UpdateAppData() {
        try {
            // シリアライズしてファイルに保存
            FileOutputStream outFile = openFileOutput("appData.file", Context.MODE_PRIVATE);
            ObjectOutputStream outObject = new ObjectOutputStream(outFile);
            outObject.writeObject(appData);
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

    // アンインストール成功の判定はこれだとできない
    // アンインストールの完了(5秒くらい?)を待ってから再び一覧を取得し存在してなければ消すようにする?
    // BroadcastIntentから行う?
//    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        // 返り値の取得
//        if (requestCode == MY_INTENT_BROWSER) {
//            if (resultCode == RESULT_OK) {
//                // Success
//                Log.d("IntentSample", "success");
//                if(uninstallNum > -1) {
//                    AppData ad = appData.get(uninstallNum);
//
//                    // 選択されたアイテムをリストから削除
//                    adapter.remove(ad);
//                    adapter.notifyDataSetChanged();
//                }
//                uninstallNum = -1;
//            } else if (resultCode == RESULT_CANCELED) {
//                // Handle cancel
//                Log.d("IntentSample", "canceled");
//
//            }
//        }
//    }

}