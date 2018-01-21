package com.c0114573.broadcastapp;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

public class PermissionList extends Activity {

    // Switchボタンの設定
    Switch switchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_lsit);

        // 読み込み
        int num = 0;
        try {
            // AppDataの読み込み(デシリアライズ)
            FileInputStream inFile = openFileInput("appData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            final List<AppData> dataList2 = (ArrayList<AppData>) inObject.readObject();
            inObject.close();
            inFile.close();

            // アイコン情報を読み取り
            for (AppData appData : dataList2) {
                num++;
                // SharedPreferenceのインスタンスを生成
                SharedPreferences pref = getSharedPreferences("DATA" + num, Context.MODE_PRIVATE);
                String s = pref.getString("ICON", "");
                if (!s.equals("")) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    byte[] b = Base64.decode(s, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length).copy(Bitmap.Config.ARGB_8888, true);
                    // AppDataにアイコン情報を格納
                    appData.setIcon(new BitmapDrawable(bitmap));
                }
            }
            // AppDataをファイルに保存(シリアライズ)
            FileOutputStream outFile = openFileOutput("appData.file", Context.MODE_PRIVATE);
            ObjectOutputStream outObject = new ObjectOutputStream(outFile);
            outObject.writeObject(dataList2);
            outObject.close();
            outFile.close();
            num = 0;


            // リストビューにアプリケーションの一覧を表示する
            final ListView listView = new ListView(this);
            int padding = (int) (getResources().getDisplayMetrics().density * 8);
            listView.setPadding(padding, 0, padding, 0);
            listView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
            listView.setDivider(null);

            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View header = inflater.inflate(R.layout.list_header_footer, listView, false);

            listView.addHeaderView(header, null, false);
            listView.setAdapter(new AppListAdapter(this, dataList2));

            //クリック処理
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                ApplicationInfo item = applicationInfo.get(position);

                    // タップされたアプリ名取得
                    AppData item = dataList2.get(position - 1);
//                Log.d("onClick","VIEW_ID"+view.getId());
                    Log.i("PermissionList", "onClick" + dataList2.get(position - 1).getpackageName()+",s:"+dataList2.size()+",p:"+position);

                    // リスト表示用のアラートダイアログ
                    displayDialog(item.getpackageLabel(), item.getpackageName(), position-1);

                }
            });

            setContentView(listView);

        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // AppDataクラスのアプリラベルとアイコンを表示する独自アダプタークラス
    private  class AppListAdapter extends ArrayAdapter<AppData> {
        private final LayoutInflater mInflater;

        public AppListAdapter(Context context, List<AppData> dataList) {
            super(context, R.layout.activity_permission_lsit);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            addAll(dataList);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.activity_permission_lsit, parent, false);
                holder.textLabel = (TextView) convertView.findViewById(R.id.label);
                holder.imageIcon = (ImageView) convertView.findViewById(R.id.icon);
                holder.packageName = (TextView) convertView.findViewById(R.id.pname);
                holder.tSwitch = (Switch) convertView.findViewById(R.id.switch1);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // 表示データを取得
            final AppData data = getItem(position);
            // ラベルとアイコンをリストビューに設定
            holder.textLabel.setText(data.getpackageLabel());
            holder.imageIcon.setImageDrawable(data.icon);
//            holder.packageName.setText(data.getpackageName());
            holder.packageName.setText(data.getPermissionName());


            switchButton = (Switch) convertView.findViewById(R.id.switch1);
            switchButton.setTag(position);
            switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Log.d("TEST", "true" + position);
                        data.setLock(isChecked);

                        // positionは0から呼ばれた値が呼ばれる
//                        AppLockSwitch(position);

                    } else {
                        Log.d("TEST", "false" + position);
//                        AppLockSwitch(position);
                        data.setLock(isChecked);

                    }
//                    AppLockSwitch(PermissionList.this,position);
                }
            });

            holder.tSwitch.setChecked(data.getLock());

            return convertView;
        }
    }


    // ビューホルダー
    private static class ViewHolder {
        TextView textLabel;
        ImageView imageIcon;
        TextView packageName;
        Switch tSwitch;
    }

    private void displayDialog(final String name, final String text, final int position) {
        String lockText = "";
        boolean checked=true;
        try {
            // デシリアライズ
            FileInputStream inFile = openFileInput("appData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            List<AppData> dataList2 = (ArrayList<AppData>) inObject.readObject();
            inObject.close();
            inFile.close();

            if (dataList2.get(position).getLock()) {
                lockText = "制限解除";
                checked=false;

            } else {
                lockText = "制限登録";
                checked=true;
            }

        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle(name);

        String[] items = {"アプリを起動", "アプリ情報画面を開く", "GooglePlayストアを開く", "アンインストール", lockText};
        dlg.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = null;
                Uri uri;
                PackageManager pManager = getPackageManager();
                switch (which) {

                    case 0:
                        intent = pManager.getLaunchIntentForPackage(text);
                        startActivity(intent);
                        break;

                    case 1:
                        intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + text));
                        startActivity(intent);
                        break;

                    case 2:
                        uri = Uri.parse("market://details?id=" + text);
                        intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        break;

                    case 3:
                        uri = Uri.fromParts("package", text, null);
                        intent = new Intent(Intent.ACTION_DELETE, uri);
                        startActivity(intent);
                        break;

                    case 4:
                        AppLockSwitch(position);
                        Log.d("TEST", "dialog" + position);
//                        switchButton.setChecked(checked);
                        break;

                    default:
                        break;
                }
            }
        });
        dlg.show();
    }


    // appDataクラスのアプリ制限のon/offを切り替える
    public void AppLockSwitch(int position) {
        position+=1;
        try {
            // デシリアライズ
            FileInputStream inFile = openFileInput("appData.file");
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            List<AppData> dataList2 = (ArrayList<AppData>) inObject.readObject();
            inObject.close();
            inFile.close();

            if (dataList2.get(position - 1).getLock()) {
                dataList2.get(position - 1).setLock(false);
//                switchButton.setTag(position-1);
                switchButton.setChecked(false);

            } else {
                dataList2.get(position - 1).setLock(true);
//                switchButton.setTag(position-1);
                switchButton.setChecked(true);
            }

            // シリアライズしてファイルに保存
            FileOutputStream outFile = openFileOutput("appData.file", Context.MODE_PRIVATE);
            ObjectOutputStream outObject = new ObjectOutputStream(outFile);
            outObject.writeObject(dataList2);
            outObject.close();
            outFile.close();

        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // アクティビティ再起動
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);

    }
}