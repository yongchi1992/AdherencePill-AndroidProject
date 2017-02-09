package com.adherence.adherence;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public View listRootView;
        private TextView pillName;
        private TextView time_amount;
        public TextView getPillName(){
            return pillName;
        }
        public TextView getTimeAmount(){
            return time_amount;
        }

        public ViewHolder(View itemView) {
            super(itemView);
            listRootView=itemView;
            pillName= (TextView) itemView.findViewById(R.id.pill_name);
            time_amount= (TextView) itemView.findViewById(R.id.time_amount);
        }
    }

    public TodayListAdapter(ArrayList<String> pillName, ArrayList<String> time_amount){
        this.pillName=new ArrayList<String>(pillName);
        this.time_amount=new ArrayList<String>(time_amount);
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
        holder.getTimeAmount().setText(time_amount.get(position));
    }

    @Override
    public int getItemCount() {
        return time_amount.size();
    }


}
