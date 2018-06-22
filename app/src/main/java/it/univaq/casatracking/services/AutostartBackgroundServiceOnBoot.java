package it.univaq.casatracking.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutostartBackgroundServiceOnBoot extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            context.startService(new Intent(context, BackgroundService.class));
        }
    }
}
