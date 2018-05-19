package it.univaq.casatracking.services;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;

import it.univaq.casatracking.utils.Preferences;

public class Services extends IntentService {

    public static final String ACTION_CALL_EDUCATORE = "action_call_educatore";
    public static final String ACTION_SEND_SMS = "action_send_sms";
    public static final String ACTION_ALERT = "action_alert";

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

                case ACTION_SEND_SMS:
                    sendSMS(intent.getStringExtra("sms_body"));
                    break;

                case ACTION_ALERT:
                    alert(intent.getStringExtra("sms_body"));
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

            sendSMS("RICHIAMAMI, sono in difficoltà");

        } else {
            // Call permission granted, call

            getApplicationContext().startActivity(callIntent);
        }

    }

    private void sendSMS(String body){
        /*
        APRE MESSAGGI PER MANDARE SMS!!
        Uri uri = Uri.parse("smsto:" + Preferences.loadUtente(getApplicationContext()).getNumeroTelefonoEducatore());
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
        smsIntent.putExtra("sms_body", body);
        */

        String phoneNumber = Preferences.loadUtente(getApplicationContext()).getNumeroTelefonoEducatore();
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, body, null, null);

    }

    private void alert(String body){
        sendSMS(body);
        callEducatore();
    }

}
