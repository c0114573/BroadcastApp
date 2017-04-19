package com.c0114573.broadcastapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Toast.makeText(context, "BOOT_COMPLETED", Toast.LENGTH_LONG).show();
            context.startService(new Intent(context, ExampleService.class));
        }else{
            Toast.makeText(context,action, Toast.LENGTH_LONG).show();
        }
    }

}