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


	@Override
	public void onReceive(Context context, Intent intent)
	{
		algo=new Algo(context.getApplicationContext());
		if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
        {
            Log.d("app", "Phone connectivity mode changed");
            boolean noConnectivity = intent.getBooleanExtra(
                    ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            if (!noConnectivity) {
                Log.d("NetworkCheckReceiver", "connected");
                performOperation(Enums.EventIdConstant.DATA_ON, 1);
            }
            else
            {
                Log.d("NetworkCheckReceiver", "disconnected");
                performOperation(Enums.EventIdConstant.DATA_ON, 0);
            }
        }
		
	}
	
	private void performOperation(int eventSettingsId, int value)
	{
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
		algo.writeToDB(eventSettingsId,  time.toMillis(false), value);	
		
	}

}
