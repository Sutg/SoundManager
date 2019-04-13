package com.example.administrator.soundmanager.util;

import android.util.Log;

public class LOG {
    private static boolean isDebug=false;
    public static void d(String tag,String content){
        if(isDebug){
            Log.d(tag,content);
        }
    }
    public static void e(String tag,String content){
        if(isDebug){
            Log.e(tag,content);
        }
    }
    public static void i(String tag,String content){
        if(isDebug){
            Log.i(tag,content);
        }
    }
}