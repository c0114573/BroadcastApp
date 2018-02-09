package com.c0114573.broadcastapp;

import java.io.Serializable;

/**
 * Created by member on 2017/09/29.
 * 登録位置情報を保存するクラス
 */

public class LocationData implements Serializable {
    String locationName;
    double lat;
    double lng;
    int distance;
    boolean valid;

    public LocationData() {
    }

    public void setValid(boolean val) {
        valid = val;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getLatlng() {
        return lat + "," + lng;
    }

    public String getValidText() {
        if(valid){
            return "有効";
        }else {
            return "無効";
        }
    }

    public String selectValidText() {
        if(valid){
            return "無効にする";
        }else {
            return "有効にする";
        }
    }
}