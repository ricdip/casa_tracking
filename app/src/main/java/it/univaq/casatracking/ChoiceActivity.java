package it.univaq.casatracking;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import it.univaq.casatracking.services.Services;

public class ChoiceActivity extends AppCompatActivity {

    private Button navigazioneLibera;
    private Button scegliPercorso;
    private Button callButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        navigazioneLibera = findViewById(R.id.navigazioneLiberaButton);
        scegliPercorso = findViewById(R.id.scegliPercorsoButton);
        callButton = findViewById(R.id.choice_callButton);

        //non ancora implementato
        scegliPercorso.setVisibility(View.GONE);

        navigazioneLibera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), NavigazioneLiberaActivity.class);
                startActivity(i);
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
