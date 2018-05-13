package it.univaq.casatracking.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import it.univaq.casatracking.model.Utente;

public class Request extends AsyncTask<Object, Void, String> {


    @Override
    protected String doInBackground(Object... objects) {

        Context context = (Context) objects[0];
        String request = (String) objects[1];
        Utente utente = (Utente) objects[2];
        LatLng loc = (LatLng) objects[3];

        String result = "";

        if(!isConnected(context)){
            return "NO INTERNET CONNECTION";
        }

        switch (request){
            case "monitor":
                result = doMonitorRequest(utente.getNumeroTelefono(), loc.latitude, loc.longitude);
                break;
        }

        return result;
    }

    private String doMonitorRequest(String phone, double lat, double lon){

        //handle connection, request and return response

        //address
        String address = "http://www.smartengineers.eu/joomla/components/com_locationtracking/script/script.php";
        //get request
        address += "?task=monitor" + "&phone=" + phone + "&lat=" + lat + "&lon=" + lon;

        //send to server
        HttpURLConnection con = null;

        String result = "";

        try {

            URL url = new URL(address);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");


            //read response
            int responseCode = con.getResponseCode();
            boolean success = false;

            if (responseCode == HttpURLConnection.HTTP_OK) {
                success = true;
            } else {
                success = false;
            }

            System.out.println("\nSending 'GET' request to URL : " + address);
            System.out.println("Response Code : " + responseCode + "\nsuccess: " + success);

            //read response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();


            System.out.println("RESPONSE: " + response.toString());

            result = response.toString();

        } catch(IOException e){
            e.printStackTrace();
        } finally {
            con.disconnect();
        }

        return result;
    }

    /* CONNECTION IS ACTIVE */

    private boolean isConnected(Context context){

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        boolean isConnected = false;

        try {
            activeNetwork = cm.getActiveNetworkInfo();
        } catch (NullPointerException e1){
            e1.printStackTrace();
        }

        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();


        return isConnected;
    }

}
