package com.adherence.adherence;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.view.Gravity;
import android.widget.TextView;

import java.io.Serializable;

public class Dialogs implements Serializable
{
    public static AlertDialog makeAboutDialog(Activity activity)
    {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.about)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                    }
                })
                .create();

        return setAboutDialogContents(activity, dialog);
    }

    private static AlertDialog setAboutDialogContents(Activity activity, AlertDialog dialog)
    {
        String app_name = activity.getString(R.string.app_name);
        String link = activity.getString(R.string.about_link);
        String version = getAppVersion(activity.getApplicationContext());

        String msg = String.format("%s\nv%s\n%s", app_name, version, link);

        TextView messageView = new TextView(activity);
        messageView.setText(msg);
        messageView.setGravity(Gravity.CENTER_HORIZONTAL);
        messageView.setTextSize(20.0f);
        dialog.setView(messageView);

        return dialog;
    }

    private static String getAppVersion(Context context)
    {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        String versionName = ""; // initialize String

        if (packageManager != null && packageName != null)
        {
            try
            {
                versionName = packageManager.getPackageInfo(packageName, 0).versionName;
            }
            catch (PackageManager.NameNotFoundException e)
            {
            }
        }

        return versionName;
    }

}