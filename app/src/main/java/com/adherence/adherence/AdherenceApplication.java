package com.adherence.adherence;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

/**
 * Created by suhon_000 on 11/6/2015.
 */
public class AdherenceApplication extends Application {

    public static boolean flag = true;
    private static Context mAllContext;

    @Override
    public void onCreate(){
        super.onCreate();
        mAllContext = getApplicationContext();
    }

    public static Context getContext(){
        return mAllContext;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


}
