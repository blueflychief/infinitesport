package com.infinite.com.infinitesport.goodlocation;

import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.infinite.com.infinitesport.MyApplication;
import com.infinite.com.infinitesport.R;
import com.infinite.com.infinitesport.pathtrack.AmapObtainLocation;
import com.infinite.com.infinitesport.pathtrack.CaculateUtil;
import com.infinite.com.infinitesport.pathtrack.FileUtil;
import com.infinite.com.infinitesport.pathtrack.WriteThread;
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
public class GoodLocationActivity extends AppCompatActivity implements View.OnClickListener, SimpleLocation.PositionListener {

    private static final String TAG = "GoodLocationActivity";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Button bt_test;
    private Button bt_real;
    private MapView mMapView = null;
    private AMap mAmap = null;
    private SimpleLocation mSimpleLocation;
    //行驶轨迹
    private List<LatLng> mMoveTrack;
    private File mTmpfile;

    private Marker mMoveMarker;
    private MarkerOptions markerOptions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_location);

        mMapView = (MapView) findViewById(R.id.map);
        bt_test = (Button) findViewById(R.id.bt_test);
        bt_real = (Button) findViewById(R.id.bt_real);
        bt_test.setOnClickListener(this);
        bt_real.setOnClickListener(this);
        initAMap(savedInstanceState);
        initData();
        mSimpleLocation = new SimpleLocation(MyApplication.getInstance(), true, true, 0, true);
        mSimpleLocation.setPositionListener(this);
        mSimpleLocation.beginUpdates();

//        initLocationServer();
//        initData();
    }

    private void initAMap(Bundle savedInstanceState) {
        mMapView.onCreate(savedInstanceState);
        if (mAmap == null) {
            mAmap = mMapView.getMap();
        }
    }

    private void initData() {
        mMoveTrack = new ArrayList<>();
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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


    /**
     * 播放运动轨迹
     */
    private void initMoveMarker(LatLng latLng, float b) {
//        Polyline line = mAmap.addPolyline(polylineOptions);
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
//        deactivate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mSimpleLocation.endUpdates();
        AmapObtainLocation.getLocation().stopLocation();
    }


    /**
     * 围绕操场的跑道画圆，根据速度显示颜色
     */
    private void drawTrack(List<LatLng> mLatLngs) {
        mAmap.addPolyline(new PolylineOptions()
                .colorValues(CaculateUtil.caculateSpeedColor(mLatLngs, 5000))
                .addAll(mLatLngs)
                .useGradient(true)
                .width(10));
        moveToCurrent(mLatLngs.get(0).latitude, mLatLngs.get(0).longitude, 16);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_test:
//                drawTrack(CaculateUtil.readLatLngs(CaculateUtil.coords));
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        initMoveMarker(CaculateUtil.getPolylineOptions(CaculateUtil.coords));
//                    }
//                }, 2000);
                break;
            case R.id.bt_real:
                drawTrack(FileUtil.logReader(mTmpfile));
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        initMoveMarker(FileUtil.logReader(mTmpfile));
//                    }
//                }, 2000);
                break;
        }
    }

    @Override
    public void onPositionChanged(Location location) {
        printLocationInfo(location);
    }

    private void printLocationInfo(Location location) {
        if (location != null) {
            //此处是GPS原始坐标
            MyLogger.i("---------定位成功");
            MyLogger.i("---------纬度" + location.getLatitude());
            MyLogger.i("---------经度" + location.getLongitude());
            MyLogger.i("---------时间" + location.getTime());
            MyLogger.i("---------时间" + sdf.format(location.getTime()));
            MyLogger.i("---------精度" + location.getAccuracy());
            MyLogger.i("---------海拔" + location.getAltitude());
            MyLogger.i("---------方向" + location.getBearing());


            LatLng latLng = new LatLng(mSimpleLocation.getLatitude(), mSimpleLocation.getLongitude());
            CoordinateConverter coordinateConverter = new CoordinateConverter(this);
            LatLng _latLng = coordinateConverter.coord(latLng).from(CoordinateConverter.CoordType.GPS).convert();
            moveToCurrent(_latLng.latitude, _latLng.longitude, 18);
//            moveToCurrent(GeoLatLngTransformUtil.transformFromWGSToGCJ(latLng).latitude, GeoLatLngTransformUtil.transformFromWGSToGCJ(latLng).longitude, 18);

            String s = location.getLatitude() + "," + location.getLongitude();
            whiterSchedule(mTmpfile, s);
            initMoveMarker(_latLng, location.getBearing());
        }
    }


}