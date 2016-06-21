package com.infinite.com.infinitesport.pathtrack;

/**
 * Created by guoning on 16-2-17.
 */
public interface ILocationCallback {
    void onLocation(AmapCityInfo city_info);

    void onLocationError(int errcode);
}
