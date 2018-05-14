package it.univaq.casatracking;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.Gson;
import com.multidots.fingerprintauth.AuthErrorCodes;
import com.multidots.fingerprintauth.FingerPrintAuthCallback;
import com.multidots.fingerprintauth.FingerPrintAuthHelper;

import it.univaq.casatracking.model.Utente;
import it.univaq.casatracking.utils.Preferences;

// TODO : NO INTERNET CONNECTION HANDLER

public class MainActivity extends AppCompatActivity implements FingerPrintAuthCallback {

    /* fingerprint authentication object */
    private FingerPrintAuthHelper mFingerPrintAuthHelper;
    private TextView messaggio;

    public static final int PERMISSION_MULTIPLE_REQUEST = 1;

    private static boolean dialog_show = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(this, this);
        messaggio = findViewById(R.id.messaggio);

        // REQUEST PERMISSIONS

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS))
                != PackageManager.PERMISSION_GRANTED) {

            //REQUEST

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.SEND_SMS}, PERMISSION_MULTIPLE_REQUEST);
        }

        // END REQUEST PERMISSIONS

    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isFirstAccess = Preferences.checkFirstAccess(getApplicationContext());

        if (isFirstAccess) {

            if(dialog_show){
                return;
            }

            //login default user

            final AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(MainActivity.this);
            }

            builder.setTitle("ATTENZIONE")
                    .setMessage("Nessun utente registrato\nContinuare ugualmente ?")
                    .setNegativeButton(R.string.button_procedi, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            //Login Default user
                            Utente utente = new Utente();
                            utente.createTest();

                            //next page
                            Intent i = new Intent(getApplicationContext(), NavigazioneLiberaActivity.class);
                            Gson gson = new Gson();
                            String utente_json = gson.toJson(utente);
                            i.putExtra("utente", utente_json);

                            startActivity(i);

                            dialog_show = false;

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .show();

            dialog_show = true;

        } else {
            mFingerPrintAuthHelper.startAuth();
            messaggio.setText("METTI IL DITO SUL LETTORE IMPRONTE");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        mFingerPrintAuthHelper.stopAuth();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case PERMISSION_MULTIPLE_REQUEST:

                if (grantResults.length > 0) {
                    boolean phone_call = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean internet = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean gps = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean send_sms = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if (phone_call && internet && gps && send_sms) {
                        //all permissions granted


                    } else {
                        //not all permissions granted
                        // TODO : ALERT DIALOG CON SPIEGAZIONE PERICOLO

                        if ((!phone_call) && (!send_sms)) {
                            this.finish();
                            System.exit(1);
                        }


                    }

                } else {
                    //permissions not granted
                    // TODO : ALERT DIALOG CON SPIEGAZIONE
                    this.finish();
                    System.exit(1);
                }

                break;

        }

    }

    /* FINGERPRINT METHODS HANDLER */

    @Override
    public void onNoFingerPrintHardwareFound() {
        //Device does not have finger print scanner.

        //other auth method

    }

    @Override
    public void onNoFingerPrintRegistered() {
        //There are no finger prints registered on this device.
        //alert dialog e login default user

        messaggio.setText("");

        boolean isFirstAccess = Preferences.checkFirstAccess(getApplicationContext());

        if(!isFirstAccess){
            /* TODO : HANDLE OTHER AUTH METHOD */
        }

        final AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(MainActivity.this);
        }

        builder.setTitle("ATTENZIONE")
                .setMessage("Nessuna impronta registrata\nContinuare ugualmente ?")
                .setNegativeButton(R.string.button_procedi , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        //Login Default user
                        Utente utente = new Utente();
                        utente.createTest();

                        //next page
                        Intent i = new Intent(getApplicationContext(), NavigazioneLiberaActivity.class);
                        Gson gson = new Gson();
                        String utente_json = gson.toJson(utente);
                        i.putExtra("utente", utente_json);
                        startActivity(i);

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .show();

    }

    @Override
    public void onBelowMarshmallow() {
        //Device running below API 23 version of android that does not support finger print authentication.

        //other auth method

    }

    @Override
    public void onAuthSuccess(FingerprintManager.CryptoObject cryptoObject) {
        //Authentication successful.
        //handle auth

        Utente utente = Preferences.loadUtente(getApplicationContext());

        //next page
        Intent i = new Intent(getApplicationContext(), NavigazioneLiberaActivity.class);
        Gson gson = new Gson();
        String utente_json = gson.toJson(utente);
        i.putExtra("utente", utente_json);
        startActivity(i);

    }

    @Override
    public void onAuthFailed(int errorCode, String errorMessage) {
        switch (errorCode) {    //Parse the error code for recoverable/non recoverable error.
            case AuthErrorCodes.CANNOT_RECOGNIZE_ERROR:
                //Cannot recognize the fingerprint scanned.
                messaggio.setText("RIPROVA");
                break;
            case AuthErrorCodes.NON_RECOVERABLE_ERROR:
                //This is not recoverable error. Try other options for user authentication. like pin, password.
                messaggio.setText("DAI IL TELEFONO ALL'EDUCATORE");
                break;
            case AuthErrorCodes.RECOVERABLE_ERROR:
                //Any recoverable error. Display message to the user.
                messaggio.setText("RIPROVA");
                break;
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
            case R.id.preferenze:
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
