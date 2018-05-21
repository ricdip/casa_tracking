package it.univaq.casatracking.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import it.univaq.casatracking.R;

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
        //System.out.println("Refreshed token: " + token);

        //sends this token to the server
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        //
        //SERVER NON PRONTO AD INVIARE NOTIFICHE PUSH
        //
        //sendRegistrationToServer(token);
    }

    private boolean sendRegistrationToServer(String token) {

        boolean success = false;
        HttpURLConnection connection = null;

        try {

            URL url = new URL(getApplicationContext().getString(R.string.server_path));

            connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setDoInput(true);

            connection.setRequestMethod("POST");

            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());

            dos.writeBytes("token=" + token);

            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                success = true;
            } else {
                success = false;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(connection != null)
                connection.disconnect();
        }

        return success;

    }
}