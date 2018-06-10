package it.univaq.casatracking.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import it.univaq.casatracking.model.Utente;

public class Preferences {

    public static void save(Context context, Utente utente){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();

        if(pref.getBoolean("first_access", true))
            editor.putBoolean("first_access", false);

        editor.putString("nome_utente", utente.getNome());
        editor.putString("numero_telefono", utente.getNumeroTelefono());
        editor.putString("numero_telefono_educatore", utente.getNumeroTelefonoEducatore());
        editor.putString("numero_telefono_emergenza", utente.getNumeroEmergenza());

        editor.apply();

    }

    public static void save(Context context, String key, String value){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();

        if(pref.getBoolean("first_access", true))
            editor.putBoolean("first_access", false);

        editor.putString(key, value);

        editor.apply();

    }

    public static int loadPercentageTimeoutTimer(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        String percentuale_scadenza_timeout = pref.getString("percentuale_scadenza_timeout", "0");

        return Integer.parseInt(percentuale_scadenza_timeout);
    }

    public static void savePercentageTimeoutTimer(Context context, int percentuale_scadenza_timeout){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("percentuale_scadenza_timeout", String.valueOf(percentuale_scadenza_timeout));

        editor.apply();
    }

    public static Utente loadUtente(Context context){

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        String nome_utente = pref.getString("nome_utente", "TEST");
        String numero_telefono = pref.getString("numero_telefono", "TEST");
        String numero_telefono_educatore = pref.getString("numero_telefono_educatore", "TEST");
        String numero_telefono_emergenza = pref.getString("numero_telefono_emergenza", "TEST");

        Utente utente = new Utente(nome_utente, numero_telefono, numero_telefono_educatore, numero_telefono_emergenza);

        return utente;

    }

    public static String load(Context context, String key){

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        String value = pref.getString(key, "TEST");

        return value;

    }

    public static boolean checkFirstAccess(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean isFirst = pref.getBoolean("first_access", true);

        return isFirst;
    }

    public static void cancelFirstAccess(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean("first_access", false);

        editor.apply();
    }

    public static boolean checkNoFingerprintRegisteredFirstTime(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean noFingerprintRegisteredFirstTime = pref.getBoolean("no_fingerprint_registered_first_time", true);

        return noFingerprintRegisteredFirstTime;
    }

    public static void cancelNoFingerprintRegisteredFirstTime(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean("no_fingerprint_registered_first_time", false);

        editor.apply();
    }

    public static boolean checkAutomaticLoginNotEnabled(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean noFingerprintRegisteredFirstTime = pref.getBoolean("automatic_login_not_enabled", true);

        return noFingerprintRegisteredFirstTime;
    }

    public static void cancelAutomaticLoginNotEnabled(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean("automatic_login_not_enabled", false);

        editor.apply();
    }

    public static void setAutomaticLoginNotEnabled(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean("automatic_login_not_enabled", true);

        editor.apply();
    }

    public static boolean checkAutomaticSMS(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean automaticSMS = pref.getBoolean("invio_automatico_sms_onevent_area_sicura", false);

        return automaticSMS;
    }

}
