package com.adherence.adherence;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by sam on 17/6/18.
 */


public class TodayListAdapter3 extends RecyclerView.Adapter<TodayListAdapter3.ViewHolder>{

    /*
    show like this:
    pillname
    time: amount
     */

    private ArrayList<JSONObject> schedule;


    public static class ViewHolder extends RecyclerView.ViewHolder{
        public View listRootView;
        private TextView pillName;

        private TextView period;
        private ImageView tick;
        private ImageView pillAmount;
        private TextView pillNote;




        public TextView getPillName(){
            return pillName;
        }
        public TextView getPeriod() {return period;}
        public ImageView getTick() {return tick;}
        public TextView getPillNote() {return pillNote;}
        public ImageView getPillAmount() {return pillAmount;}



        public ViewHolder(View itemView) {
            super(itemView);
            listRootView=itemView;
            pillName= (TextView) itemView.findViewById(R.id.pill_name4);
            period = (TextView) itemView.findViewById(R.id.period);
            tick = (ImageView) itemView.findViewById(R.id.tick3);
            pillNote = (TextView) itemView.findViewById(R.id.pillNote3);
            pillAmount = (ImageView) itemView.findViewById(R.id.pillAmount4);


        }

    }

    public TodayListAdapter3(ArrayList<JSONObject> schedule){
        this.schedule = schedule;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.today_listitem3,viewGroup,false);
        ViewHolder vh=new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(TodayListAdapter3.ViewHolder holder, final int position) {

        JSONObject temp = schedule.get(position);


        try {
            holder.getPillName().setText(temp.getString("name"));
            holder.getPeriod().setText(temp.getString("period"));
            holder.getPillNote().setText(temp.getString("note"));
            //may use xml to replace in future
            switch (temp.getInt("amount")){
                case 1:
                    holder.getPillAmount().setImageResource(R.drawable.one_blue);
                    break;
                case 2:
                    holder.getPillAmount().setImageResource(R.drawable.two_blue);
                    break;
                case 3:
                    holder.getPillAmount().setImageResource(R.drawable.three_blue);
                    break;
                case 4:
                    holder.getPillAmount().setImageResource(R.drawable.four_blue);
                    break;
                case 5:
                    holder.getPillAmount().setImageResource(R.drawable.five_blue);
                    break;
                case 6:
                    holder.getPillAmount().setImageResource(R.drawable.six_blue);
                    break;
                case 7:
                    holder.getPillAmount().setImageResource(R.drawable.seven_blue);
                    break;
                case 8:
                    holder.getPillAmount().setImageResource(R.drawable.eight_blue);
                    break;
                case 9:
                    holder.getPillAmount().setImageResource(R.drawable.nine_blue);
                    break;
                case 10:
                    holder.getPillAmount().setImageResource(R.drawable.ten_blue);
                    break;
                default:
                    break;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }



        int flag;
        try {
            flag = temp.getInt("flag");
            if(flag == 1){
                //before taken time

                holder.getTick().setVisibility(View.INVISIBLE);
                holder.getPillName().setTextColor(Color.BLACK);
//                holder.getPillAmount().setVisibility(View.VISIBLE);




            } else if(flag == 2) {
                //not have pills in time

                holder.getTick().setImageResource(R.drawable.red_cross);
                holder.getTick().setVisibility(View.VISIBLE);
                holder.getPillName().setTextColor(Color.RED);
//                holder.getPillAmount().setVisibility(View.INVISIBLE);
            } else{
                //have pills in time

                holder.getTick().setImageResource(R.drawable.green_tick);
                holder.getTick().setVisibility(View.VISIBLE);
                holder.getPillName().setTextColor(Color.GREEN);
                holder.getPillAmount().setVisibility(View.INVISIBLE);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(view.getContext(), "test: " + position, Toast.LENGTH_SHORT).show();
//            }
//        });


    }


    @Override
    public int getItemCount() {
        return schedule.size();
    }

    public void addItem(JSONObject newSchedule) {
        schedule.add(newSchedule);
        notifyItemInserted(schedule.size());
    }

    public void removeItem(int position) {
        schedule.remove(position);
        notifyItemRemoved(position);
//        notifyItemRangeChanged(position, schedule.size());
    }

    public void moveItemToLast(int position) {
        Collections.swap(schedule, position, schedule.size() - 1);
        notifyItemMoved(position, schedule.size() - 1);
    }




}
