package it.univaq.casatracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import it.univaq.casatracking.model.POI;
import it.univaq.casatracking.model.Percorso;
import it.univaq.casatracking.model.Utente;
import it.univaq.casatracking.services.Services;
import it.univaq.casatracking.utils.Images;
import it.univaq.casatracking.utils.Player;
import it.univaq.casatracking.utils.Preferences;
import it.univaq.casatracking.utils.RequestHandler;
import it.univaq.casatracking.utils.Timer;

public class POIActivity extends AppCompatActivity {

    //TODO : DEGUG E REVISIONE classe POIActivity

    private Utente utente;
    private Percorso percorso;
    private POI poi;
    private String alert;

    private ImageView navigazione_immagini_immagine;
    private TextView navigazione_immagini_descrizione;
    public static TextView navigazione_immagini_timeout;
    private TextView navigazione_immagini_titolo;

    private Button navigazioneimmagini_take_a_photoButton;
    private Button navigazioneimmagini_callButton;

    private Timer timer;

    private static final int TAKE_A_PICTURE = 2;

    //alert only called once
    private static boolean alertIsActive;

    private boolean notify_cancelled;

    /* handler per alert */
    private static int TIME_OUT_AUTOMATIC_ALERT;
    private LatLng location_for_picture_and_alerthandler;
    private Handler alertHandler = new Handler();
    private Runnable alertRunnable = new Runnable() {
        @Override
        public void run() {
            alertHandler.removeCallbacks(alertRunnable);
            alert(location_for_picture_and_alerthandler);
        }
    };
    /* END handler per alert */

    /* handler per autocall */
    //auto chiamata in 15 secondi
    private Handler autoCallHandler = new Handler();
    private static final int TIME_OUT_AUTOMATIC_CALL = 15000;
    private static boolean dismissed = false;
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
                //pattern link: https://www.google.com/maps/@42.0458585,13.9318123,15z
                doAlert.putExtra("sms_body", "NAVIGAZIONE PER IMMAGINI: " + "https://www.google.com/maps/@" + autoCallRunnableLatLng.latitude + "," + autoCallRunnableLatLng.longitude + ",15z");
                startService(doAlert);

                //cancel wake up
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

            }

            //if dialog dismissed
            //no action

        }
    };
    /* END handler per autocall */

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
            location_for_picture_and_alerthandler = loc;

            String res = RequestHandler.navigazione(getApplicationContext(), utente, loc, percorso.getId(), alert);

            if(res == null){
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_poi_error), Toast.LENGTH_LONG).show();
                return;
            }

            try {

                JSONObject navigazione_json = new JSONObject(res);

                //alert retrieve
                if(navigazione_json.get("alert") instanceof Integer)
                    alert = String.valueOf((Integer)navigazione_json.get("alert"));
                else
                    alert = (String)navigazione_json.get("alert");

                //pois retrieve
                if(navigazione_json.has("pois")){
                    JSONArray array = navigazione_json.getJSONArray("pois");
                    Gson gson = new Gson();
                    POI newpoi = gson.fromJson(array.getJSONObject(0).toString(), POI.class);

                    if(poi == null){
                        //first poi
                        poi = newpoi;
                        navigazione_immagini_titolo.setText(poi.getNome());
                        navigazione_immagini_descrizione.setText(poi.getDescrizione());
                        navigazione_immagini_immagine.setImageBitmap(RequestHandler.downloadImage(getApplicationContext(), poi.getFoto(), loc));

                        timer.startTimer();

                        Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_new_poi), Toast.LENGTH_SHORT).show();
                    }

                    if(poi.getId() != newpoi.getId()){
                        //poi change
                        poi = newpoi;
                        navigazione_immagini_titolo.setText(poi.getNome());
                        navigazione_immagini_descrizione.setText(poi.getDescrizione());
                        navigazione_immagini_immagine.setImageBitmap(RequestHandler.downloadImage(getApplicationContext(), poi.getFoto(), loc));

                        Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_new_poi), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    poi = null;
                    navigazione_immagini_titolo.setText(getApplicationContext().getString(R.string.toast_no_pois));
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_no_pois), Toast.LENGTH_SHORT).show();
                }

                //alert check
                if(alert.equals("1") && !notify_cancelled)
                    alert(loc);

                //debug
                System.out.println("alert: " + alert);
                //debug
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
        setContentView(R.layout.activity_navigazione_immagini);

        navigazione_immagini_titolo = findViewById(R.id.navigazione_immagini_titolo);
        navigazione_immagini_immagine = findViewById(R.id.navigazione_immagini_immagine);
        navigazione_immagini_descrizione = findViewById(R.id.navigazione_immagini_descrizione);
        navigazione_immagini_timeout = findViewById(R.id.navigazione_immagini_timeout);

        navigazioneimmagini_take_a_photoButton = findViewById(R.id.navigazioneimmagini_take_a_photoButton);
        navigazioneimmagini_callButton = findViewById(R.id.navigazioneimmagini_callButton);

        navigazioneimmagini_callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Call action
                Intent i = new Intent(getApplicationContext(), Services.class);
                i.setAction(Services.ACTION_CALL_EDUCATORE);
                startService(i);

            }
        });

        navigazioneimmagini_take_a_photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //upload image action
                takeAPicture();
            }
        });

        navigazione_immagini_timeout.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().equals(getApplicationContext().getString(R.string.timesup))){
                    // alert in TIME_OUT_AUTOMATIC_CALL ms
                    alertHandler.postDelayed(alertRunnable, TIME_OUT_AUTOMATIC_ALERT);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        utente = Preferences.loadUtente(getApplicationContext());

        String percorso_json = (String) getIntent().getStringExtra("percorso");
        Gson gson = new Gson();
        percorso = gson.fromJson(percorso_json, Percorso.class);

        poi = null;

        setTitle(percorso.getNome());

        timer = Timer.getInstance(getApplicationContext(), percorso.getTempo()*1000);

        //AUTOCALL IN x% TEMPO_PERCORSO
        //tempo percorso in secondi
        TIME_OUT_AUTOMATIC_ALERT = (Preferences.loadPercentageTimeoutTimer(getApplicationContext())*(percorso.getTempo()*1000))/100;

    }

    @Override
    protected void onResume() {
        super.onResume();

        //access location
        mManager = (LocationManager) getSystemService(LOCATION_SERVICE);

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

    /* metodo chiamato se alert=1 */
    private void alert(final LatLng loc){

        /* BOOLEAN PER ATOMICITA' DELLA FUNZIONE ALERT */
        if(alertIsActive){
            return;
        }
        alertIsActive = true;

        Player.getInstance(getApplicationContext()).startPlaying();

        dismissed = false;

        //creazione dialog chiamata
        AlertDialog.Builder builder = new AlertDialog.Builder(POIActivity.this, android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(getApplicationContext().getString(R.string.alert_title))
                .setMessage(getApplicationContext().getString(R.string.alert_call_educatore)
                )
                .setPositiveButton(R.string.button_si, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Player.getInstance(getApplicationContext()).stopPlaying();

                        //alert a educatore con send sms
                        Intent doAlert = new Intent(getApplicationContext(), Services.class);
                        doAlert.setAction(Services.ACTION_ALERT);
                        //https://www.google.com/maps/@42.0458585,13.9318123,15z
                        doAlert.putExtra("sms_body", "NAVIGAZIONE PER IMMAGINI: " + "https://www.google.com/maps/@" + loc.latitude + "," + loc.longitude + ",15z");
                        startService(doAlert);

                        dismissed = true;
                        dialog.dismiss();

                        alertIsActive = false;

                        notify_cancelled = true;

                        //cancel wake up
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                        autoCallHandler.removeCallbacks(autoCallRunnable);

                    }
                })
                .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        Player.getInstance(getApplicationContext()).stopPlaying();

                        //solo avviso con sms all'educatore che utente è fuori area sicura
                        Intent sms = new Intent(getApplicationContext(), Services.class);
                        sms.setAction(Services.ACTION_SEND_SMS);
                        sms.putExtra("sms_body", "NAVIGAZIONE PER IMMAGINI: " + "https://www.google.com/maps/@" + loc.latitude + "," + loc.longitude + ",15z");
                        startService(sms);

                        dismissed = true;
                        dialog.dismiss();

                        alertIsActive = false;

                        notify_cancelled = true;

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(timer != null){
            timer.stopTimer();
            timer = null;
        }
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

                    if(location_for_picture_and_alerthandler == null){
                        Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_take_a_picture_no_gps), Toast.LENGTH_LONG).show();
                        return;
                    }

                    //prepare service
                    Intent i = new Intent(getApplicationContext(), Services.class);
                    i.setAction(Services.ACTION_TAKE_A_PICTURE);
                    i.putExtra("image_path", ImageAbsolutePath);
                    i.putExtra("loc", location_for_picture_and_alerthandler);
                    //start service
                    startService(i);

                }
                break;
        }

    }

    public void takeAPicture(){

        if(location_for_picture_and_alerthandler == null){
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
