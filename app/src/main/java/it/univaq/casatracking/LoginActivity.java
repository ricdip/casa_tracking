package it.univaq.casatracking;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import it.univaq.casatracking.model.Utente;
import it.univaq.casatracking.utils.Preferences;

public class LoginActivity extends AppCompatActivity {

    private Utente utente;
    private EditText nome;
    private EditText numero_telefono;
    private Button LOGIN_loginButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        nome = findViewById(R.id.editTextNome);
        numero_telefono = findViewById(R.id.editTextTelefono);
        LOGIN_loginButton = findViewById(R.id.LOGIN_loginButton);

        utente = Preferences.loadUtente(getApplicationContext());

        LOGIN_loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(nome.getText().toString().equals("") && numero_telefono.getText().toString().equals("")){
                    //snackbar creation
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.login_constraint), getApplicationContext().getString(R.string.snackbar_campi_vuoti), Snackbar.LENGTH_LONG);
                    snackbar.show();

                    return;
                }

                boolean AcceptLogin = checkLogin(nome.getText().toString(), numero_telefono.getText().toString());

                if(AcceptLogin){
                    //settiamo flag automatic_login su enabled
                    Preferences.cancelAutomaticLoginNotEnabled(getApplicationContext());

                    Intent i = new Intent(v.getContext(), ChoiceActivity.class);
                    startActivity(i);
                } else {
                    //snackbar creation
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.login_constraint), getApplicationContext().getString(R.string.snackbar_dati_errati), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }

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

    private boolean checkLogin(String nome, String numero_telefono){

        if(utente.getNome().equals(nome) && utente.getNumeroTelefono().equals(numero_telefono))
            return true;
        else
            return false;

    }

}
