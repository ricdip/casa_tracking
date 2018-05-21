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
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import it.univaq.casatracking.model.Utente;
import it.univaq.casatracking.services.Services;
import it.univaq.casatracking.utils.Preferences;
import it.univaq.casatracking.utils.Request;

public class NavigazioneLiberaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "NavigazioneLibera";

    private GoogleMap mMap;
    private LocationManager mManager;

    private Button callButton;

    private Utente utente;
    private boolean notify_cancelled;

    //alert user with alert sound and vibration
    private MediaPlayer mp;
    private Vibrator v;
    private long[] mVibratePattern;

    //alert only called once
    private static boolean alertIsActive;

    /* handler per autocall */
    //auto chiamata in 15 secondi
    private static final int TIME_OUT_AUTOMATIC_CALL = 15000;
    private static boolean dismissed = false;

    private Handler autoCallHandler = new Handler();

    private LatLng autoCallRunnableLatLng;
    private Runnable autoCallRunnable = new Runnable() {

        @Override
        public void run() {

            autoCallHandler.removeCallbacks(autoCallRunnable);

            if(!dismissed){

                mp.pause();
                v.cancel();

                alertIsActive = false;
                notify_cancelled = true;

                //sms con coordinate a educatore e successiva chiamata
                Intent doAlert = new Intent(getApplicationContext(), Services.class);
                doAlert.setAction(Services.ACTION_ALERT);
                //pattern link: https://www.google.com/maps/@42.0458585,13.9318123,15z
                doAlert.putExtra("sms_body", "SONO QUI: " + "https://www.google.com/maps/@" + autoCallRunnableLatLng.latitude + "," + autoCallRunnableLatLng.longitude + ",15z");
                startService(doAlert);

            }

            //if dialog dismissed
            //no action

        }
    };
    /* END handler per autocall */

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
    /* END receiver for internet connection changes */

    /* location change listener */
    private LocationListener listener = new LocationListener() {

        private Marker myMarker;
        private LatLng loc;
        MarkerOptions options;

        @Override
        public void onLocationChanged(Location location) {

            double lat = location.getLatitude();
            double lng = location.getLongitude();

            loc = new LatLng(lat, lng);

            //connection to server
            Request request = new Request();
            request.execute(getApplicationContext(), "monitor", utente, loc);

            //opzioni
            options = new MarkerOptions();

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
                JSONObject json = new JSONObject(res);

                // print response (debug)
                if(json.has("error")){
                    String error = json.getString("error");
                    Log.d(TAG, "Response error" + error);
                    return;
                }
                // /print response (debug)

                String alert = json.getString("alert");

                if(alert.equals("1") && (!notify_cancelled)){
                    //utente fuori area sicura
                    alert(loc);
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
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_no_gps), Toast.LENGTH_LONG).show();
        }
    };


    //END ACTION HANDLERS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigazione_libera);

        callButton = findViewById(R.id.navigazionelibera_callButton);

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
        utente = Preferences.loadUtente(getApplicationContext());

        //boolean notify per allertare utente dell' area non sicura
        notify_cancelled = false;

        //boolean alert is active
        alertIsActive = false;

        //creazione ring per alert
        //Uri alert_ring = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mp = MediaPlayer.create(getApplicationContext(), R.raw.alert);
        mp.setLooping(true);

        //creazione vibrazione per alert
        v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        //vibrate pattern
        mVibratePattern = new long[]{0, 400, 200, 400};

        //
        //setting mapfragment
        //
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
            Snackbar snackbar = Snackbar.make(findViewById(R.id.navigazionelibera_constraint), getApplicationContext().getString(R.string.snackbar_no_internet), Snackbar.LENGTH_LONG);
            snackbar.show();

            //preparo intent per ripresa connessione
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

            //receiver registrato
            isReceiverRegistered = true;
            getApplicationContext().registerReceiver(receiver, filter);

            return;
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


    /* metodo chiamato quando la mappa è pronta */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //disabilita gestures sulla mappa
        mMap.getUiSettings().setAllGesturesEnabled(false);
        // Sets the map type to be "normal"
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }


    /* metodo chiamato se alert=1, ovvero se l'utente non è più nell'area sicura */
    private synchronized void alert(final LatLng loc){

        /* BOOLEAN PER ATOMICITA' DELLA FUNZIONE ALERT */
        if(alertIsActive){
            return;
        }
        alertIsActive = true;


        //start alert sonoro
        mp.start();

        //start vibrazione
        // -1 : Do not repeat this pattern, 0 if you want to repeat this pattern from 0th index
        v.vibrate(mVibratePattern, 0);

        dismissed = false;

        //creazione dialog chiamata
        AlertDialog.Builder builder = new AlertDialog.Builder(NavigazioneLiberaActivity.this, android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(getApplicationContext().getString(R.string.alert_title))
                .setMessage(getApplicationContext().getString(R.string.alert_call_educatore)
)
                .setPositiveButton(R.string.button_si, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        mp.pause();
                        v.cancel();

                        //alert a educatore con send sms
                        Intent doAlert = new Intent(getApplicationContext(), Services.class);
                        doAlert.setAction(Services.ACTION_ALERT);
                        //https://www.google.com/maps/@42.0458585,13.9318123,15z
                        doAlert.putExtra("sms_body", "SONO QUI: " + "https://www.google.com/maps/@" + loc.latitude + "," + loc.longitude + ",15z");
                        startService(doAlert);

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
                        v.cancel();

                        //solo avviso con sms all'educatore che utente è fuori area sicura
                        Intent sms = new Intent(getApplicationContext(), Services.class);
                        sms.setAction(Services.ACTION_SEND_SMS);
                        sms.putExtra("sms_body", "SONO FUORI DALLA MIA AREA SICURA: " + "https://www.google.com/maps/@" + loc.latitude + "," + loc.longitude + ",15z");
                        startService(sms);

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
        autoCallRunnableLatLng = loc;
        autoCallHandler.postDelayed(autoCallRunnable, TIME_OUT_AUTOMATIC_CALL);

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

}
