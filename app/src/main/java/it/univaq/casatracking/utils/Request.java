package it.univaq.casatracking.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import it.univaq.casatracking.R;
import it.univaq.casatracking.model.Utente;

public class Request extends AsyncTask<Object, Void, Object> {


    @Override
    protected Object doInBackground(Object... objects) {

        Context context = (Context) objects[0];
        String request = (String) objects[1];
        Utente utente;
        LatLng loc;
        int id_percorso;
        String alert;
        String nome_foto;

        Object result = "";

        if(!isConnected(context)){
            return "{\"error\": \"NO INTERNET CONNECTION\"}";
        }

        switch (request){
            case "monitor":
                utente = (Utente) objects[2];
                loc = (LatLng) objects[3];
                result = doMonitorRequest(context, utente.getNumeroTelefono(), loc.latitude, loc.longitude);
                break;

            case "get_percorsi":
                utente = (Utente) objects[2];
                result = doGetPercorsiRequest(context, utente.getNumeroTelefono());
                break;

            case "navigazione":
                utente = (Utente) objects[2];
                loc = (LatLng) objects[3];
                id_percorso = Integer.parseInt((String) objects[4]);
                alert = (String) objects[5];
                result = doNavigazioneRequest(context, utente.getNumeroTelefono(), loc.latitude, loc.longitude, id_percorso, alert);
                break;

            case "download_image":
                nome_foto = (String) objects[2];
                loc = (LatLng) objects[3];
                Bitmap bmp = doDownloadImageRequest(context,nome_foto, loc.latitude, loc.longitude);

                if(bmp == null)
                    result = "{\"error\": \"DOWNLOAD IMAGE ERROR\"}";
                else
                    result = bmp;

                break;
        }

        return result;
    }

    private String doMonitorRequest(Context context, String phone, double lat, double lon){

        //handle connection, request and return response

        //address
        String address = context.getString(R.string.server_path);

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

            if(!success){
                return "{\"error\":\"code " + responseCode + "\"}";
            }

            /*
            System.out.println("\nSending 'GET' request to URL : " + address);
            System.out.println("Response Code : " + responseCode + "\nsuccess: " + success);
            */

            //read response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();


            result = response.toString();

        } catch(IOException e){
            e.printStackTrace();

        } finally {
            if(con != null)
                con.disconnect();

        }

        return result;
    }

    private String doGetPercorsiRequest(Context context, String phone){

        //handle connection, request and return response

        //address
        String address = context.getString(R.string.server_path);

        //get request
        address += "?task=get_percorsi" + "&phone=" + phone;

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

            if(!success){
                return "{\"error\":\"code " + responseCode + "\"}";
            }

            /*
            System.out.println("\nSending 'GET' request to URL : " + address);
            System.out.println("Response Code : " + responseCode + "\nsuccess: " + success);
            */

            //read response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();


            result = response.toString();

        } catch(IOException e){
            e.printStackTrace();

        } finally {
            if(con != null)
                con.disconnect();

        }

        return result;
    }

    private String doNavigazioneRequest(Context context, String phone, double lat, double lon, int id_percorso, String alert){

        //handle connection, request and return response

        //address
        String address = context.getString(R.string.server_path);

        //get request
        address += "?task=navigazione" + "&phone=" + phone + "&lat=" + lat + "&lon=" + lon + "&id=" + id_percorso + "&alert=" + alert;

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

            if(!success){
                return "{\"error\":\"code " + responseCode + "\"}";
            }

            /*
            System.out.println("\nSending 'GET' request to URL : " + address);
            System.out.println("Response Code : " + responseCode + "\nsuccess: " + success);
            */

            //read response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();


            result = response.toString();

        } catch(IOException e){
            e.printStackTrace();

        } finally {
            if(con != null)
                con.disconnect();

        }

        return result;
    }

    private Bitmap doDownloadImageRequest(Context context, String nome_foto, double lat, double lon){

        //TODO : bug check request and Images class

        Bitmap bmp = null;
        //images helper
        Images mImages = Images.getInstance(context);

        //handle connection, request and return response image or image if exists

        //address
        String address = context.getString(R.string.poi_images_path);

        //get request
        if(!nome_foto.equals("STREETVIEW"))
            address += nome_foto;
        else{
            address = "http://maps.googleapis.com/maps/api/streetview?size=200x200&location="+lat+","+lon+"&fov=90&heading=100&pitch=10&sensor=false";
        }

        if(mImages.exists(nome_foto)){
            //return image if exists
            bmp = mImages.loadImage(nome_foto);
            return bmp;
        }

        //send request to server
        HttpURLConnection con = null;

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

            if(!success){
                return null;
            }

            /*
            System.out.println("\nSending 'GET' request to URL : " + address);
            System.out.println("Response Code : " + responseCode + "\nsuccess: " + success);
            */

            //success, create image
            bmp = mImages.saveImage(nome_foto, con.getInputStream());

        } catch(IOException e){
            e.printStackTrace();

        } finally {
            if(con != null)
                con.disconnect();

        }

        return bmp;
    }

    /* CONNECTION IS ACTIVE */

    public static boolean isConnected(Context context){

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
