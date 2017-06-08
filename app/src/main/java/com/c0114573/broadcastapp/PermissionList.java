package com.c0114573.broadcastapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by member on 2017/06/07.
 */

public class PermissionList extends Activity {

    private String appname ="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//
        setContentView(R.layout.activity_permission_lsit);

        TextView tv = (TextView) findViewById(R.id.permissionView);

        // リスト作成
        ArrayList<String> appList = new ArrayList<String>();
        // パッケージマネージャーの作成
        PackageManager packageManager = getPackageManager();
        // インストール済みのアプリの情報を取得
        List<ApplicationInfo> applicationInfo = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo info : applicationInfo) {

            int p = getPackageManager().checkPermission(Manifest.permission.CAMERA, info.packageName);
            if(p == PackageManager.PERMISSION_GRANTED) {

                if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)
                    continue;
                if (info.packageName.equals(this.getPackageName())) continue;

                // インストール済みアプリ情報からアプリ名を取得しリストに追加
                appList.add((String) packageManager.getApplicationLabel(info));
//            appList.add(info.loadLabel(packageManager).toString());
            }
        }

        // リストの表示
        for (String app : appList) {
            appname += app + "\n";

        }
        tv.setText(appname);

    }

}


