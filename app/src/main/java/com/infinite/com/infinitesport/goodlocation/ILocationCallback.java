package com.infinite.com.infinitesport.goodlocation;

import com.amap.api.location.AMapLocation;

public interface ILocationCallback {
    void onLocation(AMapLocation city_info);

    void onLocationError(int errcode);
}
