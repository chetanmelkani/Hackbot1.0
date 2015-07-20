package com.hackbot.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.hackbot.businessLogic.Algo;
import com.hackbot.entity.Events;
import com.hackbot.utility.Enums;

import java.util.List;

public class EventBroadcastAudioReceiver extends BroadcastReceiver {


    private List<Events> eventSettingsList;
    private Algo algo;
    private final static String LOG = "HackBot"+ EventBroadcastAudioReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Log.d(LOG, "in onReceive");

        algo = new Algo(ctx.getApplicationContext());
        Toast.makeText(ctx, "Started", Toast.LENGTH_SHORT).show();

        if (intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
            AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);


            switch (am.getRingerMode()) {
                case AudioManager.RINGER_MODE_SILENT:
                    Log.d(LOG, "Silent mode");
                    performOperation(Enums.EventIdConstant.AUDIO_ON, 0);
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    Log.d(LOG, "Vibrate mode");
                    break;
                case AudioManager.RINGER_MODE_NORMAL:
                    Log.d(LOG, "Normal mode");
                    performOperation(Enums.EventIdConstant.AUDIO_ON, 1);
                    break;
            }
            Log.d(LOG, "Phone audio mode changed");
        }
        
        	
 
		/*NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx)

				.setSmallIcon(R.drawable.ic_launcher)
				.setContentText(
						message1 +" and "+ message)
				.setContentTitle("Hackbot");
		NotificationManager manager = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(0, builder.build());*/


    }

    private void updateEventSettings(List<Events> newEventList) {
        //1. Find out if there is a change in setting it needs to be unlearned
        Log.d(LOG, "in updateEventSettings");
        if (eventSettingsList == null && eventSettingsList.size() == 0) {
            this.eventSettingsList = newEventList;
        } else {
            for (int i = 0; i < eventSettingsList.size(); i++) {
                if (eventSettingsList.get(i).getIsEnabled() != newEventList.get(i).getIsEnabled()) {
                    //remove the entry from db and publish
                }
            }
        }
        //2. update the list with new list
    }


    /**
     * Action is performed
     */
    private void performOperation(int eventSettingsId, int value) {
        Log.d(LOG, "in performOperation");
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
        algo.writeToDB(eventSettingsId, time.toMillis(false), value);

    }

}
