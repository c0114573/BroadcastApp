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

    public LocationData() {}

    public void setLocationName(String l){
        locationName=l;
    }

    public void setLatlung(float la,float ln){
        lat=la;
        lng=ln;
    }

    public String getLocationName(){
        return locationName;
    }

    public String getLatlng(){
        return lat+","+ lng;
    }
}