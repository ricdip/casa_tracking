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

    // There are two types of messages data messages and notification messages. Data messages are handled
    // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
    // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
    // is in the foreground. When the app is in the background an automatically generated notification is displayed.
    // When the user taps on the notification they are returned to the app. Messages containing both notification
    // and data payloads are treated as notification messages. The Firebase console always sends notification
    // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
    /*
     * Notification message
     * Use scenario: FCM automatically displays the message to end-user devices on behalf of the client app. Notification messages have a predefined set of user-visible keys.
     * How to send:
     * Use your app server and FCM server API: Set the notification key. May have optional data payload. Always collapsible.
     * Use the Notifications console: Enter the Message Text, Title, etc., and send. Add optional data payload by providing Custom data in the Notifications console. Always collapsible.
     *
     * Data message
     * Use scenario: Client app is responsible for processing data messages. Data messages have only custom key-value pairs.
     * How to send:
     * Use your app server and FCM server API: Set the data key only. Can be either collapsible or non-collapsible.
     */

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // START NavigazioneLiberaActivity ACTIVITY sia se utente è in foreground sia se è in background, possibile
        //se e solo se viene inviato dal server un messaggio data, NON notification
        super.onMessageReceived(remoteMessage);


        //Se l'app ha un utente registrato, allora alla ricezione del messaggio viene chiamato
        //il metodo startNavigazioneLiberaActivity che avvia l'activity NavigazioneLiberaActivity
        if(!Preferences.checkFirstAccess(getApplicationContext()))
            startNavigazioneLiberaActivity();
    }

    private void startNavigazioneLiberaActivity() {

        Intent i = new Intent(getApplicationContext(), NavigazioneLiberaActivity.class);
        startActivity(i);

    }
}
