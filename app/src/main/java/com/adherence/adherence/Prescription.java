package com.adherence.adherence;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.app.PendingIntent.getActivity;

/**
 * Created by sam on 1/30/17.
 */

public class Prescription {
    private String name;//prescription name
    private String note;//prescription note
    private String pill;//pill name
    private String prescriptionId;
    private String bottleName;
    private Boolean newAdded;//whether the prescription is newly added
    private int pillNumber; //pill ID

    private Bitmap image;
//    private String imageSrc;
    private int listItemPosition;
    private boolean haveImage;
    private String details;
    private String timeList;

    private Map<String, Map<String,Integer>> schedule = new HashMap<String, Map<String,Integer>>();

    public Prescription() {

    }

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
    public void setBottleName(String bottleName){this.bottleName = bottleName;}
    public void setNewAdded (Boolean newAdded){this.newAdded = newAdded;}
    public void setPillNumber(int pillNumber){this.pillNumber = pillNumber;}
    public void setImage(Bitmap image) {
        this.image = image;
    }
    public void setListItemPosition(int listItemPosition) {
        this.listItemPosition = listItemPosition;
    }
    public void setHaveImage(boolean haveImage) {
        this.haveImage = haveImage;
    }
    public void setDetails(String details) {
        this.details = details;
    }
    public void setTimeList(String timeList){
        this.timeList = timeList;
    }


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
    public Bitmap getImage() {
        return image;
    }
    public int getListItemPosition() {return  listItemPosition;}
    public boolean isHaveImage() {
        return haveImage;
    }
    public String getDetails() {
        return details;
    }
    public String getTimeList() {
        return timeList;
    }

    public void setSchedule(String time, Map<String,Integer> days){
        this.schedule.put(time, days);
    }
    public Map<String, Map<String,Integer>> getSchedule(){
        return schedule;
    }

    public void trimTimeList() {
        timeList.trim();
    }

    //input a week day and return all the time and amount to take pills in this day
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

    public void findLocalImg(){
        if (!this.name.isEmpty()){
            String extStorageDirectory = (Environment.getExternalStorageDirectory() + "/Adherence/savedPictures").toString();
            // TODO: 8/16/17 use strings instead 
            File file = new File(extStorageDirectory, this.name + ".png");
            if (file.exists()) {
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                this.image = BitmapFactory.decodeFile(file.getAbsolutePath(), bitmapOptions);
                this.haveImage = true;

            }else {
                this.image = null;
                this.haveImage = false;
            }
        }else{
            this.image = null;
            this.haveImage = false;
        }

    }
}
