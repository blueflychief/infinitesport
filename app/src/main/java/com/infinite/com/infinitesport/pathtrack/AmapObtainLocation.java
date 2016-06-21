package com.infinite.com.infinitesport.pathtrack;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.infinite.com.infinitesport.MyApplication;
import com.infinite.com.infinitesport.util.MyLogger;

import java.text.SimpleDateFormat;

public class AmapObtainLocation implements AMapLocationListener {
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private ILocationCallback mLocationCallback = null;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static AmapObtainLocation sLocation = null;

    private AmapObtainLocation() {
        initOption();

    }

    public static AmapObtainLocation getLocation() {
        if (sLocation == null) {
            sLocation = new AmapObtainLocation();
        }
        return sLocation;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {
                AmapCityInfo cityInfo = new AmapCityInfo();
                long time_stamp = aMapLocation.getTime();
                MyLogger.i("-------time"+time_stamp);
                MyLogger.i("-------Bearing"+aMapLocation.getBearing());
                MyLogger.i("-------Satellites"+aMapLocation.getSatellites());
                MyLogger.i("-------Speed"+aMapLocation.getSpeed());
                cityInfo.setTime_stamp(time_stamp);
                cityInfo.setTime(sdf.format(time_stamp));
                cityInfo.setCity(aMapLocation.getCity());
                cityInfo.setCity_code(aMapLocation.getCityCode());
                cityInfo.setProvince(aMapLocation.getProvince());
                cityInfo.setAddress(aMapLocation.getAddress());
                cityInfo.setGeoLat(aMapLocation.getLatitude());
                cityInfo.setGeoLng(aMapLocation.getLongitude());
//                locationClient.stopLocation();
                if (mLocationCallback != null) {
                    mLocationCallback.onLocation(cityInfo);
                }
            } else {
//                locationClient.stopLocation();
                if (mLocationCallback != null) {
                    mLocationCallback.onLocationError(aMapLocation.getErrorCode());
                }
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
        super.finalize();
    }

    public void startLocation(ILocationCallback callback) {
        mLocationCallback = callback;
        initOption();
        locationClient.startLocation();
    }


    public void stopLocation() {
        locationClient.stopLocation();
        if (locationClient != null) {
            locationClient = null;
        }
        locationOption = null;
    }


    private void initOption() {
        if (locationOption == null) {
            locationOption = new AMapLocationClientOption();
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        }

        // 设置是否需要显示地址信息
        locationOption.setNeedAddress(true);
        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        locationOption.setGpsFirst(true);
        // 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
        locationOption.setInterval(5000);

        if (locationClient == null) {
            locationClient = new AMapLocationClient(MyApplication.getInstance());
        }
        locationClient.setLocationOption(locationOption);
        locationClient.setLocationListener(this);

    }
}
