package com.adherence.adherence;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.*;

import org.json.JSONException;
import org.json.JSONObject;

import static android.R.attr.name;
import static com.adherence.adherence.R.id.pwd;

public class MainActivity extends AppCompatActivity {

    private RequestQueue mRequestQueue;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button conformButton = (Button) this.findViewById(R.id.conformButton);
        Button cancelButton = (Button) this.findViewById(R.id.cancelButton);
        OnClickListener ocl = new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Login(arg0);
            }
        };
        conformButton.setOnClickListener(ocl);
        cancelButton.setOnClickListener(ocl);


        final Button button = (Button) findViewById(R.id.button_send);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Date now = new Date();
                String timestamp = new SimpleDateFormat("yyyy/MM/dd").format(now);
                ParseObject testObject = new ParseObject("TestXZ");
                testObject.put("TIME", timestamp);
                testObject.put("NAME", "ARYAN");
                testObject.saveEventually();
                Toast.makeText(getApplicationContext(), "Click button", Toast.LENGTH_LONG).show();
            }
        });

        final Button button1 = (Button) findViewById(R.id.Jarandice);
        button1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Date now = new Date();
                String timestamp = new SimpleDateFormat("yyyy/MM/dd").format(now);
                ParseObject testObject = new ParseObject("TestXZ");
                testObject.put("TIME", timestamp);
                testObject.put("NAME", "Jarandice");
                testObject.saveEventually();
                Toast.makeText(getApplicationContext(), "Click button", Toast.LENGTH_LONG).show();
            }
        });

        final Button button2 = (Button) findViewById(R.id.Truvada);
        button2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Date now = new Date();
                String timestamp = new SimpleDateFormat("yyyy/MM/dd").format(now);
                ParseObject testObject = new ParseObject("TestXZ");
                testObject.put("TIME", timestamp);
                testObject.put("NAME", "Truvada");
                testObject.saveEventually();
                Toast.makeText(getApplicationContext(), "Click button", Toast.LENGTH_LONG).show();
            }
        });

        final Button button3 = (Button) findViewById(R.id.Asprin);
        button3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Date now = new Date();
                String timestamp = new SimpleDateFormat("yyyy/MM/dd").format(now);
                ParseObject testObject = new ParseObject("TestXZ");
                testObject.put("TIME", timestamp);
                testObject.put("NAME", "Asprin");
                testObject.saveEventually();
                Toast.makeText(getApplicationContext(), "Click button", Toast.LENGTH_LONG).show();
            }
        });

        final Button button4 = (Button) findViewById(R.id.Lipitor);
        button4.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Date now = new Date();
                String timestamp = new SimpleDateFormat("yyyy/MM/dd").format(now);
                ParseObject testObject = new ParseObject("TestXZ");
                testObject.put("TIME", timestamp);
                testObject.put("NAME", "Lipitor");
                testObject.saveEventually();
                Toast.makeText(getApplicationContext(), "Click button", Toast.LENGTH_LONG).show();
            }
        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void Login(View arg0) {
        EditText name = (EditText) findViewById(R.id.username);
        EditText pwd = (EditText) findViewById(R.id.pwd);
        Button bt = (Button) findViewById(arg0.getId());
        String text = bt.getText().toString();
//        loginParse(name, pwd);

        if (text.equals("login")) {
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
        String url = "http://129.105.36.93:5000/login";

        Map<String, String> map = new HashMap<>();
//        map.put("username", name.getText().toString());
//        map.put("password", password.getText().toString());

        map.put("username", "1@1");
        map.put("password", "1");

        JSONObject jsonObject = new JSONObject(map);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject res) {
                        Log.d("res",res.toString());

                        try {
                            String sessionToken=res.getString("sessionToken");
                            Log.d("sessiontoken", sessionToken);
                            SharedPreferences data=getSharedPreferences("data",MODE_PRIVATE);
                            SharedPreferences.Editor editor=data.edit();
                            editor.putString("sessionToken",sessionToken);
//                            editor.putString("username",name.getText().toString());
                            editor.putString("username","1@1");
                            Intent intent=new Intent();
//                            intent.putExtra("sessionToken",sessionToken);
//                            intent.putExtra("username",name.getText().toString());
                            intent.setClass(MainActivity.this, NextActivity.class);
                            MainActivity.this.startActivity(intent);

                            editor.commit();
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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

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

}