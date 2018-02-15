package com.c0114573.LocationSaver;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by member on 2017/11/10.
 * 範囲内通知ダイアログを表示する
 */

public class DialogInRangeActivity extends Activity {

    String[] array;

    List<String> items = new ArrayList<>();

    ArrayList<Integer> checkedItems = new ArrayList<Integer>();

    @Override
    @TargetApi(21)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        int intTime = UsageStatsManager.INTERVAL_BEST;
        Long end = System.currentTimeMillis(); // 現在の時間を取得 (ミリ秒)
        Long start = end - (1 * 24 * 60 * 60 * 1000); // 現在時刻から1日前の時間(ミリ秒)を引く　1日 = 24*60*60*1000ミリ秒

        List<UsageStats> usl = usm.queryUsageStats(intTime, start, end); //

        List<AppData> dataList = new ArrayList<AppData>();
        // AppData読み込み (デシリアライズ)
        try {
            FileInputStream inFile = openFileInput("appData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            dataList = (ArrayList<AppData>) inObject.readObject();
            inObject.close();
            inFile.close();

            for (int i = 0; i < usl.size(); i++) {
                UsageStats us = usl.get(i);

                // 使用時間が0でない
                if (us.getTotalTimeInForeground() != 0) {
                    // そのためこの部分で使われたアプリの抽出を行う
                    if (us.getLastTimeUsed() > end - (3 * 60 * 60 * 1000)) {// 12時間

                        // リストに一覧データを格納する
                        for (AppData info : dataList) {
                            // インストール済みアプリであるか
                            if (info.packageName.equals(us.getPackageName())) {

                                if(info.pReceive==0) {
                                    items.add(info.getpackageLabel());
                                }
                            }
                        }
                    }
                }
            }
            array = (String[]) items.toArray(new String[0]);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        final AlertDialog.Builder alert = new AlertDialog.Builder(this).setCancelable(false);
        alert.setTitle("アプリ制限");
        alert.setMessage("制限範囲内に入りました\n範囲内では一部のアプリの使用が制限されます");

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ChoiceDialog();
            }
        });
        try {
            alert.show();
        }catch (Exception e){
            Log.e("DialogStart_alert_err",""+e);
        }
    }

    public void ChoiceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("終了したいアプリを選択")
                .setMultiChoiceItems(array, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) checkedItems.add(which);
                        else checkedItems.remove((Integer) which);
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (Integer i : checkedItems) {
                            // item_i checked

                            Log.i("選択されたやつ", "" + array[i]);
                            ActivityManager activityManager = ((ActivityManager) getSystemService(ACTIVITY_SERVICE));
                            activityManager.killBackgroundProcesses(array[i]);
                        }
                        try {
                            // ホームに戻る処理
                            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                            homeIntent.addCategory(Intent.CATEGORY_HOME);
                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(homeIntent);
                        } catch (IllegalStateException e) {

                        }
                        finishAndRemoveTask();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    // ホームに戻る処理
                                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                                    homeIntent.addCategory(Intent.CATEGORY_HOME);
                                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(homeIntent);
                                } catch (IllegalStateException e) {

                                }
                                finishAndRemoveTask();
                            }
                        }
                ).show();
    }
}