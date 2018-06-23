package it.univaq.casatracking.services;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.util.StringTokenizer;

import it.univaq.casatracking.model.Utente;
import it.univaq.casatracking.utils.Preferences;
import it.univaq.casatracking.utils.Request;

import static android.content.ContentValues.TAG;

public class Services extends IntentService {

    public static final String ACTION_CALL_EDUCATORE = "action_call_educatore";
    public static final String ACTION_CALL_EDUCATORE_WITH_SMS = "action_call_educatore_with_sms";
    public static final String ACTION_SEND_SMS = "action_send_sms";
    public static final String ACTION_ALERT = "action_alert";
    public static final String ACTION_SEND_DATA_TO_FIREBASE_SERVER = "action_send_data_to_firebase_server";

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

                case ACTION_CALL_EDUCATORE_WITH_SMS:
                    callEducatore((LatLng) intent.getExtras().get("loc"));
                    break;

                case ACTION_SEND_SMS:
                    sendSMS(intent.getStringExtra("sms_body"), (LatLng) intent.getExtras().get("loc"));
                    break;

                case ACTION_ALERT:
                    alert(intent.getStringExtra("sms_body"), (LatLng) intent.getExtras().get("loc"));
                    break;

                case ACTION_SEND_DATA_TO_FIREBASE_SERVER:
                    send_data_to_firebase_server(intent.getStringExtra("data"));
                    break;

            }
        }
    }

    /* handling methods for requested service */

    private void callEducatore(LatLng loc){

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + Preferences.loadUtente(getApplicationContext()).getNumeroTelefonoEducatore()));

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Call permission is not granted, fallback to send sms

            sendSMS("RICHIAMAMI, sono in difficoltà", loc);

        } else {
            // Call permission granted, call
            sendSMS("SONO QUI", loc);
            getApplicationContext().startActivity(callIntent);


        }

    }

    private void callEducatore(){

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + Preferences.loadUtente(getApplicationContext()).getNumeroTelefonoEducatore()));

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Call permission is not granted, fallback to send sms

            sendSMS("RICHIAMAMI, sono in difficoltà", null);

        } else {
            // Call permission granted, call

            getApplicationContext().startActivity(callIntent);

        }

    }

    private void sendSMS(String body, LatLng loc){

        //https://www.google.com/maps/@42.0458585,13.9318123,15z
        //http://maps.google.com/maps?z=18&q=10.8061,106.7130

        if(loc != null)
            body += ": \n" + "http://maps.google.com/maps?z=18&q=" + loc.latitude + "," + loc.longitude;
        else
            body += ": \n" + "[coordinate gps non disponibili]";

        String educatore = Preferences.loadUtente(getApplicationContext()).getNumeroTelefonoEducatore();
        String emergenza = Preferences.loadUtente(getApplicationContext()).getNumeroEmergenza();
        String tel_numbers = educatore + ";" + emergenza;
        SmsManager smsManager = SmsManager.getDefault();

        if(smsManager == null){
            //APRE MESSAGGI PER MANDARE SMS!!
            Uri uri = Uri.parse("smsto:" + tel_numbers);
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
            smsIntent.putExtra("sms_body", body);

        } else {
            //messaggio automatico
            StringTokenizer st=new StringTokenizer(tel_numbers,";");
            while (st.hasMoreElements())
            {
                String tempMobileNumber = (String)st.nextElement();
                if(tempMobileNumber.length()>0 && body.trim().length()>0) {
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(Uri.parse("smsto:" + tempMobileNumber).toString(), null, body, null, null);
                }
            }

        }

    }

    private void alert(String body, LatLng loc){
        sendSMS(body, loc);
        callEducatore();
    }

    private void send_data_to_firebase_server(String utenteJSON){

        Gson gson = new Gson();
        Utente utente = gson.fromJson(utenteJSON, Utente.class);

        //send update to firebase_server
        String token = FirebaseInstanceId.getInstance().getToken();
        boolean success = false;

        if(token == null){
            Log.d(TAG, "null token");
            return;
        }

        if(!Request.isConnected(getApplicationContext())){
            //save token in shared preferences
            Preferences.saveFirebaseToken(getApplicationContext(), token);
            Log.d(TAG, "Token stored in shared preferences: no internet connection");
            return;
        }

        success = CasaTrackingFirebaseInstanceIDService.sendRegistrationToServer(token, getApplicationContext(), utente);

        if(success){
            Log.d(TAG, "Token successfully sent to server");
            Preferences.saveFirebaseToken(getApplicationContext(), "");
        } else{
            Log.d(TAG, "Error in send token to server");
            Preferences.saveFirebaseToken(getApplicationContext(), token);
        }
    }

}
