package it.univaq.casatracking.services;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import it.univaq.casatracking.utils.Preferences;

public class Services extends IntentService {

    public static final String ACTION_CALL_EDUCATORE = "action_call_educatore";

    private static final String NAME = Services.class.getSimpleName();

    public Services() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent != null && intent.getAction() != null) {

            String action = intent.getAction();
            switch (action) {
                case ACTION_CALL_EDUCATORE:
                    callEducatore();
                    break;

            }
        }
    }

    /* handling methods for requested service */

    private void callEducatore(){

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + Preferences.loadUtente(getApplicationContext()).getNumeroTelefonoEducatore()));

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Call permission is not granted, fallback to send sms

            sendSMS();

        } else {
            // Call permission granted, call

            getApplicationContext().startActivity(callIntent);
        }

    }

    private void sendSMS(){

        Uri uri = Uri.parse("smsto:" + Preferences.loadUtente(getApplicationContext()).getNumeroTelefonoEducatore());

        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
        smsIntent.putExtra("sms_body", "RICHIAMAMI, sono in difficoltà");

        getApplicationContext().startActivity(smsIntent);

    }

}
