package com.adherence.adherence;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by caoye on 17/2/8.
 */

public class TodayListAdapter extends RecyclerView.Adapter<TodayListAdapter.ViewHolder> {

    /*
    show like this:
    pillname
    time: amount
     */
    private ArrayList<String> pillName;
    private ArrayList<String> time_amount;


    private ArrayList<Integer> flag;


    public static class ViewHolder extends RecyclerView.ViewHolder{
        public View listRootView;
        private TextView pillName;
        private TextView time_amount;
        private RelativeLayout pillLayout;
        private ImageView tick;
        private TextView pillAmount;


        public TextView getPillName(){
            return pillName;
        }
        public TextView getTimeAmount(){
            return time_amount;
        }
        public RelativeLayout getLayout() { return pillLayout; }
        public ImageView getTick() {return tick;}
//        public TextView getPillAmount() {return pillAmount;}


        public ViewHolder(View itemView) {
            super(itemView);
            listRootView=itemView;
            pillName= (TextView) itemView.findViewById(R.id.pill_name3);
            time_amount= (TextView) itemView.findViewById(R.id.time_amount);
            pillLayout = (RelativeLayout) itemView.findViewById(R.id.pill_layout);
            tick = (ImageView) itemView.findViewById(R.id.tick);
//            pillAmount = (TextView) itemView.findViewById(R.id.pill_num);
        }
    }

    public TodayListAdapter(ArrayList<String> pillName, ArrayList<String> time_amount, ArrayList<Integer> flag){
        this.pillName=new ArrayList<String>(pillName);
        this.time_amount=new ArrayList<String>(time_amount);
        this.flag=new ArrayList<Integer>(flag);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.today_listitem,viewGroup,false);
        ViewHolder vh=new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(TodayListAdapter.ViewHolder holder, int position) {
        holder.getPillName().setText(pillName.get(position));
        holder.getTimeAmount().setText(time_amount.get(position).substring(0, 8));
//        holder.getPillAmount().setText(time_amount.get(position).substring(15, 16));
        //flag==1, not arrive time
        if(flag.get(position)==1){

//            holder.getTimeAmount().setTextColor(Color.BLACK);


        }else if(flag.get(position)==2) {
            //not have pills in time
//            holder.getTimeAmount().setTextColor(Color.RED);

            //holder.getLayout().setBackgroundResource(R.drawable.prescription_button_nottaken);
            holder.getTick().setImageResource(R.drawable.red_cross);
            holder.getTick().setVisibility(View.VISIBLE);
        }
        else{
            //have pills in time
//            holder.getTimeAmount().setTextColor(Color.parseColor("#01a532"));
//            holder.getLayout().setBackgroundColor(Color.GREEN);
            //holder.getLayout().setBackgroundResource(R.drawable.prescription_button);
//            holder.getLayout().setBackgroundResource(R.drawable.prescription_button_nottaken);
            holder.getTick().setImageResource(R.drawable.green_tick);
            holder.getTick().setVisibility(View.VISIBLE);

        }
    }

    @Override
    public int getItemCount() {
        return time_amount.size();
    }


}
