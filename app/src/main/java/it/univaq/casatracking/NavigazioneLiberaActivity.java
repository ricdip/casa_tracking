package it.univaq.casatracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import it.univaq.casatracking.model.Utente;
import it.univaq.casatracking.services.Services;
import it.univaq.casatracking.utils.Dialog;
import it.univaq.casatracking.utils.Request;

public class NavigazioneLiberaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager mManager;

    private ImageButton callButton;

    private Utente utente;
    private boolean notify_cancelled;
    private MediaPlayer mp;
    private Vibrator v;
    private long[] mVibratePattern;

    //alert only called once
    private static boolean alertIsActive;

    /* handler per autocall */
    private static final int TIME_OUT_AUTOMATIC_CALL = 10000;
    private static boolean dismissed = false;

    private Handler autoCallHandler = new Handler();

    private Runnable autoCallRunnable = new Runnable() {
        @Override
        public void run() {

            autoCallHandler.removeCallbacks(autoCallRunnable);

            if(!dismissed){

                mp.pause();
                //mp.release();

                v.cancel();
                alertIsActive = false;
                notify_cancelled = true;

                //call educatore
                Intent i = new Intent(getApplicationContext(), Services.class);
                i.setAction(Services.ACTION_CALL_EDUCATORE);
                startService(i);

            } else {
                //dialog dismissed
                //no action

            }

        }
    };

    /* receiver for internet connection changes */
    private static boolean isReceiverRegistered = false;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //entro in receiver e la connessione è stata ristabilita, ridisegnamo la finestra
            if(Request.isConnected(getApplicationContext())){
                onResume();
            }

        }
    };


    /* location change listener */
    private LocationListener listener = new LocationListener() {

        private Marker myMarker;

        @Override
        public void onLocationChanged(Location location) {

            double lat = location.getLatitude();
            double lng = location.getLongitude();

            LatLng loc = new LatLng(lat, lng);

            //connection to server
            Request request = new Request();
            request.execute(getApplicationContext(), "monitor", utente, loc);

            //opzioni
            MarkerOptions options = new MarkerOptions();

            options.position(loc);
            options.title("SONO QUI");
            /*options.snippet("info")*/

            //update location on map
            if(mMap != null){

                if(myMarker != null)
                    myMarker.remove();

                myMarker = mMap.addMarker(options);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 18f));

            }

            //retrieve response
            try {

                String res = request.get();

                //print response (debug)

                JSONObject json = new JSONObject(res);

                if(json.has("error")){
                    String error = json.getString("error");
                    System.out.println("SERVER RESPONSE error: " + error);
                    return;
                }

                String alert = json.getString("alert");

                //System.out.println("SERVER RESPONSE: " + res + "\n" + "ALERT " + alert);

                if(alert.equals("1") && (!notify_cancelled)){
                    //utente fuori area sicura
                    alert();
                }

            } catch(InterruptedException|ExecutionException|JSONException e){
                e.printStackTrace();
            }


        }

        //END onLocationChanged

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "ATTIVA LA LOCALIZZAZIONE GPS", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigazione_libera);

        callButton = findViewById(R.id.callButton1);

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Call action
                Intent i = new Intent(getApplicationContext(), Services.class);
                i.setAction(Services.ACTION_CALL_EDUCATORE);
                startService(i);

            }
        });

        //get utente
        Intent i = getIntent();
        String utente_json = i.getStringExtra("utente");
        Gson gson = new Gson();
        utente = gson.fromJson(utente_json, Utente.class);

        //boolean notify per area non sicura
        notify_cancelled = false;

        //boolean alert is active
        alertIsActive = false;

        //creazione ring per alert
        Uri alert_ring = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mp = MediaPlayer.create(getApplicationContext(), alert_ring);

        //creazione vibrazione per alert
        v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        //vibrate pattern
        mVibratePattern = new long[]{0, 400, 200, 400};

        //setting mapfragment
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!Request.isConnected(getApplicationContext())){
            //snackbar creation
            Snackbar snackbar = Snackbar.make(findViewById(R.id.navigazionelibera_relative), "NESSUNA CONNESSIONE INTERNET", Snackbar.LENGTH_LONG);
            snackbar.show();

            //preparo intent per ripresa connessione
            //preparing to download
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            //receiver registrato
            isReceiverRegistered = true;
            getApplicationContext().registerReceiver(receiver, filter);

            return;
        }

        //TEST se utente è utente di default

        if(utente.isTest()){
            notify_cancelled = true;

            //alert dialog
            AlertDialog.Builder builder = new Dialog().getInstance(getApplicationContext()).getBuilder(NavigazioneLiberaActivity.this);

            builder.setTitle("ATTENZIONE")
                    .setMessage("Login come utente di default\n")
                    .setNegativeButton(R.string.button_procedi, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(true)
                    .show();

        }

        //access location
        mManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //check permissions
        int checkPerms = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(checkPerms == PackageManager.PERMISSION_GRANTED){
            //location perms granted

            mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, listener);

        } else {
            //location perms not granted, request perms

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if(isReceiverRegistered){
            getApplicationContext().unregisterReceiver(receiver);
            isReceiverRegistered = false;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.stop();
        mp.release();
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
                mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, listener);

            }
        }
    }


    /* metodo chiamato quando la mappa è pronta */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Sets the map type to be "normal"
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
            case R.id.preferenze:
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                i.putExtra("backpage", "navigazioneliberaactivity");
                startActivity(i);

                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /* metodo chiamato se alert=1, ovvero se l'utente non è più nell'area sicura */
    private synchronized void alert(){

        /* BOOLEAN PER ATOMICITA' DELLA FUNZIONE ALERT */
        if(alertIsActive){
            return;
        }
        alertIsActive = true;


        //messaggio sonoro
        mp.start();

        // -1 : Do not repeat this pattern
        // pass 0 if you want to repeat this pattern from 0th index
        v.vibrate(mVibratePattern, 0);

        dismissed = false;

        //creazione dialog chiamata
        AlertDialog.Builder builder = new Dialog().getInstance(getApplicationContext()).getBuilder(NavigazioneLiberaActivity.this);

        builder.setTitle("ATTENZIONE")
                .setMessage("CHIAMARE L'EDUCATORE ?")
                .setPositiveButton(R.string.button_si, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        mp.pause();
                        //mp.release();
                        v.cancel();

                        //call educatore
                        Intent i = new Intent(getApplicationContext(), Services.class);
                        i.setAction(Services.ACTION_CALL_EDUCATORE);
                        startService(i);

                        notify_cancelled = true;
                        dismissed = true;
                        dialog.dismiss();

                        alertIsActive = false;
                        autoCallHandler.removeCallbacks(autoCallRunnable);

                    }
                })
                .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        mp.pause();
                        //mp.release();
                        v.cancel();

                        notify_cancelled = true;
                        dismissed = true;
                        dialog.dismiss();

                        alertIsActive = false;
                        autoCallHandler.removeCallbacks(autoCallRunnable);
                    }
                })

                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .show();

        // autocall in TIME_OUT_AUTOMATIC_CALL ms
        autoCallHandler.postDelayed(autoCallRunnable, TIME_OUT_AUTOMATIC_CALL);

    }

    @Override
    public void onBackPressed() {

        if(Looper.getMainLooper().getThread().equals(Thread.currentThread())){

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);

        }

        super.onBackPressed();

    }

}
