package com.c0114573.broadcastapp;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by member on 2017/07/26.
 */

public class WarningDialogActivity extends Activity {

    Intent it;
    String label = "no title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        label = intent.getStringExtra("LABEL");
        it = getIntent();

        final AlertDialog.Builder alert = new AlertDialog.Builder(this).setCancelable(false);
        alert.setTitle("アプリ制限");
        alert.setMessage("設定範囲内にいるためこのアプリは制限されます\n" +
                "GPSをOFFにするか制限の設定を行ってください\n");
        alert.setPositiveButton("ホーム画面へ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                ActivityManager activityManager = ((ActivityManager) getSystemService(ACTIVITY_SERVICE));
                activityManager.killBackgroundProcesses(label);

                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);

                finish();

            }
        });

        // たまにエラー
        alert.show();

    }

}