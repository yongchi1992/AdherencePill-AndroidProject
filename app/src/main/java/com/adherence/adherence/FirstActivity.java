package com.adherence.adherence;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weihanchu on 6/2/16.
 */
public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);


//        Parse.enableLocalDatastore(this);
//        Parse.initialize(new Parse.Configuration.Builder(this)
//                .applicationId("myAppId")
//                .clientKey("myMasterKey")
//                .server("http://129.105.36.93:5000/parse/").build());
//
////        Parse.initialize(this, "BDo39lSOtPuBwDfq0EBDgIjTzztIQE38Fuk03EcR", "6exCVtTYC6JhQP6gw1OFByyP2RRq5McznAsoQ3Gq");
//
//
//        ParseUser.enableAutomaticUser();
//        List<ParseObject> user = new ArrayList<>();
//        try {
//            ParseQuery<ParseObject> query = ParseQuery.getQuery("saveUser");
//            query.fromLocalDatastore();
//            user = query.find();
//        } catch (Exception e) {
//        }

//        if (user.size() > 0) {;
//            Intent intent = new Intent();
//            intent.setClass(FirstActivity.this, NextActivity.class);
//            FirstActivity.this.startActivity(intent);
//
//        }


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setClass(FirstActivity.this, MainActivity.class);
                FirstActivity.this.startActivity(intent);
            }
        }, 2000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}