package com.hackbot.services;

import com.hackbot.businessLogic.Algo;
import com.hackbot.utility.Enums;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;

public class EventBroadcastBluetoothReceiver extends BroadcastReceiver
{

	private Algo algo;
	private final static String LOG = "HackBot"+ EventBroadcastBluetoothReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(LOG, "in onReceive");
		algo=new Algo(context.getApplicationContext());
		if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED))
        {
        	
        	
        	if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) 
        		    == BluetoothAdapter.STATE_OFF)
        	{
        		performOperation(Enums.EventIdConstant.BLUETOOTH_ON, 0);
        	}
        	else if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) 
        		    == BluetoothAdapter.STATE_ON)
        	{
        		performOperation(Enums.EventIdConstant.BLUETOOTH_ON, 1);
        	}
        		

        }
		
	}
	
	private void performOperation(int eventSettingsId, int value)
	{
		Log.d(LOG, "in performOperation");
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
		algo.writeToDB(eventSettingsId, time.toMillis(false), value);	
		
	}

}
