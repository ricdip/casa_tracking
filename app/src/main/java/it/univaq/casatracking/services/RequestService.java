package it.univaq.casatracking.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import it.univaq.casatracking.NavigazioneLiberaActivity;
import it.univaq.casatracking.POIActivity;
import it.univaq.casatracking.ScegliPercorsoActivity;
import it.univaq.casatracking.model.Utente;
import it.univaq.casatracking.utils.Preferences;
import it.univaq.casatracking.utils.Request;

import static android.content.ContentValues.TAG;

public class RequestService extends IntentService {

    public static final String ACTION_MONITOR = "action_monitor";
    public static final String ACTION_GET_PERCORSI = "action_get_percorsi";
    public static final String ACTION_NAVIGAZIONE = "action_navigazione";
    public static final String ACTION_DOWNLOAD_IMAGE = "action_download_image";
    public static final String ACTION_UPLOAD_IMAGE = "action_upload_image";

    private static final String NAME = Services.class.getSimpleName();

    public RequestService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if(intent != null && intent.getAction() != null){
            String action = intent.getAction();
            Intent response = null;
            Object result = null;

            try {

                switch(action){
                    case ACTION_MONITOR:

                        if(intent.getStringExtra("className").equals("POIActivity")){
                            response = new Intent(getApplicationContext(), POIActivity.class);
                            response.setAction(POIActivity.ACTION_ALERT_SERVICE_COMPLETED);
                        } else {
                            response = new Intent(getApplicationContext(), NavigazioneLiberaActivity.class);
                            response.setAction(NavigazioneLiberaActivity.ACTION_ALERT_SERVICE_COMPLETED);
                        }

                        result = monitor(getApplicationContext(), Preferences.loadUtente(getApplicationContext()), (LatLng) intent.getExtras().get("loc"));

                        break;

                    case ACTION_GET_PERCORSI:
                        response = new Intent(getApplicationContext(), ScegliPercorsoActivity.class);
                        response.setAction(ScegliPercorsoActivity.ACTION_SERVICE_COMPLETED);
                        result = getPercorsi(getApplicationContext(), Preferences.loadUtente(getApplicationContext()));

                        break;

                    case ACTION_NAVIGAZIONE:
                        response = new Intent(getApplicationContext(), POIActivity.class);
                        response.setAction(POIActivity.ACTION_NAVIGAZIONE_SERVICE_COMPLETED);
                        result = navigazione(getApplicationContext(), Preferences.loadUtente(getApplicationContext()), (LatLng) intent.getExtras().get("loc"), (Integer) intent.getExtras().get("id_percorso"), (String) intent.getExtras().get("alert"));

                        break;

                    case ACTION_DOWNLOAD_IMAGE:
                        response = new Intent(getApplicationContext(), POIActivity.class);
                        response.setAction(POIActivity.ACTION_DOWNLOAD_PHOTO_SERVICE_COMPLETED);
                        result = downloadImage(getApplicationContext(), (String) intent.getExtras().get("nome_foto"), (LatLng) intent.getExtras().get("loc"));

                        break;

                    case ACTION_UPLOAD_IMAGE:

                        if(intent.getStringExtra("className").equals("POIActivity")){
                            response = new Intent(getApplicationContext(), POIActivity.class);
                            response.setAction(POIActivity.ACTION_TAKE_A_PHOTO_SERVICE_COMPLETED);
                        } else {
                            response = new Intent(getApplicationContext(), NavigazioneLiberaActivity.class);
                            response.setAction(NavigazioneLiberaActivity.ACTION_TAKE_A_PHOTO_SERVICE_COMPLETED);
                        }

                        result = uploadImage(getApplicationContext(), (String) intent.getExtras().get("image_path"), (LatLng) intent.getExtras().get("loc"));

                        if(result.equals("0"))
                            result = Boolean.TRUE;
                        else if(result.equals("1"))
                            result = Boolean.FALSE;
                        else
                            result = Boolean.FALSE;

                        break;

                }

                if(result instanceof String)
                    response.putExtra("data", (String) result);
                else if(result instanceof Bitmap)
                    response.putExtra("data", (Bitmap) result);
                else if(result instanceof Boolean)
                    response.putExtra("data", (Boolean) result);
                else
                    response.putExtra("data", (String) null);


                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(response);

            } catch(NullPointerException e){
                e.printStackTrace();
            }

        }
    }

    public static String monitor(Context context, Utente utente, LatLng loc){

        String result = null;
        String alert = null;
        JSONObject alert_json = null;
        Request request = null;

        try {
            request = new Request();

            result = (String) request.doRequest(context, "monitor", utente, loc);

            alert_json = new JSONObject(result);

            // print response (debug)
            if(alert_json.has("error")){
                String error = alert_json.getString("error");
                Log.d(TAG, "Monitor response error" + error);
                return null;
            }
            // /print response (debug)

            alert = alert_json.getString("alert");

        } catch(JSONException e){
            e.printStackTrace();
        }

        return alert;
    }

    public static String getPercorsi(Context context, Utente utente){

        Request request = new Request();
        String result = (String) request.doRequest(context, "get_percorsi", utente);

        // print response (debug)
        if(result.equals("")){
            Log.d(TAG, "GetPercorsi response error");
            return null;
        }
        // /print response (debug)

        return result;
    }

    public static String navigazione(Context context, Utente utente, LatLng loc, int id_percorso, String alert){
        String result = null;
        Request request = null;
        JSONObject result_json = null;

        try {
            request = new Request();
            result = (String) request.doRequest(context, "navigazione", utente, loc, new Integer(id_percorso), alert);

            result_json = new JSONObject(result);

            // print response (debug)
            if(result_json.has("error")){
                String error = result_json.getString("error");
                Log.d(TAG, "Navigazione response error" + error);
                return null;
            }
            // /print response (debug)

        } catch(JSONException e){
            e.printStackTrace();
        }

        return result;
    }

    public static Bitmap downloadImage(Context context, String nome_foto, LatLng loc){
        Object result = null;
        Request request = null;
        JSONObject result_json = null;
        Bitmap bmp = null;

        try {
            request = new Request();
            result = request.doRequest(context, "download_image", nome_foto, loc);

            if(result instanceof String){
                result_json = new JSONObject((String) result);

                // print response (debug)
                if(result_json.has("error")){
                    String error = result_json.getString("error");
                    Log.d(TAG, error);
                    return null;
                }
                // /print response (debug)
                return null;
            }

            //if not, result is a Bitmap
            bmp = (Bitmap) result;

        } catch(JSONException e){
            e.printStackTrace();
        }

        return bmp;
    }

    public static String uploadImage(Context context, String image_path, LatLng loc){
        String result = null;
        Request request = null;
        JSONObject result_json = null;

        try {
            request = new Request();
            result = (String) request.doRequest(context, "upload_image", image_path, loc);

            result_json = new JSONObject(result);

            // print response (debug)
            if(result_json.has("error")){
                String error = result_json.getString("error");
                Log.d(TAG, error);
                return result;
            }
            // /print response (debug)

            if(result_json.has("photo")){
                int photo = result_json.getInt("photo");
                return String.valueOf(photo);
            }

            Log.d(TAG, "error in uploading image");

        } catch(JSONException e){
            e.printStackTrace();
        }

        return result;
    }

}
