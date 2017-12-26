package com.c0114573.broadcastapp;

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
import android.provider.Settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by member on 2017/11/10.
 */

public class LocationDialogActivity extends Activity {

    Intent it;
    String label = "no title";

    @Override
    @TargetApi(21)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityManager activityManager = ((ActivityManager) getSystemService(ACTIVITY_SERVICE));

        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        int intTime = UsageStatsManager.INTERVAL_BEST;
        Long end = System.currentTimeMillis(); // 現在の時間を取得 (ミリ秒)
        Long start = end - (1 * 24 * 60 * 60 * 1000); // 現在時刻から1日前の時間(ミリ秒)を引く　1日 = 24*60*60*1000ミリ秒
//        Long start = end - (5 * 60 * 1000); // 5分
//        Long start = end - (5000); // 5秒

        List<UsageStats> usl = usm.queryUsageStats(intTime, start, end); //

        //cslはアプリごとではないため使えない
//        List<ConfigurationStats> csl = usm.queryConfigurations(intTime, start, end);


        List<AppData> dataList = new ArrayList<AppData>();
        // AppData読み込み (デシリアライズ)
        try {
            //FileInputStream inFile = new FileInputStream(FILE_NAME);
            FileInputStream inFile = openFileInput("appData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            dataList = (ArrayList<AppData>) inObject.readObject();
            inObject.close();
            inFile.close();


            for (int i = 0; i < usl.size(); i++) {
//        for (int i = 0; i < csl.size(); i++) {
                UsageStats us = usl.get(i);
//            ConfigurationStats cs = csl.get(i);

                // 使用時間が0でない
                if (us.getTotalTimeInForeground() != 0) {
                    // uslのend時間より後にアプリを使用した場合
                    // 新たにそのアプリのusが作成される(上書きされない)
                    // よって一覧に同じアプリ名が複数存在することがある(getLastTimeStampは異なる)

                    // そのためこの部分で使われたアプリの抽出を行う
                    if (us.getLastTimeUsed() > end - (60 * 60 * 1000)) {// 1時間

                        // リストに一覧データを格納する
//                for (int m = 0; m < appList.size(); m++) {

                        for (AppData info : dataList) {
//                            ApplicationInfo app = appList.get(m);

                            // インストール済みアプリであるか
                            if (info.packageName.equals(us.getPackageName())) {
//                        array.add((n + 1) + "個目のアプリケーション");
                                activityManager.killBackgroundProcesses(info.packageName);

                            }

                        }

                    }
                }
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

        Intent intent = getIntent();
        label = intent.getStringExtra("LABEL");
        it = getIntent();

        final AlertDialog.Builder alert = new AlertDialog.Builder(this).setCancelable(false);
        alert.setTitle("アプリ制限");
        alert.setMessage("設定範囲内に入ったよ\n");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                ActivityManager activityManager = ((ActivityManager) getSystemService(ACTIVITY_SERVICE));
                activityManager.killBackgroundProcesses(label);

                // ホームに戻る処理
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);

                finish();

            }
        });
        alert.setNegativeButton("GPS設定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Noボタンが押された時の処理
//                Toast.makeText(MainActivity.this, "No Clicked!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

                finish();

            }
        });
        alert.show();

    }

}