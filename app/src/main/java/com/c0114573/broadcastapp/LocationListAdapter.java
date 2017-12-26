package com.c0114573.broadcastapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

// LocationDataクラスのデータを表示する独自アダプタ
public class LocationListAdapter extends ArrayAdapter<LocationData> {
    LayoutInflater mInflater;

    public LocationListAdapter(Context context) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_list_item_card, parent, false);
        }

        LocationData locationData = getItem(position);

        TextView tv = (TextView) convertView.findViewById(R.id.title);
        tv.setTextColor(Color.GRAY);
        tv.setText(locationData.getLocationName());

        tv = (TextView) convertView.findViewById(R.id.sub);
        tv.setText(""+"登録位置 : ("+locationData.getLatlng()+")\n設定範囲 : "+locationData.distance+"km");

        return convertView;
    }
}
