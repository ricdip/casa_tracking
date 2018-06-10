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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import it.univaq.casatracking.services.Services;

public class ChoiceActivity extends AppCompatActivity {

    private Button navigazioneLibera;
    private Button scegliPercorso;
    private Button callButton;

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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        navigazioneLibera = findViewById(R.id.navigazioneLiberaButton);
        scegliPercorso = findViewById(R.id.scegliPercorsoButton);
        callButton = findViewById(R.id.choice_callButton);

        navigazioneLibera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), NavigazioneLiberaActivity.class);
                startActivity(i);
            }
        });

        scegliPercorso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), ScegliPercorsoActivity.class);
                startActivity(i);
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
