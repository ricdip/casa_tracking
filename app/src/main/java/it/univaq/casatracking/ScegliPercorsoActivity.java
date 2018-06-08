package it.univaq.casatracking;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;

import it.univaq.casatracking.model.Utente;
import it.univaq.casatracking.services.Services;
import it.univaq.casatracking.utils.Preferences;
import it.univaq.casatracking.utils.Request;

public class ScegliPercorsoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressDialog progress;
    private Utente utente;

    private Button callButton;

    public static final String ACTION_SERVICE_COMPLETED = "action_service_completed";
    private boolean download_completed = false;

    /* handler per redirect */
    private static final int TIMEOUT = 10*1000; //60 seconds
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
                i.setAction(Services.ACTION_CALL_EDUCATORE);
                startService(i);
            }
        });

        // redirect in TIMEOUT ms
        redirectHandler.postDelayed(redirectRunnable, TIMEOUT);

    }

    @Override
    protected void onResume() {
        super.onResume();

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

        //preparing to download
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SERVICE_COMPLETED);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);

        //start download service
        Intent intent = new Intent(getApplicationContext(), Services.class);
        intent.setAction(Services.ACTION_DOWNLOAD_PERCORSI);
        startService(intent);

        resetRedirectTimer();
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

}
