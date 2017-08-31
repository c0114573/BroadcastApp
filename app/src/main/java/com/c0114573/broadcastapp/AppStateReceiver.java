package com.c0114573.broadcastapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.c0114573.broadcastapp.MainActivity;

/**
 * Created by member on 2017/08/02.
 */

// 他のアプリがインストール、アンインストールされたことを検知し、AppDataクラスを更新させる
public class AppStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // アプリのインストール
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                && !intent.getExtras().getBoolean(Intent.EXTRA_REPLACING)) {
            Toast.makeText(context, "インストール", Toast.LENGTH_LONG).show();
            //MainActivity app = new MainActivity();
            //app.InitialSetting();

            int data1 = 10;

            Intent startServiceIntent = new Intent(context,AppDataSetting.class);
            startServiceIntent.putExtra("INSTALL",data1);
            context.startService(startServiceIntent);
        }

        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {

            // プリインアプリのアンインストール
            if (intent.getExtras().getBoolean(Intent.EXTRA_DATA_REMOVED)
                    && intent.getExtras().getBoolean(Intent.EXTRA_REPLACING)) {
                Toast.makeText(context, "プリインアプリのアンインストール", Toast.LENGTH_LONG).show();
            }

            // アプリの更新
            if (!intent.getExtras().getBoolean(Intent.EXTRA_DATA_REMOVED)
                    && intent.getExtras().getBoolean(Intent.EXTRA_REPLACING)) {
                Toast.makeText(context, "更新", Toast.LENGTH_LONG).show();
            }
        }

        // アプリのアンインストール
        if (Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(intent.getAction())) {
            Toast.makeText(context, "アンインストール", Toast.LENGTH_LONG).show();
        }

    }
}