package com.c0114573.broadcastapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
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

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("アプリ制限");
        alert.setMessage("設定範囲内にいるため位置情報アプリは制限されます\n");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                ActivityManager activityManager = ((ActivityManager) getSystemService(ACTIVITY_SERVICE));
                activityManager.killBackgroundProcesses(label);

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