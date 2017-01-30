package com.adherence.adherence;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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

        Parse.enableLocalDatastore(this);
//        Parse.initialize(this, "BDo39lSOtPuBwDfq0EBDgIjTzztIQE38Fuk03EcR", "6exCVtTYC6JhQP6gw1OFByyP2RRq5McznAsoQ3Gq");
        ParseUser.enableAutomaticUser();
//        ParseObject testObject = new ParseObject("TestObject");
//        testObject.put("foo", "bar");
//        testObject.saveInBackground();
//        ParseQuery<ParseObject> query = ParseQuery.getQuery("Doctor");
//        if (query==null) Toast.makeText(getApplicationContext(),"query is null!",Toast.LENGTH_SHORT).show();
//        else Toast.makeText(getApplicationContext(),"query is not null",Toast.LENGTH_SHORT).show();
//        query.getInBackground("1FLsZV9aHZ", new GetCallback<ParseObject>() {
//            @Override
//            public void done(ParseObject object, ParseException e) {
//                if(e==null){
//                    String str=object.getString("hospitalName");
//           //         Toast.makeText(getApplicationContext(),str,Toast.LENGTH_SHORT).show();
//                }
//                else {
//          //          Toast.makeText(getApplicationContext(),"something wrong!",Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
        List<ParseObject> user = new ArrayList<>();
        try {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("saveUser");
            query.fromLocalDatastore();
            user = query.find();
        } catch (Exception e) {
        }

        if (user.size() > 0) {;
            Intent intent = new Intent();
            intent.setClass(FirstActivity.this, NextActivity.class);
//            intent.setClass(FirstActivity.this, CalendarTestActivity.class);

            FirstActivity.this.startActivity(intent);
        }


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent();
//                intent.setClass(FirstActivity.this, MainActivity2.class);
                intent.setClass(FirstActivity.this, MainActivity.class);
                //               intent.setClass(FirstActivity.this, CalendarTestActivity.class);
                FirstActivity.this.startActivity(intent);
            }
        }, 2000);




    }
}