
package com.adherence.adherence;

import com.parse.Parse;

/**
 * Created by barnabaskim on 10/19/16.
 */

public class ParseInitialize extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("myAppId")
                .clientKey("myClientKey")
                .server("http://129.105.36.93:5000/parse").build());
    }
}
