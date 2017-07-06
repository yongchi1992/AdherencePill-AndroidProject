package com.adherence.adherence;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;



public class MedicationListAdapter extends RecyclerView.Adapter<MedicationListAdapter.ViewHolder>{

    private String[] medicineListHardcode;
    private String[] detailListHardcode;
    private String[] timeListHardcode;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View listRootView;

        public TextView getMedicineName() {
            return medicineName;
        }
        public TextView getMedicineDetail() {return medicineDetail; }
        public ImageView getMedicineImage() {return medicineImage; }
        public TextView getTime() {return time;}

        private TextView medicineName;
        private TextView medicineDetail;
        private ImageView medicineImage;
        private TextView time;


        public ViewHolder(View v1) {
            super(v1);
            listRootView = v1;
            medicineName = (TextView) v1.findViewById(R.id.medicine_name);
            medicineDetail = (TextView) v1.findViewById(R.id.medicine_detail);
            medicineImage = (ImageView) v1.findViewById(R.id.medicine_image);
            time = (TextView) v1.findViewById(R.id.time);

        }
    }

    public MedicationListAdapter(String[] hardCode, String[] detailCode, String[] time) {
        medicineListHardcode = hardCode;
        detailListHardcode = detailCode;
        timeListHardcode = time;
    }

    @Override
    public MedicationListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.medication_listitem, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MedicationListAdapter.ViewHolder vh, int position) {
        vh.getMedicineName().setText(medicineListHardcode[position]);
        vh.getMedicineDetail().setText(detailListHardcode[position]);
        vh.getTime().setText(timeListHardcode[position]);
//        vh.getTime().setText("morning \n afternoon \n night \n bedtime");

    }

    @Override
    public int getItemCount() {
        return medicineListHardcode.length;
    }


}
