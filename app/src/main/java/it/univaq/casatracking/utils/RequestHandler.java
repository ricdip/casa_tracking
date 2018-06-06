package it.univaq.casatracking.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import it.univaq.casatracking.model.Utente;

import static android.content.ContentValues.TAG;

public class RequestHandler {

    public static String monitor(Context context, Utente utente, LatLng loc){

        String result = null;
        String alert = null;
        JSONObject alert_json = null;
        Request request = null;

        try {
            request = new Request();
            request.execute(context, "monitor", utente, loc);

            result = (String) request.get();

            alert_json = new JSONObject(result);

            // print response (debug)
            if(alert_json.has("error")){
                String error = alert_json.getString("error");
                Log.d(TAG, "Monitor response error" + error);
                return null;
            }
            // /print response (debug)

            alert = alert_json.getString("alert");

        } catch(InterruptedException|ExecutionException|JSONException e){
            e.printStackTrace();
        }

        return alert;
    }

    public static String getPercorsi(Context context, Utente utente){
        String result = null;
        Request request = null;

        try {
            request = new Request();
            request.execute(context, "get_percorsi", utente);

            result = (String) request.get();

            // print response (debug)
            if(result.equals("")){
                Log.d(TAG, "GetPercorsi response error");
                return null;
            }
            // /print response (debug)

        } catch(InterruptedException|ExecutionException e){
            e.printStackTrace();
        }

        return result;
    }

    public static String navigazione(Context context, Utente utente, LatLng loc, int id_percorso, String alert){
        String result = null;
        Request request = null;
        JSONObject result_json = null;

        try {
            request = new Request();
            request.execute(context, "navigazione", utente, loc, new Integer(id_percorso), alert);

            result = (String) request.get();

            result_json = new JSONObject(result);

            // print response (debug)
            if(result_json.has("error")){
                String error = result_json.getString("error");
                Log.d(TAG, "Navigazione response error" + error);
                return null;
            }
            // /print response (debug)

        } catch(InterruptedException|ExecutionException|JSONException e){
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
            request.execute(context, "download_image", nome_foto, loc);

            result = request.get();

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

        } catch(InterruptedException|ExecutionException|JSONException e){
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
            request.execute(context, "upload_image", image_path, loc);

            result = (String) request.get();
            result_json = new JSONObject(result);

            // print response (debug)
            if(result_json.has("error")){
                String error = result_json.getString("error");
                Log.d(TAG, error);
                return result;
            }
            // /print response (debug)

        } catch(InterruptedException|ExecutionException|JSONException e){
            e.printStackTrace();
        }

        return result;
    }

}
