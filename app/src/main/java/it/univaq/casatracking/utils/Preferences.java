package it.univaq.casatracking.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import it.univaq.casatracking.model.Utente;

public class Preferences {

    public static void save(Context context, Utente utente){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean("first_access", false);

        editor.putString("nome_utente", utente.getNome());
        editor.putString("numero_telefono", utente.getNumeroTelefono());
        editor.putString("numero_telefono_educatore", utente.getNumeroTelefonoEducatore());

        editor.apply();

    }

    public static Utente load(Context context){

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        String nome_utente = pref.getString("nome_utente", "TEST");
        String numero_telefono = pref.getString("numero_telefono", "TEST");
        String numero_telefono_educatore = pref.getString("numero_telefono_educatore", "TEST");

        Utente utente = new Utente(nome_utente, numero_telefono, numero_telefono_educatore);

        return utente;

    }

    public static boolean checkFirstAccess(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean isFirst = pref.getBoolean("first_access", true);

        return isFirst;
    }

}
