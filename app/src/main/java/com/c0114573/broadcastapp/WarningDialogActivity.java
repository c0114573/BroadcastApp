package com.c0114573.broadcastapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by member on 2017/07/26.
 */

public class WarningDialogActivity extends Activity {

    Intent it;
    String label = "no title";
    String TAG = "WarningDialogActivity";
    static boolean isDismiss = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        label = intent.getStringExtra("LABEL");
        it = getIntent();

        MessageDialog.show(this, "アプリ制限", "設定範囲内にいるためこのアプリは制限されます\n");

//        // サービスを起動isUsedを切り替える
//        Intent startServiceIntent = new Intent(getBaseContext(), AppDataSetting.class);
//        startServiceIntent.putExtra("UPDATE", 40);
//        startServiceIntent.putExtra("APPNAME", label);
//        getBaseContext().startService(startServiceIntent);

        Log.i(TAG,"ダイアログが終わった");

//        if(isDismiss) {
////            setService();
//            Log.i(TAG,"if(isDismiss)が終わった");
//
//        }else {
//            Log.i(TAG,"if(isDismiss)が呼ばれなかった");
//        }

/*
        final AlertDialog.Builder alert = new AlertDialog.Builder(this).setCancelable(false);
        alert.setTitle("アプリ制限");
        alert.setMessage("設定範囲内にいるためこのアプリは制限されます\n" +
                "GPSをOFFにするか制限の設定を行ってください\n");
        alert.setPositiveButton("ホーム画面へ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                ActivityManager activityManager = ((ActivityManager) getSystemService(ACTIVITY_SERVICE));
                activityManager.killBackgroundProcesses(label);

                Log.i(TAG, "onPositiveButton");

                // ダイアログを閉じる
                dialog.dismiss();

//                // アプリ使用状況の変更
//                Intent startServiceIntent = new Intent(getBaseContext(), AppDataSetting.class);
//                startServiceIntent.putExtra("UPDATE", 40);
//                startServiceIntent.putExtra("APPNAME", PackageName);
//                startService(startServiceIntent);

//                finish();

            }
        });
        // たまにエラー
        alert.show();


*/
    }


    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        Log.i(TAG,"onPause");

//        finish();

//        // ホームに戻る処理
//        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
//        homeIntent.addCategory(Intent.CATEGORY_HOME);
//        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(homeIntent);
    }

    //メッセージダイアログの定義(4)
    public static class MessageDialog extends DialogFragment {
        //ダイアログの表示(5)
        public static void show(
                Activity activity, String title, String text) {
            MessageDialog f = new MessageDialog();
            Bundle args = new Bundle();
            args.putString("title", title);
            args.putString("text", text);
            f.setArguments(args);
            f.show(activity.getFragmentManager(), "messageDialog");
        }

        //ダイアログの生成(6)
        @Override
        public Dialog onCreateDialog(Bundle bundle) {
            AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
            ad.setTitle(getArguments().getString("title"));
            ad.setMessage(getArguments().getString("text"));
            ad.setPositiveButton("ホーム画面へ", null);
            this.setCancelable(false);
            return ad.create();
        }

        // ダイアログが閉じられた時の処理
        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            Log.i("ButtonEx", "onDismiss dialog");

            onPause();
//            isDismiss = true;

            try {
                // ホームに戻る処理
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
            } catch (IllegalStateException e){
                Log.e("WarningDialogActivity", ""+e);
            }
        }
    }

//    public void setService() {

//    }
}