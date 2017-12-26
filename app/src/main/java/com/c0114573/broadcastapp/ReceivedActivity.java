package com.c0114573.broadcastapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by member on 2017/10/27.
 */

public class ReceivedActivity extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context, "called ReceivedActivity", Toast.LENGTH_SHORT).show();

        if (intent != null) {
            String appName = intent.getStringExtra("APPNAME");

            Intent startServiceIntent = new Intent(context, AppDataSetting.class);
            startServiceIntent.putExtra("UPDATE", 40);
            startServiceIntent.putExtra("APPNAME", appName);
            context.startService(startServiceIntent);

            Toast.makeText(context, "change"+appName, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(context, "ないよ", Toast.LENGTH_SHORT).show();
        }

    }
}
