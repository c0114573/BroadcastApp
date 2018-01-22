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

public class DialogWarningActivity extends Activity {

    Intent it;
    String label = "no title";
    String TAG = "DialogWarningActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        label = intent.getStringExtra("LABEL");
        it = getIntent();

//        MessageDialog.show(this, "アプリ制限", "設定範囲内にいるためこのアプリは制限されます\n");

        final AlertDialog.Builder alert = new AlertDialog.Builder(this).setCancelable(false);
        alert.setTitle("アプリ制限");
        alert.setMessage("設定範囲内にいるためこのアプリは制限されます");

        alert.setPositiveButton("ホーム画面へ", new DialogInterface.OnClickListener() {
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
        });
        alert.show();

    }


    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        Log.i(TAG, "onPause");

//        finish();

//        // ホームに戻る処理
//        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
//        homeIntent.addCategory(Intent.CATEGORY_HOME);
//        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(homeIntent);
    }

//    //メッセージダイアログの定義(4)
//    public static class MessageDialog extends DialogFragment {
//        //ダイアログの表示(5)
//        public static void show(
//                Activity activity, String title, String text) {
//            MessageDialog f = new MessageDialog();
//            Bundle args = new Bundle();
//            args.putString("title", title);
//            args.putString("text", text);
//            f.setArguments(args);
//            f.show(activity.getFragmentManager(), "messageDialog");
//        }
//
//        //ダイアログの生成(6)
//        @Override
//        public Dialog onCreateDialog(Bundle bundle) {
//            AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
//            ad.setTitle(getArguments().getString("title"));
//            ad.setMessage(getArguments().getString("text"));
//            ad.setPositiveButton("ホーム画面へ", null);
//            this.setCancelable(false);
//            return ad.create();
//        }


}