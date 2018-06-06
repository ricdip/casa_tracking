package it.univaq.casatracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.univaq.casatracking.model.POI;
import it.univaq.casatracking.model.Percorso;
import it.univaq.casatracking.model.Utente;
import it.univaq.casatracking.utils.Preferences;
import it.univaq.casatracking.utils.RequestHandler;

public class POIActivity extends AppCompatActivity {

    //TODO : CLASSE POIActivity INCOMPLETA

    private Utente utente;
    private Percorso percorso;
    private List<POI> pois;
    private String alert;

    private LocationManager mManager;
    private LocationListener listener = new LocationListener() {

        private LatLng loc;

        boolean isGPSready = false;

        @Override
        public void onLocationChanged(Location location) {

            String provider = location.getProvider();

            if(provider.equals(LocationManager.GPS_PROVIDER)){
                if(!isGPSready)
                    isGPSready = true;
            } else if(provider.equals(LocationManager.NETWORK_PROVIDER)){
                //gps is better
                if(isGPSready)
                    return;
                //gps not ready, use network
            }

            double lat = location.getLatitude();
            double lng = location.getLongitude();

            loc = new LatLng(lat, lng);

            String res = RequestHandler.navigazione(getApplicationContext(), utente, loc, percorso.getId(), alert);

            if(res == null){
                return;
            }

            try {

                // TODO : HANDLING NAVIGAZIONE REQUEST
                JSONObject navigazione_json = new JSONObject(res);

                if(navigazione_json.get("alert") instanceof Integer)
                    alert = String.valueOf((Integer)navigazione_json.get("alert"));
                else
                    alert = (String)navigazione_json.get("alert");

                JSONArray array = navigazione_json.getJSONArray("pois");
                pois = new ArrayList<POI>();
                Gson gson = new Gson();

                //pois creation
                for(int i = 0; i < array.length(); i++){
                    JSONObject item = array.optJSONObject(i);
                    pois.add(gson.fromJson(item.toString(), POI.class));
                }

                //debug
                System.out.println("alert: " + alert);
                //debug
                for(POI poi : pois)
                    System.out.println(poi);

            } catch(JSONException e){
                e.printStackTrace();
            }


        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_no_gps), Toast.LENGTH_LONG).show();
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_choice);

        utente = Preferences.loadUtente(getApplicationContext());

        String percorso_json = (String) getIntent().getStringExtra("percorso");
        Gson gson = new Gson();
        percorso = gson.fromJson(percorso_json, Percorso.class);

    }

    @Override
    protected void onResume() {
        super.onResume();

        //access location
        mManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //debug
        System.out.println(percorso);

        //check permissions
        int checkPerms = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        if(checkPerms == PackageManager.PERMISSION_GRANTED){
            //location perms granted
            //network
            mManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5*1000, 0, listener);
            //gps
            mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5*1000, 0, listener);

        } else {
            //location perms not granted, request perms

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mManager != null){
            mManager.removeUpdates(listener);
            mManager = null;
        }
    }


    /* Richiedi permessi */
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //minTime in ms
                //minDistance in m
                //network
                mManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5*1000, 0, listener);
                //gps
                mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5*1000, 0, listener);

            }
        }
    }


    /* MENU PREFERENZE */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.logout:
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                //clear activity stack
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
