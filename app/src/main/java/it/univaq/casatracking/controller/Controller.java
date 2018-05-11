package it.univaq.casatracking.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;

import it.univaq.casatracking.utils.Preferences;

public class Controller {

    private static Controller instance = null;
    private static Context context;

    public static Controller getInstance(Context ctx){

        context = ctx;
        if(instance == null)
            instance = new Controller();

        return instance;
    }

    public void callEducatore(Activity activity){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + Preferences.load(context).getNumeroTelefonoEducatore()));

        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Call permission is not granted, fallback to send sms

            Uri uri = Uri.parse("smsto:" + Preferences.load(context).getNumeroTelefonoEducatore());
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
            smsIntent.putExtra("sms_body", "RICHIAMAMI, sono in difficoltà");
            context.startActivity(smsIntent);

        } else {
            // Call permission granted, call
            context.startActivity(callIntent);
        }


    }

}
