package com.adherence.adherence;

import android.content.Context;

/**
 * Created by caoye on 16/12/2.
 */
public class ScreenUtils {
    
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }
   
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }


}
