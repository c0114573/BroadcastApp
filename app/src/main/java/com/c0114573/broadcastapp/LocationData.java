package com.c0114573.broadcastapp;

import java.io.Serializable;

/**
 * Created by member on 2017/09/29.
 */

public class LocationData implements Serializable {
    String locationName;
    double lat;
    double lng;
    int distance;
    boolean valid;

    public LocationData() {
    }

    public void setLocationName(String l) {
        locationName = l;
    }

    public void setLatlung(float la, float ln) {
        lat = la;
        lng = ln;
    }

    public void setDistance(int d) {
        distance = d;
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

    public String getNotValidText() {
        if(valid){
            return "無効にする";
        }else {
            return "有効にする";
        }
    }
}