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
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;

import it.univaq.casatracking.model.Utente;
import it.univaq.casatracking.services.RequestService;
import it.univaq.casatracking.services.Services;
import it.univaq.casatracking.utils.Images;
import it.univaq.casatracking.utils.Player;
import it.univaq.casatracking.utils.Preferences;
import it.univaq.casatracking.utils.Request;

public class NavigazioneLiberaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "NavigazioneLibera";

    private GoogleMap mMap;
    private LocationManager mManager;

    private Button callButton;
    private Button takeAPhotoButton;

    private Utente utente;
    private boolean notify_cancelled;

    private String alert;

    private static final int TAKE_A_PICTURE = 0;

    //alert only called once
    private static boolean alertIsActive;

    /* handler per autocall */
    //auto chiamata in 15 secondi
    private static final int TIME_OUT_AUTOMATIC_CALL = 15000;
    private static boolean dismissed = false;

    private Handler autoCallHandler = new Handler();
    private LatLng location_variable;

    private LatLng autoCallRunnableLatLng;
    private Runnable autoCallRunnable = new Runnable() {

        @Override
        public void run() {

            autoCallHandler.removeCallbacks(autoCallRunnable);

            if(!dismissed){

                Player.getInstance(getApplicationContext()).stopPlaying();

                alertIsActive = false;
                notify_cancelled = true;

                //sms con coordinate a educatore e successiva chiamata
                Intent doAlert = new Intent(getApplicationContext(), Services.class);
                doAlert.setAction(Services.ACTION_ALERT);
                doAlert.putExtra("sms_body", "SONO FUORI DALLA MIA AREA SICURA");
                doAlert.putExtra("loc",  autoCallRunnableLatLng);

                startService(doAlert);

                //cancel wake up
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

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

    /* receiver for services */
    public static final String ACTION_TAKE_A_PHOTO_SERVICE_COMPLETED = "action_take_a_photo_service_completed";
    public static final String ACTION_ALERT_SERVICE_COMPLETED = "action_alert_service_completed";
    private BroadcastReceiver receiver_for_services = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent == null || intent.getAction() == null) return;

            switch (intent.getAction()){
                case ACTION_TAKE_A_PHOTO_SERVICE_COMPLETED:
                    boolean success = ((Boolean) intent.getExtras().get("data")).booleanValue();

                    if(success)
                        //success
                        Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_photo_upload_success), Toast.LENGTH_SHORT).show();
                    else
                        //not success
                        Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_photo_upload_not_success), Toast.LENGTH_SHORT).show();

                    break;

                case ACTION_ALERT_SERVICE_COMPLETED:
                    alert = intent.getStringExtra("data");

                    // if error return
                    if(alert == null) {
                        return;
                    }

                    if(alert.equals("1") && (!notify_cancelled)){
                        //utente fuori area sicura
                        alert(location_variable);
                    }

                    break;

            }

        }
    };
    /* END receiver for services */

    /* location change listener */
    private LocationListener listener = new LocationListener() {

        private Marker myMarker;
        MarkerOptions options = new MarkerOptions();

        boolean isGPSready = false;

        @Override
        public void onLocationChanged(Location location) {

            String provider = location.getProvider();
            //System.out.println(location);

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

            location_variable = new LatLng(lat, lng);

            //opzioni
            //options = new MarkerOptions();

            options.position(location_variable);
            options.title("SONO QUI");
            /*options.snippet("info")*/

            //update location on map
            if(mMap != null){

                if(myMarker != null)
                    myMarker.remove();

                myMarker = mMap.addMarker(options);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location_variable, 18f));

            }

            //retrieve response
            //alert = RequestHandler2.monitor(getApplicationContext(), utente, loc);

            //prepare service
            Intent i = new Intent(getApplicationContext(), RequestService.class);
            i.setAction(RequestService.ACTION_MONITOR);
            i.putExtra("loc", location_variable);
            i.putExtra("className", "NavigazioneLiberaActivity");
            //start service
            startService(i);

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
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_no_gps), Toast.LENGTH_SHORT).show();
        }
    };

    //
    //END ACTION HANDLERS
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigazione_libera);

        callButton = findViewById(R.id.navigazionelibera_callButton);
        takeAPhotoButton = findViewById(R.id.navigazionelibera_take_a_photoButton);

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Call action
                Intent i = new Intent(getApplicationContext(), Services.class);
                i.setAction(Services.ACTION_CALL_EDUCATORE_WITH_SMS);
                i.putExtra("loc", location_variable);
                startService(i);

            }
        });

        takeAPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //upload image action
                takeAPicture();
            }
        });

        //get utente
        utente = Preferences.loadUtente(getApplicationContext());

        //boolean notify per allertare utente dell' area non sicura
        notify_cancelled = false;

        //boolean alert is active
        alertIsActive = false;


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

        //preparing take a picture
        IntentFilter filter_take_a_picture_receiver = new IntentFilter();
        filter_take_a_picture_receiver.addAction(ACTION_TAKE_A_PHOTO_SERVICE_COMPLETED);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver_for_services, filter_take_a_picture_receiver);

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

        //preparing take a picture
        IntentFilter filter_alert_receiver = new IntentFilter();
        filter_alert_receiver.addAction(ACTION_ALERT_SERVICE_COMPLETED);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver_for_services, filter_alert_receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(isReceiverRegistered){
            getApplicationContext().unregisterReceiver(receiver);
            isReceiverRegistered = false;
        }

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver_for_services);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mManager != null){
            mManager.removeUpdates(listener);
            mManager = null;
        }
    }


    /* metodo chiamato quando la mappa è pronta */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //disabilita gestures sulla mappa
        mMap.getUiSettings().setAllGesturesEnabled(false);
        // Sets the map type to be "normal"
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //access location
        mManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //check permissions
        int checkPerms = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

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


    /* metodo chiamato se alert=1, ovvero se l'utente non è più nell'area sicura */
    private void alert(final LatLng loc){

        /* BOOLEAN PER ATOMICITA' DELLA FUNZIONE ALERT */
        if(alertIsActive){
            return;
        }
        alertIsActive = true;

        Player.getInstance(getApplicationContext()).startPlaying();

        dismissed = false;

        //creazione dialog chiamata
        AlertDialog.Builder builder = new AlertDialog.Builder(NavigazioneLiberaActivity.this, android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(getApplicationContext().getString(R.string.alert_title))
                .setMessage(getApplicationContext().getString(R.string.alert_call_educatore)
)
                .setPositiveButton(R.string.button_si, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Player.getInstance(getApplicationContext()).stopPlaying();

                        //alert a educatore con send sms
                        Intent doAlert = new Intent(getApplicationContext(), Services.class);
                        doAlert.setAction(Services.ACTION_ALERT);
                        doAlert.putExtra("sms_body", "SONO FUORI DALLA MIA AREA SICURA");
                        doAlert.putExtra("loc", loc);
                        startService(doAlert);

                        notify_cancelled = true;
                        dismissed = true;
                        dialog.dismiss();

                        alertIsActive = false;
                        //cancel wake up
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                        autoCallHandler.removeCallbacks(autoCallRunnable);

                    }
                })
                .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        Player.getInstance(getApplicationContext()).stopPlaying();


                        if(!Preferences.checkAutomaticSMS(getApplicationContext())){
                            //not automatic sms
                            //solo avviso con sms all'educatore che utente è fuori area sicura
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(NavigazioneLiberaActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                            builder2.setTitle(getApplicationContext().getString(R.string.alert_title))
                                    .setMessage(getApplicationContext().getString(R.string.alert_sms_educatore))
                                    .setPositiveButton(R.string.button_si, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent sms = new Intent(getApplicationContext(), Services.class);
                                            sms.setAction(Services.ACTION_SEND_SMS);
                                            sms.putExtra("sms_body", "SONO FUORI DALLA MIA AREA SICURA");
                                            sms.putExtra("loc", loc);
                                            startService(sms);

                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setCancelable(false)
                                    .show();
                        } else {
                            //automatic sms
                            Intent sms = new Intent(getApplicationContext(), Services.class);
                            sms.setAction(Services.ACTION_SEND_SMS);
                            sms.putExtra("sms_body", "SONO FUORI DALLA MIA AREA SICURA");
                            sms.putExtra("loc", loc);
                            startService(sms);
                        }

                        notify_cancelled = true;
                        dismissed = true;
                        dialog.dismiss();

                        alertIsActive = false;

                        //cancel wake up
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                        autoCallHandler.removeCallbacks(autoCallRunnable);
                    }
                })

                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .show();

        //wake up phone
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

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


    /* UPLOAD PICTURE */

    private String ImageAbsolutePath;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case TAKE_A_PICTURE:
                if(resultCode == RESULT_OK){
                    //photo taken

                    if(location_variable == null){
                        Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_take_a_picture_no_gps), Toast.LENGTH_LONG).show();
                        return;
                    }

                    //prepare service
                    Intent i = new Intent(getApplicationContext(), RequestService.class);
                    i.setAction(RequestService.ACTION_UPLOAD_IMAGE);
                    i.putExtra("image_path", ImageAbsolutePath);
                    i.putExtra("loc", location_variable);
                    i.putExtra("className", "NavigazioneLiberaActivity");
                    //start service
                    startService(i);

                }
                break;
        }

    }

    public void takeAPicture(){

        if(location_variable == null){
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_take_a_picture_no_gps), Toast.LENGTH_LONG).show();
            return;
        }

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String img = Preferences.loadUtente(getApplicationContext()).getNumeroTelefono() + "_" + System.currentTimeMillis() + ".jpg";

        File file = new File(Images.getInstance(getApplicationContext()).getCacheDirectoryPath(), img);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".it.univaq.casatracking.provider", file);

        i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        ImageAbsolutePath = file.getAbsolutePath();
        this.startActivityForResult(i, TAKE_A_PICTURE);
    }

}
