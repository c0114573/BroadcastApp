package com.c0114573.broadcastapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by C011457331 on 2018/01/21.
 */

// AppDataクラスのアプリラベルとアイコンを表示する独自アダプタ
public class PermissionListAdapter extends ArrayAdapter<AppData> {
    LayoutInflater mInflater;

    public PermissionListAdapter(Context context) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
//        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        addAll(dataList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_permissionlist_item_card, parent, false);
            holder.textLabel = (TextView) convertView.findViewById(R.id.label);
            holder.imageIcon = (ImageView) convertView.findViewById(R.id.icon);
            holder.packageName = (TextView) convertView.findViewById(R.id.pname);
            holder.lockText = (TextView) convertView.findViewById(R.id.locktext);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 表示データを取得
        AppData appData = getItem(position);

        // ラベルとアイコンをリストビューに設定
        holder.textLabel.setText(appData.getpackageLabel());
        holder.imageIcon.setImageDrawable(appData.icon);
//            holder.packageName.setText(data.getpackageName());
        holder.packageName.setText(appData.getPermissionName());
        if(appData.lock) {
            holder.lockText.setTextColor(Color.RED);
            holder.lockText.setText("\n制限中");
        } else {
            holder.lockText.setTextColor(Color.GRAY);
            holder.lockText.setText("\n解除中");
        }

//        holder.tSwitch.setChecked(appData.getLock());
        return convertView;
    }

    // ビューホルダー
    private static class ViewHolder {
        TextView textLabel;
        ImageView imageIcon;
        TextView packageName;
        TextView lockText;
//        Switch tSwitch;
    }

}
