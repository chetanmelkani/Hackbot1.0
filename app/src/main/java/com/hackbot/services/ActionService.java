package com.hackbot.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.hackbot.dao.DBHelper;
import com.hackbot.entity.EventRunningAfterLearned;
import com.hackbot.entity.HackBotEvent;
import com.hackbot.utility.Enums.EventIdConstant;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ActionService extends Service {

    //TODO should these be created here ?
    private static List<HackBotEvent> eventsListened = new ArrayList<>();
    private static List<EventRunningAfterLearned> eventsRunning = new ArrayList<>();

    private final IBinder mBinder = new LocalBinder();

    private DBHelper dbHelper;

    private final static String LOG = "HackBot" + ActionService.class.getSimpleName();

	/*class TrackedEvent {
        int id;
		int eventId;
		int value;
		long timeToStop;
	}*/

    public class LocalBinder extends Binder {
        public ActionService getService() {
            // Return this instance of LocalService so clients can call public methods
            Log.d(LOG, "in getService, Return this instance of LocalService so clients can call public methods");
            return ActionService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d(LOG, "in onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG, "in onStartCommand");

        //	Toast.makeText(this, "Action Service Started", Toast.LENGTH_LONG).show();
        dbHelper = DBHelper.getInstance(this);

        //fillDummyData();
        Timer timer = new Timer();
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(LOG, "This is TimerTask it runs every minute");
                Log.d(LOG, "The size of eventsListened is " + eventsListened.size());
                Log.d(LOG, "The size of eventsRunning is " + eventsRunning.size());

                for (HackBotEvent hbe : eventsListened) {
                    if (hbe.toTriggerOrNot(Calendar.getInstance().getTimeInMillis())) {
                        switch (hbe.getEventId()) {
                            case EventIdConstant.AUDIO_OFF:
                            case EventIdConstant.AUDIO_ON:
                                Log.d(LOG, "eventsListened audioChange");
                                audioChange(hbe.getValue());

                                EventRunningAfterLearned eral = new EventRunningAfterLearned();
                                eral.setHbeId(hbe.getId());
                                eral.setEventId(hbe.getEventId());
                                eral.setTimeToStop(Calendar.getInstance().getTimeInMillis() + hbe.getDuration());
                                eral.setValue(hbe.getValue());
                                //add this event to the arraylist eventsRunning
                                eventsRunning.add(eral);

                                //	dbHelper.insertEventsRunning(hbe.getId(),Calendar.getInstance().getTimeInMillis() + hbe.getDuration());
                                break;

                            case EventIdConstant.BLUETOOTH_OFF:
                            case EventIdConstant.BLUETOOTH_ON:
                                Log.d(LOG, "eventsListened bluetoothChange");
                                bluetoothChange(hbe.getValue());

                                EventRunningAfterLearned eral1 = new EventRunningAfterLearned();
                                eral1.setHbeId(hbe.getId());
                                eral1.setEventId(hbe.getEventId());
                                eral1.setTimeToStop(Calendar.getInstance().getTimeInMillis() + hbe.getDuration());
                                eral1.setValue(hbe.getValue());
                                //add this event to the arraylist eventsRunning
                                eventsRunning.add(eral1);

                                //	dbHelper.insertEventsRunning(hbe.getId(),Calendar.getInstance().getTimeInMillis() + hbe.getDuration());
                                break;

                            case EventIdConstant.DATA_OFF:
                            case EventIdConstant.DATA_ON:
                                Log.d(LOG, "eventsListened dataChange");
                                dataChange(hbe.getValue());

                                EventRunningAfterLearned eral2 = new EventRunningAfterLearned();
                                eral2.setHbeId(hbe.getId());
                                eral2.setEventId(hbe.getEventId());
                                eral2.setTimeToStop(Calendar.getInstance().getTimeInMillis() + hbe.getDuration());
                                eral2.setValue(hbe.getValue());
                                //add this event to the arraylist eventsRunning
                                eventsRunning.add(eral2);

                                //	dbHelper.insertEventsRunning(hbe.getId(),Calendar.getInstance().getTimeInMillis() + hbe.getDuration());
                                break;

                        }
                    }
                }

                for (Iterator<EventRunningAfterLearned> iterator = eventsRunning.iterator(); iterator.hasNext(); ) {
                    EventRunningAfterLearned eral = iterator.next();
                    Log.d(LOG, "size of eventsRunning list is : " + eventsRunning.size());
                    if (eral.getTimeToStop() <= System.currentTimeMillis()) {
                        Log.d(LOG, "found an event to delete from the eventsRunning array");
                        switch (eral.getEventId()) {
                            case EventIdConstant.AUDIO_OFF:
                            case EventIdConstant.AUDIO_ON:
                                audioChange(switchInput(eral.getValue()));
                                break;

                            case EventIdConstant.BLUETOOTH_OFF:
                            case EventIdConstant.BLUETOOTH_ON:
                                bluetoothChange(switchInput(eral.getValue()));
                                break;

                            case EventIdConstant.DATA_OFF:
                            case EventIdConstant.DATA_ON:
                                dataChange(switchInput(eral.getValue()));
                                break;
                        }
                        Log.d(LOG, "remove this event after the state is switched");
                        iterator.remove();

                    }
                }

            }
        };

        // schedule the task to run starting now and then every minute...
        timer.schedule(hourlyTask, 0, 1000 * 60);
        return START_STICKY;
    }

    private int switchInput(int value){
        return value == 1 ? 0 : 1;
    }

    private void audioChange(int valueSet) {
        Log.d(LOG, "in audioChange");
        AudioManager am;
        am = (AudioManager) getBaseContext().getSystemService(
                Context.AUDIO_SERVICE);
        if (valueSet == 1)
            am.setRingerMode(2);
        else
            am.setRingerMode(0);
    }

    private void bluetoothChange(int valueSet) {
        Log.d(LOG, "in bluetoothChange");
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        if (valueSet == 1) {
            Log.d(LOG, "bluetooth.enable()");
            bluetooth.enable();
        } else {
            bluetooth.disable();
            Log.d(LOG, "bluetooth.disable()");
        }
    }

    private void dataChange(int valueSet) {
        Log.d(LOG, "in dataChange");
        ConnectivityManager dataManager;
        boolean value = false;
        if (valueSet == 1)
            value = true;
        dataManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Method dataMtd;
        try {
            dataMtd = ConnectivityManager.class.getDeclaredMethod(
                    "setMobileDataEnabled", boolean.class);
            dataMtd.setAccessible(value);
            try {
                dataMtd.invoke(dataManager, value);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }

    //TODO understand this method also
    public void fillListenedEventList(HackBotEvent updatedEvent) {
        Log.d(LOG, "in fillListenedEventList hbe id : " + updatedEvent.getId());
        boolean isNew = true;

        List<Integer> deletedIds = new ArrayList<Integer>();
        Log.d(LOG, "in fillListenedEventList The size of eventsListened is " + eventsListened.size());

        if (updatedEvent != null && updatedEvent.getId() != 0) {
            for (Iterator iterator = eventsListened.iterator(); iterator
                    .hasNext(); ) {
                HackBotEvent event = (HackBotEvent) iterator.next();
                if (updatedEvent.getId() == event.getId()) {
                    if (updatedEvent.getIsLearned() == 0) {
                        eventsListened.remove(event);
                        deletedIds.add(updatedEvent.getId());
                    } else
                        event = updatedEvent;
                    isNew = false;
                    break;
                }
            }

            if (isNew) {
                eventsListened.add(updatedEvent);
                Log.d(LOG, "*******don't know what this is, should not be here, commented some code here");
               /* EventRunningAfterLearned event = new EventRunningAfterLearned();
				event.setHbeId(updatedEvent.getId());
				event.setEventId(updatedEvent.getEventId());
				event.setTimeToStop(updatedEvent.getTimeToTrigger()
                        + updatedEvent.getDuration());
				if (updatedEvent.getValue() == 0)
					event.value = 1;
				else
					event.value = 0;
				trackedEvents.add(event);*/
            }
        } else {
            for (Iterator iterator = eventsListened.iterator(); iterator
                    .hasNext(); ) {
                HackBotEvent event = (HackBotEvent) iterator.next();
                if (updatedEvent.getEventId() == event.getEventId()) {
                    eventsListened.remove(event);
                    deletedIds.add(updatedEvent.getId());
                }
            }
        }

        if (deletedIds.size() > 0) {
            Log.d(LOG, "*******don't know what this is, should not be here");
            for (Integer id : deletedIds) {
                for (Iterator iterator = eventsRunning.iterator(); iterator
                        .hasNext(); ) {
                    EventRunningAfterLearned event = (EventRunningAfterLearned) iterator.next();
                    if (event.getHbeId() == id) {
                        eventsRunning.remove(event);

                    }
                    if (dbHelper != null)
                        dbHelper.deleteEventRunning(id);
                }
            }
        }
    }

    //this will insert the dummy data
    public void fillDummyData() {
        Log.d(LOG, "in fillDummyData");
		/*HackBotEvent event = new HackBotEvent();
		event.setId(1);
        event.setEventId(5);
        event.setTimeToTrigger(120);
        event.setFirstOccurrence();
        event.setLastOccurrence();
        event.setTimesOccurred("1111111");
        event.setProbability(100);
        event.setDuration(3600000);
        event.setPattern("1111111");
        event.setRepeatedWeekly(1);
        event.setRepeatInDays(0);
        event.setDaysTracked();
        event.setIsLearned(1);
        event.setIsExecuting();
        event.setDaysFulfilled();
        event.setValue();*/



		/*event.setDuration(30);
		event.setTimeToTrigger(Calendar.getInstance().getTimeInMillis());
		event.setEventId(2);
		event.setValue(0);
		eventsListened.add(event);
		HackBotEvent event2 = new HackBotEvent();
		event2.setId(2);
		event2.setDuration(30);
		event2.setTimeToTrigger(Calendar.getInstance().getTimeInMillis() + 2000);
		event2.setEventId(4);
		event2.setValue(0);
		eventsListened.add(event2);*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG, "in onDestroy");
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {

        Log.d(LOG, "in onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        Log.d(LOG, "in onUnbind");
        return true;
    }
}