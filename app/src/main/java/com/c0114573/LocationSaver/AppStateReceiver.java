package com.c0114573.LocationSaver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by member on 2017/08/02.
 * ブロードキャストレシーバ
 * 新たにアプリがインストールされたりしたときに呼ばれる
 */

// 他のアプリがインストール、アンインストールされたことを検知し、AppDataクラスを更新させる
public class AppStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String str ="";
        str = intent.getData().toString();
        if(str.startsWith("package:")){
            str = str.substring(8);
        }

        // アプリのインストール
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                && !intent.getExtras().getBoolean(Intent.EXTRA_REPLACING)) {
//            Toast.makeText(context, str + "インストール", Toast.LENGTH_LONG).show();

            Intent startServiceIntent = new Intent(context,AppDataSetting.class);
            startServiceIntent.putExtra("INSTALL",20);
            startServiceIntent.putExtra("APPNAME",str);
            context.startService(startServiceIntent);
        }

        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {

            // プリインアプリのアンインストール
            if (intent.getExtras().getBoolean(Intent.EXTRA_DATA_REMOVED)
                    && intent.getExtras().getBoolean(Intent.EXTRA_REPLACING)) {
//                Toast.makeText(context, str + "プリインアンイン", Toast.LENGTH_LONG).show();

                Intent startServiceIntent = new Intent(context,AppDataSetting.class);
                startServiceIntent.putExtra("UNINSTALL",30);
                startServiceIntent.putExtra("APPNAME",str);
                context.startService(startServiceIntent);
            }

            // アプリの更新
            if (!intent.getExtras().getBoolean(Intent.EXTRA_DATA_REMOVED)
                    && intent.getExtras().getBoolean(Intent.EXTRA_REPLACING)) {
//                Toast.makeText(context, str + "更新", Toast.LENGTH_LONG).show();

                Intent startSeviceIntent = new Intent(context,AppDataSetting.class);
                startSeviceIntent.putExtra("UPDATE" ,40);
                startSeviceIntent.putExtra("APPNAME" ,str);
                context.startService(startSeviceIntent);
            }
        }

        // アプリのアンインストール
        if (Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(intent.getAction())) {
//            Toast.makeText(context, str + "アンインスト", Toast.LENGTH_LONG).show();

            Intent startServiceIntent = new Intent(context,AppDataSetting.class);
            startServiceIntent.putExtra("UNINSTALL",30);
            startServiceIntent.putExtra("APPNAME",str);
            context.startService(startServiceIntent);
        }

    }
}