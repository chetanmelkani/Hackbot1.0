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
	private List<HackBotEvent> eventsListened = new ArrayList<>();
	private List<TrackedEvent> trackedEvents = new ArrayList<>();

	private final IBinder mBinder = new LocalBinder();

	private DBHelper dbHelper;

	private final static String LOG = "HackBot" + ActionService.class.getSimpleName();

	class TrackedEvent {
		int id;
		int eventId;
		int value;
		long trigerringTime;
	}

	public class LocalBinder extends Binder {
		public ActionService getService() {
			// Return this instance of LocalService so clients can call public methods
			Log.d(LOG, "in getService");
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

				for (HackBotEvent bot : eventsListened) {
					if (bot.toTriggerOrNot(Calendar.getInstance()
							.getTimeInMillis())) {
						switch (bot.getEventId()) {
						case EventIdConstant.AUDIO_OFF:
						case EventIdConstant.AUDIO_ON:
							audioChange(bot.getValue());
							dbHelper.insertEventsRunning(bot.getId(),Calendar.getInstance().getTimeInMillis());
							break;

						case EventIdConstant.BLUETOOTH_OFF:
						case EventIdConstant.BLUETOOTH_ON:
							bluetoothChange(bot.getValue());
							dbHelper.insertEventsRunning(bot.getId(),Calendar.getInstance().getTimeInMillis());
							break;

						case EventIdConstant.DATA_OFF:
						case EventIdConstant.DATA_ON:
							dataChange(bot.getValue());
							dbHelper.insertEventsRunning(bot.getId(),Calendar.getInstance().getTimeInMillis());
							break;

						}
					}
				}

                Log.d(LOG, "The size of trackedEvents is " + trackedEvents.size());
				for (TrackedEvent bot : trackedEvents) {
					
						switch (bot.eventId) {
						case EventIdConstant.AUDIO_OFF:
						case EventIdConstant.AUDIO_ON:
							audioChange(bot.value);
							break;

						case EventIdConstant.BLUETOOTH_OFF:
						case EventIdConstant.BLUETOOTH_ON:
							bluetoothChange(bot.value);
							break;

						case EventIdConstant.DATA_OFF:
						case EventIdConstant.DATA_ON:
							dataChange(bot.value);
							break;
					}
				}
			}
		};

		// schedule the task to run starting now and then every minute...
		timer.schedule(hourlyTask, 0, 1000 * 60);
		return START_STICKY;
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
		if (valueSet == 1)
			bluetooth.enable();
		else
			bluetooth.disable();
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

	public void fillListenedEventList(HackBotEvent updatedEvent) {
		Log.d(LOG, "in fillListenedEventList");
		boolean isNew = true;
		List<Integer> deletedIds = new ArrayList<Integer>();

		if (updatedEvent != null && updatedEvent.getId() != 0) {
			for (Iterator iterator = eventsListened.iterator(); iterator
					.hasNext();) {
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
				TrackedEvent event = new TrackedEvent();
				event.id = updatedEvent.getId();
				event.eventId = updatedEvent.getEventId();
				event.trigerringTime = updatedEvent.getTimeToTrigger()
						+ updatedEvent.getDuration();
				if (updatedEvent.getValue() == 0)
					event.value = 1;
				else
					event.value = 0;
				trackedEvents.add(event);
			}
		} else {
			for (Iterator iterator = eventsListened.iterator(); iterator
					.hasNext();) {
				HackBotEvent event = (HackBotEvent) iterator.next();
				if (updatedEvent.getEventId() == event.getEventId()) {
					eventsListened.remove(event);
					deletedIds.add(updatedEvent.getId());
				}
			}
		}

		if (deletedIds.size() > 0) {
			for (Integer id : deletedIds) {
				for (Iterator iterator = trackedEvents.iterator(); iterator
						.hasNext();) {
					TrackedEvent event = (TrackedEvent) iterator.next();
					if (event.id == id) {
						trackedEvents.remove(event);
					
					}
					if(dbHelper!=null)
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