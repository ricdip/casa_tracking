package it.univaq.casatracking.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;

import it.univaq.casatracking.R;

public class Player {

    private static Context context;
    private static Player player;

    private MediaPlayer mp;
    private Vibrator v;
    private long[] mVibratePattern;

    public static Player getInstance(Context ctx){
        context = ctx;
        if(player == null)
            player = new Player();

        return player;
    }

    public void startPlaying(){

        //creazione ring per alert
        //Uri alert_ring = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mp = MediaPlayer.create(context, R.raw.alert);

        //creazione vibrazione per alert
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        //vibrate pattern
        mVibratePattern = new long[]{0, 400, 200, 400};

        //start alert sonoro
        mp.start();
        mp.setLooping(true);

        //start vibrazione
        // -1 : Do not repeat this pattern, 0 if you want to repeat this pattern from 0th index
        v.vibrate(mVibratePattern, 0);

    }

    public void stopPlaying(){

        if(mp != null){
            if(mp.isPlaying())
                mp.pause();

            mp.stop();
            mp.release();
            mp = null;

            v.cancel();
            v = null;
        }
    }
}
