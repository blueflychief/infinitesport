package com.infinite.com.infinitesport.goodlocation;

import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.infinite.com.infinitesport.MyApplication;
import com.infinite.com.infinitesport.R;
import com.infinite.com.infinitesport.util.MyLogger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2016-06-21.
 */
public class GoodLocationActivity extends AppCompatActivity implements View.OnClickListener, SimpleGpsLocation.PositionListener, ILocationCallback, LocationSource {

    private static final String TAG = "GoodLocationActivity";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final int MARK_MOVE_INTERVAL = 80;  //移动间隔
    private Button bt_test;
    private Button bt_real;
    private Button bt_start_amap;
    private Button bt_stop_amap;
    private Button bt_start_gps;
    private Button bt_stop_gps;
    private MapView mMapView = null;
    private AMap mAmap = null;
    private SimpleGpsLocation mSimpleLocation;
    //行驶轨迹
    private List<LatLng> mMoveTrack;
    private File mTmpfile;

    private Marker mMoveMarker;
    private MarkerOptions markerOptions;
    private LocationSource.OnLocationChangedListener mOnLocationChangedListener = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_location);

        mMapView = (MapView) findViewById(R.id.map);
        bt_test = (Button) findViewById(R.id.bt_test);
        bt_real = (Button) findViewById(R.id.bt_real);
        bt_start_amap = (Button) findViewById(R.id.bt_start_amap);
        bt_stop_amap = (Button) findViewById(R.id.bt_stop_amap);
        bt_start_gps = (Button) findViewById(R.id.bt_start_gps);
        bt_stop_gps = (Button) findViewById(R.id.bt_stop_gps);
        bt_test.setOnClickListener(this);
        bt_real.setOnClickListener(this);
        bt_start_amap.setOnClickListener(this);
        bt_stop_amap.setOnClickListener(this);
        bt_start_gps.setOnClickListener(this);
        bt_stop_gps.setOnClickListener(this);
        initAMap(savedInstanceState);
        initData();

        //初始化高德定位
        initLocationServer();
        //初始化GPS定位
        mSimpleLocation = new SimpleGpsLocation(MyApplication.getInstance(), true, true, 0, true);

    }

    //
    private void initLocationServer() {
        mAmap.setLocationSource(this);// 设置定位监听，这句不写无法点击定位和显示小蓝点
        mAmap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        mAmap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式：
        // 定位（AMap.LOCATION_TYPE_LOCATE）、
        // 跟随（AMap.LOCATION_TYPE_MAP_FOLLOW）
        // 地图根据面向方向旋转（AMap.LOCATION_TYPE_MAP_ROTATE）三种模式
        mAmap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
    }


    private void initAMap(Bundle savedInstanceState) {
        mMapView.onCreate(savedInstanceState);
        if (mAmap == null) {
            mAmap = mMapView.getMap();
        }
    }

    private void initData() {
        mMoveTrack = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        mTmpfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "mypath", dateFormat.format(new Date()) + "_path.txt");
        Log.i(TAG, "------mTmpfile: " + mTmpfile);
        if (!mTmpfile.getParentFile().exists()) {
            mTmpfile.getParentFile().mkdir();
        }
        try {
            mTmpfile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "------mTmpfile: " + mTmpfile.exists());
    }


    private void moveToCurrent(double lat, double lon, int rank) {
        CameraUpdate cu = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(lat, lon), rank, 0, 0));
        mAmap.animateCamera(cu);
    }


    private void whiterSchedule(File file, final String s) {
        //启动一个线程每5秒钟向日志文件写一次数据
        Executor exec = Executors.newScheduledThreadPool(1);
        exec.execute(new WriteThread(file, s));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //停止GPS定位
        mSimpleLocation.endUpdates();

        //停止高德定位
        AmapObtainLocation.getLocation().stopLocation();
    }


    private void drawTrack(List<TrackPoint> mLatLngs) {
        if (mLatLngs == null) {
            MyLogger.i("------轨迹数据为空");
            return;
        }
        List<LatLng> all = new ArrayList<>();
        for (TrackPoint point : mLatLngs) {
            all.add(new LatLng(point.getLat(), point.getLng()));
        }
        mAmap.addPolyline(new PolylineOptions()
                .colorValues(CaculateUtil.caculateSpeedColor(mLatLngs))
                .addAll(all)
                .useGradient(true)
                .width(10));
        if (all.size() > 0) {
            moveToCurrent(all.get(0).latitude, all.get(0).longitude, 16);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_test:
                drawTrack(FileUtil.getAllTrackPoint(FileUtil.readLogFromString(CaculateUtil.testdata)));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        List<LatLng> latLngs = FileUtil.getAllLatLng(FileUtil.readLogFromString(CaculateUtil.testdata));
                        if (latLngs != null && latLngs.size() > 0) {
                            initMoveMarker(CaculateUtil.getPolylineOptions(latLngs), latLngs.get(0), 0.0f);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(GoodLocationActivity.this, "没有数据", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }, 2000);
                break;
            case R.id.bt_real:
                drawTrack(FileUtil.getAllTrackPoint(FileUtil.readLog(mTmpfile)));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        List<LatLng> latLngs = FileUtil.getAllLatLng(FileUtil.readLog(mTmpfile));
                        if (latLngs != null && latLngs.size() > 0) {
                            initMoveMarker(CaculateUtil.getPolylineOptions(latLngs), latLngs.get(0), 0.0f);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(GoodLocationActivity.this, "没有数据", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }, 2000);
                break;
            case R.id.bt_start_amap:
                mSimpleLocation.endUpdates();
                AmapObtainLocation.getLocation().startLocation(this);
                break;
            case R.id.bt_stop_amap:
                AmapObtainLocation.getLocation().stopLocation();
                break;
            case R.id.bt_start_gps:
                AmapObtainLocation.getLocation().stopLocation();
                mSimpleLocation.setPositionListener(this);
                mSimpleLocation.beginUpdates();
                break;
            case R.id.bt_stop_gps:
                //停止GPS定位
                mSimpleLocation.endUpdates();
                break;
        }
    }

    @Override
    public void onPositionChanged(Location location) {
        MyLogger.i("---------GPS定位成功");
        printLocationInfo(0, location);
    }

    /**
     * @param type 0:GPS    1:高德
     * @param obj
     */
    private void printLocationInfo(int type, Object obj) {
        if (obj != null) {
            Location _obj = (Location) obj;
            if (type == 0) {
                //此处是GPS原始坐标，需要转换成高德坐标
                LatLng latLng = new LatLng(mSimpleLocation.getLatitude(), mSimpleLocation.getLongitude());
                CoordinateConverter coordinateConverter = new CoordinateConverter(this);
                LatLng _latLng = coordinateConverter.coord(latLng).from(CoordinateConverter.CoordType.GPS).convert();
                _obj.setLatitude(_latLng.latitude);
                _obj.setLongitude(_latLng.longitude);
//            moveToCurrent(GeoLatLngTransformUtil.transformFromWGSToGCJ(latLng).latitude, GeoLatLngTransformUtil.transformFromWGSToGCJ(latLng).longitude, 18);
            }
            moveToCurrent(_obj.getLatitude(), _obj.getLongitude(), 18);
            MyLogger.i("---------纬度" + _obj.getLatitude());
            MyLogger.i("---------经度" + _obj.getLongitude());
            MyLogger.i("---------时间" + _obj.getTime());
            MyLogger.i("---------时间" + sdf.format(_obj.getTime()));
            MyLogger.i("---------精度" + _obj.getAccuracy());
            MyLogger.i("---------海拔" + _obj.getAltitude());
            MyLogger.i("---------方向" + _obj.getBearing());
            String s = _obj.getTime() + "#"
                    + _obj.getLatitude() + "#"
                    + _obj.getLongitude() + "#"
                    + _obj.getAltitude() + "#"
                    + _obj.getBearing() + "#"
                    + _obj.getAccuracy();
            whiterSchedule(mTmpfile, s);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();

    }

    @Override
    public void onLocation(AMapLocation amapLocation) {
        MyLogger.i("---------高德定位成功：");
        mOnLocationChangedListener.onLocationChanged(amapLocation);// 显示系统小蓝点
        printLocationInfo(1, amapLocation);
    }

    @Override
    public void onLocationError(int errcode) {
        MyLogger.i("---------高德定位失败，错误码：" + errcode);
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mOnLocationChangedListener = listener;
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mOnLocationChangedListener = null;
    }


    /**
     * 播放运动轨迹
     */
    private void initMoveMarker(PolylineOptions polylineOptions, LatLng latLng, float b) {
        Polyline line = mAmap.addPolyline(polylineOptions);
        if (markerOptions == null) {
            markerOptions = new MarkerOptions();
            markerOptions.setFlat(true)
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker))
                    .position(latLng);
            mMoveMarker = mAmap.addMarker(markerOptions);
        }
        mMoveMarker.setPosition(latLng);
        mMoveMarker.setRotateAngle(b);
        moveLooper(line);
    }

    /**
     * 循环进行移动
     */
    public void moveLooper(final Polyline line) {
        if (line.getPoints().size() > 0) {
            moveToCurrent(line.getPoints().get(0).latitude, line.getPoints().get(0).longitude, 17);
        }
        new Thread() {
            public void run() {
                while (true) {
                    for (int i = 0; i < line.getPoints().size() - 1; i++) {
                        LatLng startPoint = line.getPoints().get(i);
                        LatLng endPoint = line.getPoints().get(i + 1);
                        mMoveMarker.setPosition(startPoint);
                        mMoveMarker.setRotateAngle((float) CaculateUtil.getAngle(startPoint, endPoint));
                        double slope = CaculateUtil.getSlope(startPoint, endPoint);
                        // 是不是正向的标示（向上设为正向）
                        boolean isReverse = (startPoint.latitude > endPoint.latitude);
                        double intercept = CaculateUtil.getInterception(slope, startPoint);
                        double xMoveDistance = isReverse ? CaculateUtil.getXMoveDistance(slope)
                                : -1 * CaculateUtil.getXMoveDistance(slope);

                        for (double j = startPoint.latitude; !((j > endPoint.latitude) ^ isReverse);
                             j = j - xMoveDistance) {
                            LatLng latLng = null;
                            if (slope != Double.MAX_VALUE) {
                                latLng = new LatLng(j, (j - intercept) / slope);
                            } else {
                                latLng = new LatLng(j, startPoint.longitude);
                            }
                            mMoveMarker.setPosition(latLng);
//                            moveToCurrent(latLng.latitude, latLng.longitude, 16);
                            try {
                                Thread.sleep(MARK_MOVE_INTERVAL);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }

        }.start();
    }
}