package it.univaq.casatracking.services;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import it.univaq.casatracking.NavigazioneLiberaActivity;
import it.univaq.casatracking.ScegliPercorsoActivity;
import it.univaq.casatracking.model.Utente;
import it.univaq.casatracking.utils.Preferences;
import it.univaq.casatracking.utils.RequestHandler;

import static android.content.ContentValues.TAG;

public class Services extends IntentService {

    public static final String ACTION_CALL_EDUCATORE = "action_call_educatore";
    public static final String ACTION_SEND_SMS = "action_send_sms";
    public static final String ACTION_ALERT = "action_alert";
    public static final String ACTION_TAKE_A_PICTURE = "action_take_a_picture";
    public static final String ACTION_DOWNLOAD_PERCORSI = "action_download_percorsi";


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

                case ACTION_TAKE_A_PICTURE:
                    handleUploadPicture(intent.getStringExtra("image_path"), (LatLng) intent.getExtras().get("loc"));
                    break;

                case ACTION_DOWNLOAD_PERCORSI:
                    downloadPercorsi();
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

        String phoneNumber = Preferences.loadUtente(getApplicationContext()).getNumeroTelefonoEducatore();
        SmsManager smsManager = SmsManager.getDefault();

        if(smsManager == null){
            //APRE MESSAGGI PER MANDARE SMS!!
            Uri uri = Uri.parse("smsto:" + phoneNumber);
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
            smsIntent.putExtra("sms_body", body);

        } else {
            //messaggio automatico
            smsManager.sendTextMessage(phoneNumber, null, body, null, null);
        }

    }

    private void alert(String body){
        sendSMS(body);
        callEducatore();
    }

    private void handleUploadPicture(String imagepath, @Nullable LatLng loc){

        boolean success = false;
        Intent response = new Intent(NavigazioneLiberaActivity.ACTION_SERVICE_COMPLETED);

        try {

            if(loc == null){
                response.putExtra("success", false);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(response);
                return;
            }

            String result = RequestHandler.uploadImage(getApplicationContext(), imagepath, loc);
            JSONObject result_json = new JSONObject(result);

            if(result_json.has("error")){
                Log.d(TAG, (String) result_json.get("error"));

            } else if(result_json.has("photo")){
                int photo = result_json.getInt("photo");

                if(photo == 0)
                    //success
                    success = true;
                else
                    //not success
                    success = false;

            } else {
                Log.d(TAG, "UPLOAD IMAGE: " + result);

            }

        } catch(JSONException e){
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }

        if(success)
            response.putExtra("success", true);
        else
            response.putExtra("success", false);


        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(response);

    }

    private void downloadPercorsi(){

        Intent response = new Intent(ScegliPercorsoActivity.ACTION_SERVICE_COMPLETED);
        Utente utente = Preferences.loadUtente(getApplicationContext());


        //download percorsi
        String result = RequestHandler.getPercorsi(getApplicationContext(), utente);

        response.putExtra("data", result);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(response);

    }

}
