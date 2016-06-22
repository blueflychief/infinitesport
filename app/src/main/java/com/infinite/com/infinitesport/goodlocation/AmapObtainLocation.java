package com.infinite.com.infinitesport.goodlocation;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.infinite.com.infinitesport.MyApplication;
import com.infinite.com.infinitesport.util.MyLogger;
import com.infinite.com.infinitesport.util.NetworkUtils;

public class AmapObtainLocation implements AMapLocationListener {
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private ILocationCallback mLocationCallback = null;

    private AmapObtainLocation() {

    }

    public static AmapObtainLocation getLocation() {
        return AmapObtainLocationHolder.INSTANCE;
    }

    private static class AmapObtainLocationHolder{
        private static final AmapObtainLocation INSTANCE=new AmapObtainLocation();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {
//                locationClient.stopLocation();
                if (mLocationCallback != null) {
                    mLocationCallback.onLocation(aMapLocation);
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
        initOption();
        mLocationCallback = callback;
        locationClient.startLocation();
    }


    public void stopLocation() {
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient = null;
        }
        locationOption = null;
        MyLogger.i("------停止高德定位");
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
        if (NetworkUtils.isNetConnect()) {
            locationOption.setGpsFirst(false);
            MyLogger.i("------网络已连接，启用网络定位");
        } else {
            MyLogger.i("------网络未连接，启用GPS定位");
            locationOption.setGpsFirst(true);
        }

        // 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
        locationOption.setInterval(5000);

        if (locationClient == null) {
            locationClient = new AMapLocationClient(MyApplication.getInstance());
        }
        locationClient.setLocationOption(locationOption);
        locationClient.setLocationListener(this);

    }
}
