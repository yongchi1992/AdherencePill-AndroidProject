
package com.adherence.adherence;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by barnabaskim on 10/19/16.
 */

public class ParseInitialize extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("myAppId")
                .clientKey("myClientKey")
                .server("http://129.105.36.93:5000/parse/").build());
        ParseObject upload = new ParseObject("GameScore");
        upload.put("score", "90");
        upload.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Log.d("Parse test", "succeed");
                } else {
                    Log.d("Parse test", e.toString());
                }
            }
        });
        ParseUser.enableAutomaticUser();
    }


}
