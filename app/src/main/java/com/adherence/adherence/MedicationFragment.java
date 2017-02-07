package com.adherence.adherence;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MedicationFragment extends Fragment {

    private static final String ARG_MEDICINE_LIST = "medicine list";
    private static final String ARG_MEDICINE_DETAIL = "medicine detail";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_SESSION_TOKEN="session_token";

    private RecyclerView mRecyclerView;
    private MedicationListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private String[] medicineListHardcode;
    private String[] detailListHardcode;
    private String sessionToken;

    private Context mContext=null;

    private TextView pop_pillname;
    private TextView pop_pillinfo;
    private TextView pop_pillinstruction;

    private Prescription[] prescriptions;
    private RequestQueue mRequestQueue;

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
        medicineListHardcode = getArguments().getStringArray(ARG_MEDICINE_LIST);
        detailListHardcode = getArguments().getStringArray(ARG_MEDICINE_DETAIL);
        sessionToken=getArguments().getString(ARG_SESSION_TOKEN);
        Log.d("medi_fragment session",sessionToken);
        mContext = this.getContext();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_medication, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.medication_list);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRequestQueue= Volley.newRequestQueue(getActivity());
        String url="http://129.105.36.93:5000/patient/prescription";
        JsonArrayRequest request=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Toast.makeText(getActivity(),response.toString(),Toast.LENGTH_SHORT).show();
                Log.d("response",response.toString());
                
                //retrive data from JSONobject
                int i = response.length();
                prescriptions = new Prescription[i];
                for (int j = 0;j < i;j++){
                    prescriptions[j] = new Prescription();
                    try {
                        JSONObject prescript = response.getJSONObject(j);
                        prescriptions[j].setName(prescript.getString("name"));
                        prescriptions[j].setNote(prescript.getString("note"));
                        prescriptions[j].setPill(prescript.getString("pill"));
                        JSONArray schedule = prescript.getJSONArray("schedule");


                        for(int k = 0; k < schedule.length(); k++){
                            JSONObject takeTime = schedule.getJSONObject(k);
                            String time = takeTime.getString("time");
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
                            prescriptions[j].setSchedule(time, days);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // test if data stored in prescriptions
                for(int j = 0; j < i; j++) {
                    System.out.println(prescriptions[j].getName());
                    System.out.println(prescriptions[j].getNote());
                    System.out.println(prescriptions[j].getPill());

                    //traverse with Map.Entry
                    Iterator<Map.Entry<String, Map<String, Integer>>> it = prescriptions[j].getSchedule().entrySet().iterator();

                    while (it.hasNext()) {

                        // entry.getKey() return key
                        // entry.getValue() return value
                        Map.Entry<String, Map<String, Integer>> entry = (Map.Entry) it.next();
                        System.out.println(entry.getKey());

                        HashMap<String, Integer> tmp_in_hashmap = (HashMap) entry.getValue();

                        Iterator<Map.Entry<String, Integer>> in_iterator = tmp_in_hashmap
                                .entrySet().iterator();

                        while (in_iterator.hasNext()) {
                            Map.Entry in_entry = (Map.Entry) in_iterator.next();
                            System.out.println(in_entry.getKey() + ":" + in_entry.getValue());
                        }
                    }
                }

                
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
        mRequestQueue.add(request);

     /*   ParseQuery<ParseObject> query2=ParseQuery.getQuery("Prescription");
        query2.whereNotEqualTo("pill",null);
        query2.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(objects==null){
                    Toast.makeText(getActivity(),"objects is null!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i("objects.size another:",objects.size()+"");
                medicineListHardcode=new String[objects.size()];
                detailListHardcode=new String[objects.size()];
                Log.i("medicineListHardcode", medicineListHardcode.length+"");
                Log.i("detailListHardcode",detailListHardcode.length+"");
                for(int i=0;i<objects.size();i++){
                    medicineListHardcode[i]=objects.get(i).getString("name");
                    detailListHardcode[i]=objects.get(i).getString("note");
                }
                mAdapter = new MedicationListAdapter(medicineListHardcode,detailListHardcode);
                mRecyclerView.setAdapter(mAdapter);

                mAdapter.setOnItemClickListener(new MedicationListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(final View view, int position) {
                        //Toast.makeText(getActivity(),position+"", Toast.LENGTH_SHORT).show();
                        ParseQuery<ParseObject>query3= ParseQuery.getQuery("Prescription");
                        query3.whereEqualTo("name",medicineListHardcode[position]);
                        query3.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                String pillID=objects.get(0).getParseObject("pill").getObjectId();
                                ParseQuery<ParseObject>query4=ParseQuery.getQuery("PillLib");
                                query4.whereEqualTo("objectId",pillID);
                                query4.findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(List<ParseObject> objects, ParseException e) {
                                        String pillname=objects.get(0).getString("pillName");
                                        String pillinfo=objects.get(0).getString("pillInfo");
                                        String pillinstruction=objects.get(0).getString("pillInstruction");
                                        showPopupWindow(view,"pillName: "+pillname,"pillInfo: "+pillinfo,"pillInstruction: "+pillinstruction);

                                    }
                                });
                            }
                        });
                        //   showPopupWindow(view);
                    }
                    @Override
                    public void onItemLongClick(final View view, int position) {
                        ParseQuery<ParseObject>query3= ParseQuery.getQuery("Prescription");
                        query3.whereEqualTo("name",medicineListHardcode[position]);
                        query3.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                String pillID=objects.get(0).getParseObject("pill").getObjectId();
                                ParseQuery<ParseObject>query4=ParseQuery.getQuery("PillLib");
                                query4.whereEqualTo("objectId",pillID);
                                query4.findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(List<ParseObject> objects, ParseException e) {
                                        String pillname=objects.get(0).getString("pillName");
                                        String pillinfo=objects.get(0).getString("pillInfo");
                                        String pillinstruction=objects.get(0).getString("pillInstruction");
                                        showPopupWindow(view,"pillName: "+pillname,"pillInfo: "+pillinfo,"pillInstruction: "+pillinstruction);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });*/
//        mAdapter = new MedicationListAdapter(detailListHardcode,detailListHardcode);
//        mRecyclerView.setAdapter(mAdapter);

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
}
