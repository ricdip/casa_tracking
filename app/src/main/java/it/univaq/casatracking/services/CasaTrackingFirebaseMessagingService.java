package it.univaq.casatracking.services;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import it.univaq.casatracking.NavigazioneLiberaActivity;
import it.univaq.casatracking.utils.Preferences;

public class CasaTrackingFirebaseMessagingService extends FirebaseMessagingService {

    public  CasaTrackingFirebaseMessagingService(){
        super();
    }

    /* TODO : NOTIFICA BACKGROUND NON FUNZIONA */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        /* START NAVIGAZIONE LIBERA ACTIVITY */
        super.onMessageReceived(remoteMessage);
        /*
        String title = remoteMessage.getNotification().getTitle();
        String message = remoteMessage.getNotification().getBody();
        */

        System.out.println("NOTIFICA! " + Preferences.checkFirstAccess(getApplicationContext()));

        if(!Preferences.checkFirstAccess(getApplicationContext()))
            startNavigazioneLiberaActivity();
    }

    private void startNavigazioneLiberaActivity() {

        Intent i = new Intent(getApplicationContext(), NavigazioneLiberaActivity.class);
        startActivity(i);

    }
}
