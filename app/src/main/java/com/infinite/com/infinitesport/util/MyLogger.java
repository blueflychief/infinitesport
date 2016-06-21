package com.infinite.com.infinitesport.util;

import android.util.Log;

/**
 * 日志工具类
 */
public class MyLogger {
    private static boolean DEBUG = true;
    
    private static String AMAP_TAG="amap_log";    
    public static void v(String msg) {
        if (DEBUG) {
            Log.v(AMAP_TAG, msg);
        }
    }

    public static void d(String msg) {
        if (DEBUG) {
            Log.d(AMAP_TAG, msg);
        }
    }

    public static void i(String msg) {
        if (DEBUG) {
            Log.i(AMAP_TAG, msg);
        }
    }

    public static void w(String msg) {
        if (DEBUG) {
            Log.w(AMAP_TAG, msg);
        }
    }

    public static void e(String msg) {
        if (DEBUG) {
            Log.e(AMAP_TAG, msg);
        }
    }

    public static void w(Throwable tr) {
        if (DEBUG) {
            Log.w(AMAP_TAG, Log.getStackTraceString(tr));
        }
    }

    public static void e(Throwable tr) {
        if (DEBUG)
            Log.e(AMAP_TAG, Log.getStackTraceString(tr));
    }

    public static void i(Throwable tr) {
        if (DEBUG) {
            Log.i(AMAP_TAG, Log.getStackTraceString(tr));
        }
    }

}
