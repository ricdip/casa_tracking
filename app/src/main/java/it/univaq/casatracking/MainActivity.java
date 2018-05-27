package it.univaq.casatracking;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.multidots.fingerprintauth.AuthErrorCodes;
import com.multidots.fingerprintauth.FingerPrintAuthCallback;
import com.multidots.fingerprintauth.FingerPrintAuthHelper;
import com.multidots.fingerprintauth.FingerPrintUtils;

import it.univaq.casatracking.utils.Preferences;


public class MainActivity extends AppCompatActivity implements FingerPrintAuthCallback {

    /* fingerprint authentication object */
    private FingerPrintAuthHelper mFingerPrintAuthHelper;
    private TextView messaggio;

    private Button riconoscimentoFaccialeButton;
    private Button loginButton;

    public static final int PERMISSION_MULTIPLE_REQUEST = 1;

    private boolean dialog_show = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(this, this);
        messaggio = findViewById(R.id.messaggio);

        riconoscimentoFaccialeButton = findViewById(R.id.riconoscimentoFaccialeButton);
        loginButton = findViewById(R.id.loginButton);

        //non ancora implementato
        riconoscimentoFaccialeButton.setVisibility(View.GONE);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //se il login è stato fatto in passato, login automatico al click (redirect a ChoiceActivity)
                //altrimenti apriamo LoginActivity

                if(Preferences.checkAutomaticLoginNotEnabled(getApplicationContext())){
                    //automatic login not enabled
                    Intent i = new Intent(view.getContext(), LoginActivity.class);
                    startActivity(i);

                } else {
                    //automatic login enabled
                    Intent i = new Intent(view.getContext(), ChoiceActivity.class);
                    startActivity(i);

                }

            }
        });

        // REQUEST PERMISSIONS

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS))
                != PackageManager.PERMISSION_GRANTED) {

            //REQUEST

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.SEND_SMS}, PERMISSION_MULTIPLE_REQUEST);
        }

        // END REQUEST PERMISSIONS

    }

    @Override
    protected void onResume() {
        super.onResume();

        /* print token di firebase (debug) */
        String token = FirebaseInstanceId.getInstance().getToken();
        System.out.println("FIREBASE TOKEN: " + token);
        /* /print token di firebase (debug) */

        boolean isFirstAccess = Preferences.checkFirstAccess(getApplicationContext());

        if (isFirstAccess) {

            if(dialog_show){
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);

            builder.setTitle(getApplicationContext().getString(R.string.alert_title))
                    .setMessage(getApplicationContext().getString(R.string.alert_no_user_registered))
                    .setPositiveButton(R.string.button_si, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            //preferenze
                            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                            startActivity(i);

                            dialog_show = false;

                            dialog.dismiss();

                        }
                    })
                    .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {

                            dialog_show = false;

                            dialog.dismiss();
                            finish();
                            System.exit(1);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .show();

            dialog_show = true;

        } else {
            messaggio.setText(getApplicationContext().getString(R.string.textview_scan_fingerprint));
            mFingerPrintAuthHelper.startAuth();
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
                    boolean read_phone_state = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean gps = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean coarse = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean network_state = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean send_sms = grantResults[5] == PackageManager.PERMISSION_GRANTED;

                    if (phone_call && read_phone_state && gps && coarse && network_state && send_sms) {
                        //all permissions granted
                        //no actions

                    } else {
                        //not all permissions granted
                        this.finish();
                        System.exit(1);

                    }

                } else {
                    //permissions not granted
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
        messaggio.setText(getApplicationContext().getString(R.string.textview_no_hardware_for_scan_fingerprint));

    }

    @Override
    public void onNoFingerPrintRegistered() {
        //There are no finger prints registered on this device.
        //alert dialog if is first time

        if(Preferences.checkNoFingerprintRegisteredFirstTime(getApplicationContext())){
            //first time no fingerprint registered
            //show popup

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);

            builder.setTitle(getApplicationContext().getString(R.string.alert_title))
                    .setMessage(getApplicationContext().getString(R.string.alert_no_fingerprints))
                    .setPositiveButton(R.string.button_si , new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            //redirect a pagina inserimento impronte
                            FingerPrintUtils.openSecuritySettings(MainActivity.this);

                            dialog.dismiss();

                        }
                    })
                    .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {

                            messaggio.setText(getApplicationContext().getString(R.string.textview_no_fingerprint_registered));
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .show();

            Preferences.cancelNoFingerprintRegisteredFirstTime(getApplicationContext());

        } else {
            //else no action, only show in TextView messaggio
            messaggio.setText(getApplicationContext().getString(R.string.textview_no_fingerprint_registered));

        }

    }

    @Override
    public void onBelowMarshmallow() {
        //Device running below API 23 version of android that does not support finger print authentication.
        //other auth method
        messaggio.setText(getApplicationContext().getString(R.string.textview_no_hardware_for_scan_fingerprint));

    }

    @Override
    public void onAuthSuccess(FingerprintManager.CryptoObject cryptoObject) {
        //Authentication successful.
        //handle auth

        //next page
        Intent i = new Intent(getApplicationContext(), ChoiceActivity.class);
        startActivity(i);

    }

    @Override
    public void onAuthFailed(int errorCode, String errorMessage) {
        switch (errorCode) {    //Parse the error code for recoverable/non recoverable error.
            case AuthErrorCodes.CANNOT_RECOGNIZE_ERROR:
                //Cannot recognize the fingerprint scanned.
                messaggio.setText(getApplicationContext().getString(R.string.textview_riprova));
                break;
            case AuthErrorCodes.NON_RECOVERABLE_ERROR:
                //This is not recoverable error. Try other options for user authentication. like pin, password.
                messaggio.setText(getApplicationContext().getString(R.string.textview_error_not_recoverable));
                break;
            case AuthErrorCodes.RECOVERABLE_ERROR:
                //Any recoverable error. Display message to the user.
                messaggio.setText(getApplicationContext().getString(R.string.textview_riprova));
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
