package com.adherence.adherence;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class MedicationListAdapter extends RecyclerView.Adapter<MedicationListAdapter.ViewHolder>{

//    private String[] medicineListHardcode;
//    private String[] detailListHardcode;
//    private String[] timeListHardcode;
    private ArrayList<Prescription> prescriptions;
    private Context context;
    private ViewHolder vh;
    private MedicationFragment medicationFragment;

    public static final int CAMERA_REQUEST = 101;

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

    public MedicationListAdapter(ArrayList<Prescription> prescriptions, Context context, MedicationFragment medicationFragment) {
        this.prescriptions = prescriptions;
        this.context = context;
        this.medicationFragment = medicationFragment;
    }

    @Override
    public MedicationListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.medication_listitem, viewGroup, false);
        vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MedicationListAdapter.ViewHolder vh, final int position) {
        final Prescription prescription = prescriptions.get(position);
        prescription.setListItemPosition(position);
        vh.getMedicineName().setText(prescription.getName());
        vh.getMedicineDetail().setText(prescription.getNote());
        vh.getTime().setText(prescription.getTimeList());
        if (!prescription.isHaveImage()){
            vh.getMedicineImage().setImageResource(R.drawable.capsule);

        }else{
            vh.getMedicineImage().setImageDrawable(null);
            vh.getMedicineImage().setImageBitmap(prescription.getImage());
        }

        vh.getMedicineImage().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(view, prescription);
            }
        });

    }

    @Override
    public int getItemCount() {
        return prescriptions.size();
    }

    private void selectImage(final View view, final Prescription prescription){
        final CharSequence[] options = { "Take Photo", "Choose from Gallery"};

        final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Add a Pill Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    medicationFragment.captureImage(prescription.getListItemPosition(), prescription.getName());
                }
                else if (options[item].equals("Choose from Gallery")) {
                    medicationFragment.chooseFromGallery(prescription.getListItemPosition(), prescription.getName());
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }



    private File saveBitmap(Bitmap bmp, String filename) {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        OutputStream outStream = null;
        Log.d("extStorageDirectory", extStorageDirectory);
        // String temp = null;
        File file = new File(extStorageDirectory, filename + ".png");
        if (file.exists()) {
            file.delete();
            file = new File(extStorageDirectory, filename + ".png");

        }

        try {
            outStream = new FileOutputStream(file);
            //// TODO: 7/21/17 calculate compression ratio before compress image into required size;
            bmp.compress(Bitmap.CompressFormat.PNG, 50, outStream);
            outStream.flush();
            outStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    public void setImageInItem(int position, Bitmap imageMap) {
        Prescription dataSet = prescriptions.get(position);
        saveBitmap(imageMap, dataSet.getName());
        dataSet.setImage(imageMap);
        dataSet.setHaveImage(true);
        notifyDataSetChanged();
    }


}
