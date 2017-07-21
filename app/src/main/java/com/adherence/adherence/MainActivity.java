package com.adherence.adherence;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RequestQueue mRequestQueue;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private SharedPreferences loginPrefs;
    private SharedPreferences.Editor editor;
    private SharedPreferences settings;
    private SharedPreferences.Editor setting_editor;
    private CheckBox saveLoginCheckBox;
    private EditText name;
    private EditText pwd;

    private Resources res;

    public static final String UserPREFERENCES = "UserPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button conformButton = (Button) this.findViewById(R.id.conformButton);
        Button cancelButton = (Button) this.findViewById(R.id.cancelButton);
        saveLoginCheckBox = (CheckBox) findViewById(R.id.saveLoginCheckBox);
        name = (EditText) findViewById(R.id.username);
        pwd = (EditText) findViewById(R.id.pwd);
        res = getResources();

        loginPrefs = getSharedPreferences(UserPREFERENCES, MODE_PRIVATE);
        editor = loginPrefs.edit();
        initLogin();

        saveLoginCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                rememberMe();
            }
        });


        OnClickListener ocl = new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Login(arg0);
            }
        };
        conformButton.setOnClickListener(ocl);
        cancelButton.setOnClickListener(ocl);




        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void Login(View arg0) {

        Button bt = (Button) findViewById(arg0.getId());
        String text = bt.getText().toString();

        if (text.equals("login")) {
            rememberMe();
            loginParse(name,pwd);
        }
        else{
            if(text.equals("reset")) {
                name.setText("");
                pwd.setText("");
            }
        }
    }

    private void loginParse(final EditText name, final EditText password) {
        mRequestQueue = Volley.newRequestQueue(this);

        String url = getString(R.string.parseURL) + "/login";

        Map<String, String> map = new HashMap<>();
        map.put("username", name.getText().toString());
        map.put("password", password.getText().toString());

//        map.put("username", "1@1");
//        map.put("password", "1");

        JSONObject jsonObject = new JSONObject(map);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject res) {
                        Log.d("res",res.toString());

                        try {
                            String sessionToken=res.getString("sessionToken");
                            Log.d("sessiontoken", sessionToken);
//                            String bottle_temp = res.getString("bottle");
//                            Log.d("code", bottle_temp);

                            editor.putString("sessionToken",sessionToken);
                            editor.putString("username",name.getText().toString());
//                            editor.putString("bottle", "SC36-05  4C:55:CC:10:7B:12");
                            editor.commit();
                            settings = getSharedPreferences(loginPrefs.getString("username", ""), MODE_PRIVATE);
                            setting_editor = settings.edit();
                            initSettings();

                            Intent intent=new Intent();
                            intent.putExtra("sessionToken",sessionToken);
                            intent.putExtra("username",name.getText().toString());
                            intent.setClass(MainActivity.this, NextActivity.class);
                            MainActivity.this.startActivity(intent);


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("res","get token wrong!");
                        }
                    }
                },
                new Response.ErrorListener() {


                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"wrong username or password!",Toast.LENGTH_SHORT).show();
                        name.setText("");
                        password.setText("");
                    }
                }
        );
        mRequestQueue.add(jsonObjectRequest);
    }
    /*
    private void loginparse(String name, String password) {
        if (isNetworkConnected()) {
            ParseUser.logInInBackground(name, password, new
                    com.parse.LogInCallback() {
                        public void done(ParseUser user, ParseException e) {
                            if (user != null) {
                                // These lines are saving the user's log-in status
                                ParseObject saveuser = new ParseObject("saveUser");
                                saveuser.put("user", user);
                                saveuser.pinInBackground("user");
                                // Go to the next page after logged in
                                Intent intent = new Intent();
                                intent.setClass(MainActivity.this, NextActivity.class);
                                MainActivity.this.startActivity(intent);
                            } else {
                                Context context = getApplicationContext();
                                CharSequence text = "Wrong password or username, please input again";
                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }
                        }
                    });
        } else {
            Context context = getApplicationContext();
            CharSequence text = "Network connection not available, please try again.";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
    */

//    private boolean isNetworkConnected() {
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo ni = cm.getActiveNetworkInfo();
//        if (ni == null) {
//            // There are no active networks.
//            return false;
//        } else
//            return true;
//    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
        finish();
    }

    public void initLogin() {
        if (loginPrefs.getBoolean("saveLogin", false)){
            name.setText(loginPrefs.getString("username", ""));
            pwd.setText(loginPrefs.getString("password", ""));
            saveLoginCheckBox.setChecked(true);
            name.setSelection(name.getText().length());
        }

    }

    public void rememberMe() {
        if (saveLoginCheckBox.isChecked()){
            editor.putBoolean("saveLogin", true);
            editor.putString("username", String.valueOf(name.getText()));
            editor.putString("password", String.valueOf(pwd.getText()));
            editor.commit();

        } else {
            editor.putBoolean("saveLogin", false);
            editor.remove("username");
            editor.remove("password");
            editor.commit();
        }
    }

    private void initSettings(){
        setting_editor.putInt("morning_progress", settings.getInt("morning_progress", res.getInteger(R.integer.settings_morning_progress_init)));
        setting_editor.putInt("afternoon_progress", settings.getInt("afternoon_progress", res.getInteger(R.integer.settings_afternoon_progress_init)));
        setting_editor.putInt("evening_progress", settings.getInt("evening_progress", res.getInteger(R.integer.settings_evening_progress_init)));
        setting_editor.putInt("bedtime_progress", settings.getInt("bedtime_progress", res.getInteger(R.integer.settings_bedtime_progress_init)));

        setting_editor.putBoolean("notification", settings.getBoolean("notification", true));
        setting_editor.putBoolean("vibration", settings.getBoolean("vibration", true));
        setting_editor.putBoolean("sound", settings.getBoolean("sound", true));

        setting_editor.putInt("remind_progress", settings.getInt("remind_progress", res.getInteger(R.integer.settings_remind_progress_init)));
        setting_editor.putInt("interval_progress", settings.getInt("interval_progress", res.getInteger(R.integer.settings_interval_progress_init)));
        setting_editor.putInt("maximum_progress", settings.getInt("maximum_progress", res.getInteger(R.integer.settings_max_remind_progress_init)));

        setting_editor.commit();

    }

}