package com.c0114573.broadcastapp;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PermissionList extends Activity {

    AppData data = new AppData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_lsit);

//        TextView tv = (TextView) findViewById(R.id.textView2);


        // リスト作成
        ArrayList<String> appList = new ArrayList<String>();
        // パッケージマネージャーの作成
        PackageManager packageManager = getPackageManager();
        // インストール済みのアプリの情報を取得
        final List<ApplicationInfo> applicationInfo = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);


        // アイコンあり
        final List<AppData> dataList = new ArrayList<AppData>();

        // リストに一覧データを格納する
        for (ApplicationInfo info : applicationInfo) {

            int p = getPackageManager().checkPermission(Manifest.permission.CAMERA, info.packageName);
            if(p == PackageManager.PERMISSION_GRANTED) {

                if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)
                    continue;
                if (info.packageName.equals(this.getPackageName())) continue;

                // インストール済みアプリ情報からアプリ名を取得しリストに追加
                appList.add((String) packageManager.getApplicationLabel(info));
//            appList.add(info.loadLabel(packageManager).toString());


                AppData data = new AppData();
                data.label = info.loadLabel(packageManager).toString();
                data.icon = info.loadIcon(packageManager);
                data.pname = info.packageName;
                dataList.add(data);

            }
        }

        /*
        // リストの表示
        for (String app : appList) {
            appname += app + "\n";
        }
        tv.setText(appname);
        */




        // リストビューで表示

//        lv = (ListView)findViewById(R.id.listView);
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
//        // リストの表示
//        for (String app : appList) {
//            adapter.add(app);
//        }
//        lv.setAdapter(adapter);



        // リストビューにアプリケーションの一覧を表示する
        final ListView listView = new ListView(this);
        listView.setAdapter(new AppListAdapter(this, dataList));
        //クリック処理
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                ApplicationInfo item = applicationInfo.get(position);

                // タップされたアプリ名取得
                AppData item = dataList.get(position);
                Log.d("onClick",item.getName());

                // アプリを開く
//                PackageManager pManager = getPackageManager();
//                Intent intent = pManager.getLaunchIntentForPackage(item.getName());
//                startActivity(intent);


                // アプリをアンインストール
                PackageManager pManager = getPackageManager();
                Uri uri = Uri.fromParts("package", item.getName(), null);
                Intent intent = new Intent(Intent.ACTION_DELETE, uri);
                startActivity(intent);







            }
        });
        setContentView(listView);



    }


    // アプリケーションデータ格納クラス
    private static class AppData {
        String label;
        Drawable icon;
        String pname;

        public String getName(){
            return this.pname;
        }
    }


    // アプリケーションのラベルとアイコンを表示するためのアダプタークラス
    private static class AppListAdapter extends ArrayAdapter<AppData> {

        private final LayoutInflater mInflater;

        public AppListAdapter(Context context, List<AppData> dataList) {
            super(context, R.layout.activity_permission_lsit);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            addAll(dataList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = new ViewHolder();

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_permission_lsit, parent, false);
                holder.textLabel = (TextView) convertView.findViewById(R.id.label);
                holder.imageIcon = (ImageView) convertView.findViewById(R.id.icon);
                holder.packageName = (TextView) convertView.findViewById(R.id.pname);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // 表示データを取得
            final AppData data = getItem(position);
            // ラベルとアイコンをリストビューに設定
            holder.textLabel.setText(data.label);
            holder.imageIcon.setImageDrawable(data.icon);
            holder.packageName.setText(data.pname);

            return convertView;
        }
    }

    // ビューホルダー
    private static class ViewHolder {
        TextView textLabel;
        ImageView imageIcon;
        TextView packageName;
    }

}