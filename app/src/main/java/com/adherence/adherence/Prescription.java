package com.adherence.adherence;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.app.PendingIntent.getActivity;

/**
 * Created by sam on 1/30/17.
 */

public class Prescription {
    private String name;
    private String note;
    private String pill;
    private String prescriptionId;
    private String bottleName;
    private Boolean newAdded;
    private int pillNumber;


    private Map<String, Map<String,Integer>> schedule = new HashMap<String, Map<String,Integer>>();
    public void setName(String name){
        this.name = name;
    }
    public void setNote(String note){
        this.note = note;
    }
    public void setPill(String pill){
        this.pill = pill;
    }
    public void setPrescriptionId(String prescptionId){this.prescriptionId = prescptionId;}
    public void setBottleName(String bottleName){this.bottleName = bottleName;Log.d("TAG","bottle name is set");}
    public void setNewAdded (Boolean newAdded){this.newAdded = newAdded;}
    public void setPillNumber(int pillNumber){this.pillNumber = pillNumber;}

    public String getName(){
        return name;
    }
    public String getNote(){
        return note;
    }
    public String getPill(){
        return pill;
    }
    public String getPrescriptionId(){return prescriptionId;}
    public String getBottleName(){return bottleName;}
    public Boolean getNewAdded(){return newAdded;}
    public int getPillNumber(){return pillNumber;}

    public void setSchedule(String time, Map<String,Integer> days){
        this.schedule.put(time, days);
    }
    public Map<String, Map<String,Integer>> getSchedule(){
        return schedule;
    }
    public HashMap<String, Integer> getTimeAmount(String day){
        HashMap<String, Integer> set = new HashMap<String, Integer>();
        int amount;
        Iterator<Map.Entry<String, Map<String, Integer>>> it = schedule.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, Map<String, Integer>> entry = (Map.Entry) it.next();
            if((amount = entry.getValue().get(day)) != 0){
                set.put(entry.getKey(), amount);
            };
        }
        return set;
    }

}
