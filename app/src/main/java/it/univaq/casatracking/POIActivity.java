package it.univaq.casatracking;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.List;

import it.univaq.casatracking.model.POI;
import it.univaq.casatracking.model.Percorso;
import it.univaq.casatracking.model.Utente;
import it.univaq.casatracking.utils.Preferences;

public class POIActivity extends AppCompatActivity {

    //TODO : CLASSE POIActivity INCOMPLETA

    private Utente utente;
    private Percorso percorso;
    private List<POI> pois;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_choice);

        utente = Preferences.loadUtente(getApplicationContext());

        String percorso_json = (String) getIntent().getStringExtra("percorso");
        Gson gson = new Gson();
        percorso = gson.fromJson(percorso_json, Percorso.class);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
