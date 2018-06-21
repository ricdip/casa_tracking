package it.univaq.casatracking.services;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import it.univaq.casatracking.R;
import it.univaq.casatracking.model.Utente;
import it.univaq.casatracking.utils.Preferences;
import it.univaq.casatracking.utils.Request;

import static android.content.ContentValues.TAG;

public class CasaTrackingFirebaseInstanceIDService extends FirebaseInstanceIdService {

    public CasaTrackingFirebaseInstanceIDService() {
        super();
    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        // Get updated InstanceID token.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + token);

        //sends this token to the server
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        //
        //Send token to server if user is registered
        //
        boolean success = false;
        if(!Preferences.checkFirstAccess(getApplicationContext())){

            if(!Request.isConnected(getApplicationContext())){
                //save token in shared preferences
                Preferences.saveFirebaseToken(getApplicationContext(), token);
                Log.d(TAG, "Token stored in shared preferences: no internet connection");
                return;
            }

            success = sendRegistrationToServer(token, getApplicationContext(), Preferences.loadUtente(getApplicationContext()));

            if(success){
                Log.d(TAG, "Token successfully sent to server");
                Preferences.saveFirebaseToken(getApplicationContext(), "");
            } else{
                Log.d(TAG, "Error in send token to server");
                Preferences.saveFirebaseToken(getApplicationContext(), token);
            }
        }
    }

    public static boolean sendRegistrationToServer(@Nullable String token, Context context, Utente utente) {

        if(token == null){
            return false;
        }

        boolean success = false;
        HttpURLConnection connection = null;

        try {

            URL url = new URL(context.getString(R.string.firebase_server_path));

            connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setDoInput(true);

            connection.setRequestMethod("POST");

            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());

            dos.writeBytes("task=" + "store_data" + "&token=" + token.trim() + "&phone=" + utente.getNumeroTelefono().trim() + "&userName=" + utente.getNome());
            dos.flush();
            dos.close();

            //connection.connect(); /*implicit call*/

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                success = true;
            } else {
                success = false;
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if(connection != null)
                connection.disconnect();

        }

        return success;
    }
}
