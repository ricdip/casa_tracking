package it.univaq.casatracking;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import it.univaq.casatracking.utils.Preferences;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public static class SettingsFragment extends PreferenceFragment {

        private EditTextPreference nome_utente;
        private EditTextPreference numero_telefono;
        private EditTextPreference numero_telefono_educatore;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            nome_utente = (EditTextPreference) findPreference("nome_utente_preference");
            numero_telefono = (EditTextPreference) findPreference("numero_telefono_preference");
            numero_telefono_educatore = (EditTextPreference) findPreference("numero_telefono_educatore_preference");


            // listeners
            nome_utente.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String nome_utente = (String) newValue;

                    Preferences.save(getContext(), "nome_utente", nome_utente);

                    preference.setSummary(nome_utente);
                    return true;
                }
            });

            numero_telefono.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String numero_telefono = (String) newValue;

                    Preferences.save(getContext(), "numero_telefono", numero_telefono);

                    preference.setSummary(numero_telefono);
                    return true;
                }
            });

            numero_telefono_educatore.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String numero_telefono_educatore = (String) newValue;

                    Preferences.save(getContext(), "numero_telefono_educatore", numero_telefono_educatore);

                    preference.setSummary(numero_telefono_educatore);
                    return true;
                }
            });

            // summary
            nome_utente.setSummary(Preferences.load(getContext(), "nome_utente"));
            numero_telefono.setSummary(Preferences.load(getContext(),"numero_telefono"));
            numero_telefono_educatore.setSummary(Preferences.load(getContext(),"numero_telefono_educatore"));

        }


    }

}
