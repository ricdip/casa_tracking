package it.univaq.casatracking.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.univaq.casatracking.services.BackgroundService;

public class BackgroundServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent == null || intent.getAction() == null) return;

        //start Service BackgroundService at boot
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            context.startService(new Intent(context, BackgroundService.class));
        }
    }
}
