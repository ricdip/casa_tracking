package it.univaq.casatracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import it.univaq.casatracking.model.Utente;
import it.univaq.casatracking.services.RequestService;
import it.univaq.casatracking.services.Services;
import it.univaq.casatracking.utils.Preferences;
import it.univaq.casatracking.utils.Request;

public class ScegliPercorsoActivity extends AppCompatActivity {

    //TODO : REMOVE PRINTF FOR DEBUG

    private RecyclerView recyclerView;
    private ProgressDialog progress;
    private Utente utente;

    private Button callButton;

    public static final String ACTION_SERVICE_COMPLETED = "action_service_completed";
    private boolean download_completed = false;

    /* LOCATION LISTENER */
    private LatLng location_variable;
    private LocationManager mManager;
    private LocationListener listener = new LocationListener() {

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

            location_variable = new LatLng(lat, lng);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_no_gps), Toast.LENGTH_SHORT).show();
        }
    };
    /* END LOCATION LISTENER */

    /* handler per redirect */
    private static final int TIMEOUT = 60*1000; //60 seconds
    private Handler redirectHandler = new Handler();
    private Runnable redirectRunnable = new Runnable() {
        @Override
        public void run() {
            redirectHandler.removeCallbacks(redirectRunnable);
            //redirect to NavigazioneLibera
            finish();
            Intent i = new Intent(getApplicationContext(), NavigazioneLiberaActivity.class);
            startActivity(i);
        }
    };
    /* END handler per alert */

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent == null || intent.getAction() == null) return;

            switch (intent.getAction()){
                case ACTION_SERVICE_COMPLETED:

                    //download completed, dismissing progress dialog
                    String result = intent.getStringExtra("data");
                    try {

                        if(result == null){
                            dismissProgress();
                            //snackbar creation
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.sceglipercorso_constraint), getApplicationContext().getString(R.string.snackbar_download_percorsi_error), Snackbar.LENGTH_LONG);
                            snackbar.show();

                            return;
                        }

                        JSONArray array = new JSONArray(result);

                        if(array.length() == 0){
                            dismissProgress();
                            //snackbar creation
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.sceglipercorso_constraint), getApplicationContext().getString(R.string.snackbar_no_data), Snackbar.LENGTH_LONG);
                            snackbar.show();

                            return;
                        }

                        PercorsiAdapter percorsi_adapter = new PercorsiAdapter(array);

                        dismissProgress();
                        // /progress

                        recyclerView.setAdapter(percorsi_adapter);

                        //snackbar creation
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.sceglipercorso_constraint), getApplicationContext().getString(R.string.snackbar_download_completato), Snackbar.LENGTH_LONG);
                        snackbar.show();

                        download_completed = true;

                        resetRedirectTimer();

                    } catch(JSONException e){
                        e.printStackTrace();
                    }

                    break;

            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scegli_percorso);

        recyclerView = findViewById(R.id.main_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(new PercorsiAdapter(new JSONArray()));

        utente = Preferences.loadUtente(getApplicationContext());

        callButton = findViewById(R.id.sceglipercorso_callButton);

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

    }

    @Override
    protected void onResume() {
        super.onResume();

        /* LOCATION HANDLE */
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
        /* END LOCATION HANDLE */

        if(download_completed)
            return;

        //download data
        if(!Request.isConnected(getApplicationContext())){
            //snackbar creation
            Snackbar snackbar = Snackbar.make(findViewById(R.id.sceglipercorso_constraint), getApplicationContext().getString(R.string.snackbar_no_internet), Snackbar.LENGTH_LONG);
            snackbar.show();

            return;
        }

        //progress
        showProgress();
        stopRedirectTimer();

        //preparing to download
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SERVICE_COMPLETED);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);

        System.out.println("filter prepared");

        //start download service
        Intent intent = new Intent(getApplicationContext(), RequestService.class);
        intent.setAction(RequestService.ACTION_GET_PERCORSI);
        startService(intent);

        System.out.println("start service");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
        stopRedirectTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mManager != null){
            mManager.removeUpdates(listener);
            mManager = null;
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        resetRedirectTimer();
    }

    private void showProgress(){
        //progress dialog
        progress = new ProgressDialog(this);
        //progress setting
        progress.setMessage("Download percorsi...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false);
        progress.setIndeterminate(true);
        progress.show();
    }

    private void dismissProgress(){
        //download completed, dismissing progress dialog
        if(progress != null)
            if(progress.isShowing())
                progress.dismiss();
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

    public void resetRedirectTimer(){
        redirectHandler.removeCallbacks(redirectRunnable);
        redirectHandler.postDelayed(redirectRunnable, TIMEOUT);
    }

    public void stopRedirectTimer(){
        redirectHandler.removeCallbacks(redirectRunnable);
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

}
