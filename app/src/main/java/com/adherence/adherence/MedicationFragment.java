package com.adherence.adherence;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MedicationFragment extends Fragment {

    private static final String ARG_MEDICINE_LIST = "medicine list";
    private static final String ARG_MEDICINE_DETAIL = "medicine detail";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_SESSION_TOKEN="session_token";

    private RecyclerView mRecyclerView;
    private MedicationListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

//    private String[] medicineListHardcode;
//    private String[] detailListHardcode;
//    private String[] timeListHardcode;
    private int position;
    private ArrayList<Prescription> prescriptions;
    private String sessionToken;
    private String mDay;
    private String imageTempName;

    private Context mContext=null;

    private TextView pop_pillname;
    private TextView pop_pillinfo;
    private TextView pop_pillinstruction;

//    private Prescription[] prescriptions;
    private static RequestQueue mRequestQueue;

    public Calendar c = Calendar.getInstance();

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MedicationFragment newInstance(String[] medicineList,String[] detailList, String sessionToken, int sectionNumber) {
        MedicationFragment fragment = new MedicationFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_MEDICINE_LIST, medicineList);
        args.putStringArray(ARG_MEDICINE_DETAIL, detailList);
        args.putString(ARG_SESSION_TOKEN,sessionToken);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        medicineListHardcode = getArguments().getStringArray(ARG_MEDICINE_LIST);
//        detailListHardcode = getArguments().getStringArray(ARG_MEDICINE_DETAIL);
        sessionToken=getArguments().getString(ARG_SESSION_TOKEN);
        Log.d("medi_fragment session",sessionToken);
        mContext = this.getContext();
        mRequestQueue = Volley.newRequestQueue(getActivity());
        Log.d("sequence", "onCreate!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_medication, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.medication_list);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
//            mRequestQueue= Volley.newRequestQueue(getActivity());

        String int_day=String.valueOf(c.get(c.DAY_OF_WEEK));
        switch (int_day){
            case "1":mDay="Sunday";break;
            case "2":mDay="Monday";break;
            case "3":mDay="Tuesday";break;
            case "4":mDay="Wednesday";break;
            case "5":mDay="Thursday";break;
            case "6":mDay="Friday";break;
            case "7":mDay="Saturday";break;
            default:mDay="Sunday";break;
        }

        String url= getString(R.string.parseURL) + "/patient/prescription";

        final JsonArrayRequest prescriptionRequest=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("response",response.toString());

                //retrive data from JSONobject
                int size = response.length();
                prescriptions = new ArrayList<>();

                for (int j = 0;j < size;j++){
                    Prescription newPrescrip = new Prescription();
                    //TODO:set image from server
                    newPrescrip.setHaveImage(false);
                    try {
                        JSONObject prescript = response.getJSONObject(j);
                        newPrescrip.setName(prescript.getString("name"));
                        if(prescript.has("bottle")){
                            if(prescript.getJSONObject("bottle").has("bottleName")){
                                newPrescrip.setBottleName(prescript.getJSONObject("bottle").getString("bottleName"));
                            }
                            if(prescript.getJSONObject("bottle").has("pillNumber")){
                                newPrescrip.setPillNumber(prescript.getJSONObject("bottle").getInt("pillNumber"));
                            }
                        }else{
                            newPrescrip.setBottleName("null");
                            newPrescrip.setPillNumber(0);
                        }
                        if(prescript.has("note")) {
                            newPrescrip.setNote(prescript.getString("note"));
                        }else{
                            newPrescrip.setNote("none");
                        }
                        if(prescript.has("pill")) {
                            newPrescrip.setPill(prescript.getString("pill"));
                        }else{
                            newPrescrip.setPill("none");
                        }
                        if(prescript.has("newAdded")){
                            newPrescrip.setNewAdded(prescript.getBoolean("newAdded"));
                        }else {
                            newPrescrip.setNewAdded(false);
                        }
                        //prescriptions[j].setPrescriptionId(prescript.getString("objectId"));

                        JSONArray schedule = prescript.getJSONArray("schedule");


                        for(int k = 0; k < schedule.length(); k++){
                            JSONObject takeTime = schedule.getJSONObject(k);
                            String time = takeTime.getString("time").substring(11, 19);
                            JSONArray takeWeek = takeTime.getJSONArray("days");
                            Map<String, Integer> days = new HashMap<String, Integer>();
                            for(int l = 0; l < takeWeek.length(); l++){
                                JSONObject takeDays = takeWeek.getJSONObject(l);
                                if(takeDays.has("amount")){
                                    days.put(takeDays.getString("name"), takeDays.getInt("amount"));
                                }else {
                                    days.put(takeDays.getString("name"), 0);
                                }
                            }
                            newPrescrip.setSchedule(time, days);
                        }

                        Iterator<Map.Entry<String, Integer>> itr = newPrescrip.getTimeAmount("Monday").entrySet().iterator();

                        while(itr.hasNext()){
//                            TODO: Implement this function in Prescription Class
                        Map.Entry<String, Integer> entry = itr.next();
                        int cur_hour = Integer.parseInt(entry.getKey().substring(0, 2));

                        if (7 <= cur_hour && cur_hour < 12) {
                            if (newPrescrip.getTimeList() == null) {
                                newPrescrip.setTimeList("Morning" + "\n");
                            }else {
                                newPrescrip.setTimeList(newPrescrip.getTimeList() + "Morning" + "\n");
                            }
                        }

                        if (12 <= cur_hour && cur_hour < 18) {
                            if (newPrescrip.getTimeList() == null) {
                                newPrescrip.setTimeList("Afternoon" + "\n");
                            }else {
                                newPrescrip.setTimeList(newPrescrip.getTimeList() + "Afternoon" + "\n");
                            }
                        }

                        if (18 <= cur_hour && cur_hour <= 24) {
                            if (newPrescrip.getTimeList() == null) {
                                newPrescrip.setTimeList("Evening" + "\n");
                            }else {
                                newPrescrip.setTimeList(newPrescrip.getTimeList() + "Evening" + "\n");
                            }
                        }

                        if (0 <= cur_hour && cur_hour < 7) {
                            if (newPrescrip.getTimeList() == null) {
                                newPrescrip.setTimeList("Bedtime" + "\n");
                            }else {
                                newPrescrip.setTimeList(newPrescrip.getTimeList() + "Bedtime" + "\n");
                            }
                        }

                        newPrescrip.trimTimeList();

                    }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    prescriptions.add(newPrescrip);
                }

                mAdapter = new MedicationListAdapter(prescriptions, getContext(), MedicationFragment.this);
                mRecyclerView.setAdapter(mAdapter);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error",error.toString());
            }
        }){
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("x-parse-session-token",sessionToken);
                return headers;
            }
        };


        //prescriptions[0].requestSetBottleName(sessionToken, mRequestQueue);

        mRequestQueue.add(prescriptionRequest);


        Log.d("sequence","onCreateView!");

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NextActivity){
            ((NextActivity) context).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    private void showPopupWindow(View view,String pillname,String pillinfo,String pillinstruction) {


        View contentView = LayoutInflater.from(mContext).inflate(
                R.layout.pop_up_window, null);


        final PopupWindow popupWindow = new PopupWindow(contentView,
                RecyclerView.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);

        pop_pillname= (TextView) contentView.findViewById(R.id.pop_pillname);
        pop_pillinfo= (TextView) contentView.findViewById(R.id.pop_pillinfo);
        pop_pillinstruction= (TextView) contentView.findViewById(R.id.pop_instruction);
        pop_pillname.setText(pillname);
        pop_pillinfo.setText(pillinfo);
        pop_pillinstruction.setText(pillinstruction);
        popupWindow.setTouchable(true);

        popupWindow.setTouchInterceptor(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                Log.i("mengdd", "onTouch : ");

                return false;

            }
        });


        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape));
//        popupWindow.setBackgroundDrawable(null);


        View windowContentViewRoot = getView();
        int windowPos[] = calculatePopWindowPos(view, windowContentViewRoot);
        int xOff = 20;
        windowPos[0] -= xOff;
        popupWindow.showAtLocation(view, Gravity.TOP | Gravity.START, windowPos[0], windowPos[1]);

    }

    private static int[] calculatePopWindowPos(final View anchorView, final View contentView) {
        final int windowPos[] = new int[2];
        final int anchorLoc[] = new int[2];

        anchorView.getLocationOnScreen(anchorLoc);
        final int anchorHeight = anchorView.getHeight();

        final int screenHeight = ScreenUtils.getScreenHeight(anchorView.getContext());
        final int screenWidth = ScreenUtils.getScreenWidth(anchorView.getContext());
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        final int windowHeight = contentView.getMeasuredHeight();
        final int windowWidth = contentView.getMeasuredWidth();

        final boolean isNeedShowUp = (screenHeight - anchorLoc[1] - anchorHeight < windowHeight);
        if (isNeedShowUp) {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] - windowHeight;
        } else {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] + anchorHeight;
        }
        return windowPos;
    }




    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage, String imageName) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, imageName, null);
        return Uri.parse(path);
    }

    private void onCaptureImageResult(Intent data) {

        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) extras.get("data");

        // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
        Uri tempUri = getImageUri(getContext(), imageBitmap, imageTempName);
        String picturePath = getRealPathFromURI(tempUri);
        mAdapter.setImageInItem(position, imageBitmap);

    }

    public Bitmap convertSrcToBitmap(String imageSrc) {
        Bitmap myBitmap = null;
        File imgFile = new File(imageSrc);
        if (imgFile.exists()) {
            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
        return myBitmap;
    }

    public void captureImage(int pos, String imageName) {
        position = pos;
        imageTempName = imageName;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 100);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == 100) {
                onCaptureImageResult(data);
            }
        }
    }
}
