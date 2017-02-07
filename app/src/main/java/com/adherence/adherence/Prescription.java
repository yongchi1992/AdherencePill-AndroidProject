package com.adherence.adherence;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sam on 1/30/17.
 */

public class Prescription {
    private String name;
    private String note;
    private String pill;
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

    public String getName(){
        return name;
    }
    public String getNote(){
        return note;
    }
    public String getPill(){
        return pill;
    }
    public void setSchedule(String time, Map<String,Integer> days){
        this.schedule.put(time, days);
    }
    public Map<String, Map<String,Integer>> getSchedule(){
        return schedule;
    }
}
