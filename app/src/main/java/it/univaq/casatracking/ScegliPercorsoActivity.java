package it.univaq.casatracking;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.ExecutionException;

import it.univaq.casatracking.model.Utente;
import it.univaq.casatracking.services.Services;
import it.univaq.casatracking.utils.Preferences;
import it.univaq.casatracking.utils.Request;

public class ScegliPercorsoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressDialog progress;
    private Utente utente;

    private Button callButton;

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

    }

    @Override
    protected void onResume() {
        super.onResume();

        try {

            //download data
            if(!Request.isConnected(getApplicationContext())){
                //snackbar creation
                Snackbar snackbar = Snackbar.make(findViewById(R.id.sceglipercorso_constraint), getApplicationContext().getString(R.string.snackbar_no_internet), Snackbar.LENGTH_LONG);
                snackbar.show();

                return;
            }

            Request request = new Request();
            request.execute(getApplicationContext(), "get_percorsi", utente, null);

            showProgress();

            String result = (String) request.get();
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

            recyclerView.setAdapter(percorsi_adapter);

            //snackbar creation
            Snackbar snackbar = Snackbar.make(findViewById(R.id.sceglipercorso_constraint), getApplicationContext().getString(R.string.snackbar_download_completato), Snackbar.LENGTH_LONG);
            snackbar.show();

        } catch(InterruptedException|ExecutionException|JSONException e){
            e.printStackTrace();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

}