package com.adherence.adherence;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.name;

/**
 * Created by sam on 17/6/18.
 */


public class TodayListAdapter4 extends RecyclerView.Adapter {

    /*
    show like this:
    pillname
    time: amount
     */

    private ArrayList<JSONObject> schedule;
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();
    private LayoutInflater mInflater;
    private RecyclerView recyclerView;
    private String sessionToken;


    private RequestQueue mRequestQueue;




    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View listRootView;
        private TextView pillName;

        private TextView period;
        private ImageView tick;
        private ImageView pillAmount;
        private TextView pillNote;

        private SwipeRevealLayout swipeLayout;
        private LinearLayout hideLayout;
        private TextView taken;
        private TextView skip;

        private LinearLayout expandView;
        private TextView details;
        private ImageView toggle;








        public TextView getPillName(){
            return pillName;
        }
        public TextView getPeriod() {return period;}
        public ImageView getTick() {return tick;}
        public TextView getPillNote() {return pillNote;}
        public ImageView getPillAmount() {return pillAmount;}


        public SwipeRevealLayout getSwipeLayout() {return swipeLayout;}
        public LinearLayout getHideLayout() {return hideLayout;}
        public TextView getTaken() {return taken;}
        public TextView getSkip() {return  skip;}

        public TextView getDetails() {return  details;}
        public LinearLayout getExpandView() {return expandView;}




        public ViewHolder(View itemView) {
            super(itemView);
            listRootView=itemView;
            pillName= (TextView) itemView.findViewById(R.id.pill_name4);
            period = (TextView) itemView.findViewById(R.id.period);
            tick = (ImageView) itemView.findViewById(R.id.tick4);
            pillNote = (TextView) itemView.findViewById(R.id.pillNote4);
            pillAmount = (ImageView) itemView.findViewById(R.id.pillAmount4);

            swipeLayout = (SwipeRevealLayout) itemView.findViewById(R.id.today_SL);
            hideLayout = (LinearLayout) itemView.findViewById(R.id.hide_layout);
            taken = (TextView) itemView.findViewById(R.id.taken);
            skip = (TextView) itemView.findViewById(R.id.skip);

            expandView = (LinearLayout) itemView.findViewById(R.id.expandView);
            details = (TextView) itemView.findViewById(R.id.details);
            toggle = (ImageView) itemView.findViewById(R.id.toggle);


        }


    }

    public TodayListAdapter4(Context context, ArrayList<JSONObject> schedule, RecyclerView recyclerView, String sessionToken){
        this.schedule = schedule;
        this.mInflater = LayoutInflater.from(context);
        this.recyclerView = recyclerView;
        this.sessionToken = sessionToken;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v=mInflater.inflate(R.layout.today_listitem4, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        mRequestQueue = Volley.newRequestQueue(v.getContext());
        return vh;
    }



    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder h, final int position) {



        final ViewHolder holder = (ViewHolder) h;

        final JSONObject temp = schedule.get(position);


        String name = null;
        String period = null;
        final String url = holder.itemView.getContext().getString(R.string.parseURL);
        try {
            name =temp.getString("name");
            period = temp.getString("period");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String finalName = name;
        final String finalPeriod = period;


        holder.expandView.setVisibility(View.GONE);



        holder.toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean shouldExpand = holder.getExpandView().getVisibility() == View.GONE;

                ChangeBounds transition = new ChangeBounds();
                transition.setDuration(125);

                if (shouldExpand) {
                    holder.details.setText("Description!!!!!!!!!!!!");
                    holder.expandView.setVisibility(View.VISIBLE);
//                    Toast.makeText(view.getContext(), "Pressed on item!", Toast.LENGTH_SHORT).show();
                    holder.toggle.setImageResource(R.drawable.collapse_icon);


                    binderHelper.closeLayout(finalName + finalPeriod);
                    binderHelper.lockSwipe(finalName + finalPeriod);


                } else {
                    holder.expandView.setVisibility(View.GONE);
                    holder.toggle.setImageResource(R.drawable.expand_icon);
                    try {
                        if (temp.getInt("flag") != 3) {
                            binderHelper.unlockSwipe(finalName + finalPeriod);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                TransitionManager.beginDelayedTransition(recyclerView, transition);
                holder.itemView.setActivated(shouldExpand);
            }
        });



        try {
            binderHelper.bind(holder.getSwipeLayout(), finalName + finalPeriod);
            holder.getPillName().setText(finalName);
            holder.getPeriod().setText(finalPeriod);
            holder.getPillNote().setText(temp.getString("note"));

            holder.getTaken().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

//                        Toast.makeText(view.getContext(), "Taken " + temp.getString("name"), Toast.LENGTH_SHORT).show();
                    JSONObject postObj = new JSONObject();
                    try {
                        temp.put("flag", 3);

//                        postObj.put("Name", temp.getString("name"));
                        postObj.put("Name", "SC36-05  4C:55:CC:10:7B:12");
                        postObj.put("timeStamp", temp.get("timeStamp")+ ", " + new SimpleDateFormat("MM/dd/yy").format(new Date()));
//                        Log.d("post test timestamp", temp.get("timeStamp")+ "," + new SimpleDateFormat("MM/dd/yy").format(new Date()));
                        addItem(temp);
                        removeItem(holder.getAdapterPosition());

                        binderHelper.closeLayout(finalName + finalPeriod);
                        binderHelper.lockSwipe(finalName + finalPeriod);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                    String reqUrl = url + "/bottleUpdate";

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, reqUrl, postObj, new Response.Listener() {
                        @Override
                        public void onResponse(Object response) {
                            Log.d("post", "success");
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("post error", error.toString());
                        }
                    }){
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("x-parse-session-token", sessionToken);
                            return headers;
                        }
                    };
                    mRequestQueue.add(request);
                }
            });

            holder.getSkip().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    addItem(temp);
                    removeItem(holder.getAdapterPosition());

                    binderHelper.closeLayout(finalName + finalPeriod);




                }
            });


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
                binderHelper.lockSwipe(finalName + finalPeriod);

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
