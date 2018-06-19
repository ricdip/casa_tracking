package it.univaq.casatracking.utils;

import android.content.Context;
import android.os.CountDownTimer;

import it.univaq.casatracking.POIActivity;
import it.univaq.casatracking.R;

public class Timer extends CountDownTimer {

    private static Timer instance;
    private static Context context;
    private static long TICK = 1*1000; //1second
    private static long timeout;

    private boolean isStart;
    private boolean isAlarm;

    public static Timer getInstance(Context ctx, long time){
        context = ctx;
        if(timeout != time || instance == null){
            timeout = time;
            instance = new Timer(time);
        }

        return instance;
    }

    public static Timer getInstance(){
        return instance;
    }

    private Timer(long time){
        super(time, TICK);
    }

    @Override
    public void onTick(long millisRemaining) {

        String time_remaining = "RIMANENTE: " + (millisRemaining/1000) + " sec";
        POIActivity.navigazione_immagini_timeout.setText(time_remaining);

    }

    @Override
    public void onFinish() {
        isAlarm = true;
        //start alarm
        POIActivity.navigazione_immagini_timeout.setText(context.getString(R.string.timesup));
    }

    public void startTimer(){
        if(!isStart)
            start();
        isStart = true;
    }

    public void stopTimer(){
        if(isStart)
            cancel();
        isStart = false;
        isAlarm = false;
    }

    public void restartTimer(){
        stopTimer();
        startTimer();
    }

    public boolean isAlarm(){
        return isAlarm;
    }
}
