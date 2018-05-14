package it.univaq.casatracking.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;

public class Dialog {

    private static Dialog instance = null;
    private static Context context;

    private static AlertDialog.Builder builder;

    public Dialog getInstance(Context ctx){

       context = ctx;

        if(instance == null)
            instance = new Dialog();

        return instance;
    }

    public AlertDialog.Builder getBuilder(Activity activity){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(activity);
        }

        return builder;

    }

}
