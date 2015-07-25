package com.hackbot.services;

import com.hackbot.businessLogic.Algo;
import com.hackbot.utility.Enums;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.format.Time;
import android.util.Log;

public class EventBroadcastDataReceiver extends BroadcastReceiver
{
	private Algo algo;
    private final static String LOG = "HackBot"+ EventBroadcastDataReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent)
	{
        Log.d(LOG, "in onReceive");

        if(isInitialStickyBroadcast()){
            Log.d(LOG, "in onReceive, this is a sticky broadcast, ignoring...");
        }
        else {
            Log.d(LOG, "in onReceive, not a sticky broadcast, processing...");
            algo = new Algo(context.getApplicationContext());
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.d(LOG, "Phone connectivity mode changed");
                boolean noConnectivity = intent.getBooleanExtra(
                        ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

                if (!noConnectivity) {
                    Log.d(LOG, "connected");
                    performOperation(Enums.EventIdConstant.DATA_ON, 1);
                } else {
                    Log.d(LOG, "disconnected");
                    performOperation(Enums.EventIdConstant.DATA_ON, 0);
                }
            }
        }
		
	}
	
	private void performOperation(int eventSettingsId, int value)
	{
        Log.d(LOG, "in performOperation");
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
		algo.writeToDB(eventSettingsId,  time.toMillis(false), value);	
		
	}

}
