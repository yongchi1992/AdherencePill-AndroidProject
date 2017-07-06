package com.adherence.adherence;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

//Sohum Mehrotra
//5/18

public class TableListAdapter extends RecyclerView.Adapter<TableListAdapter.ViewHolder> {
    /*
    show like this:
    pillname
    time: amount
     */
    private ArrayList<String> pillName;
    private ArrayList<String> time_amount;
    private ArrayList<String> morning;
    private ArrayList<String> afternoon;
    private ArrayList<String> night;


    private ArrayList<Integer> flag;


    public static class ViewHolder extends RecyclerView.ViewHolder{
        public View listRootView;
        private TextView pillName;
        private TextView time_amount;
        private TextView morning;
        private TextView afternoon;
        private TextView night;


        public TextView getPillName(){
            return pillName;
        }
        public TextView getTimeAmount(){
            return time_amount;
        }
        public TextView getMorning(){ return morning;}
        public TextView getAfternoon(){return afternoon;}
        public TextView getNight(){return night;}

        public ViewHolder(View itemView) {
            super(itemView);
            listRootView=itemView;
            pillName= (TextView) itemView.findViewById(R.id.pill_name4);
            time_amount= (TextView) itemView.findViewById(R.id.time_amount);
            morning = (TextView) itemView.findViewById(R.id.morning);
            afternoon = (TextView) itemView.findViewById(R.id.afternoon);
            night = (TextView) itemView.findViewById(R.id.night);
        }
    }

    public TableListAdapter(ArrayList<String> pillName, ArrayList<String> time_amount, ArrayList<Integer> flag
    ,ArrayList<String> aMorning, ArrayList<String> aAfternoon, ArrayList<String> aNight){
        this.pillName=new ArrayList<String>(pillName);
        this.time_amount=new ArrayList<String>(time_amount);
        this.flag=new ArrayList<Integer>(flag);

        this.morning=new ArrayList<String>(aMorning);
        this.afternoon=new ArrayList<String>(aAfternoon);
        this.night=new ArrayList<String>(aNight);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.table_listitem,viewGroup,false);
        ViewHolder vh=new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(TableListAdapter.ViewHolder holder, int position) {
        holder.getPillName().setText(pillName.get(position));
        holder.getTimeAmount().setText(time_amount.get(position));
        holder.getMorning().setText(morning.get(position));
        holder.getAfternoon().setText(afternoon.get(position));
        holder.getNight().setText(night.get(position));
        //flag==1, not arrive time
        if(flag.get(position)==1){
            holder.getTimeAmount().setTextColor(Color.BLACK);

        }else if(flag.get(position)==2) {
            //not have pills in time
            holder.getTimeAmount().setTextColor(Color.RED);

        }
        else{
            //have pills in time
            holder.getTimeAmount().setTextColor(Color.parseColor("#01a532"));
        }
    }

    @Override
    public int getItemCount() {
        return time_amount.size();
    }


}
