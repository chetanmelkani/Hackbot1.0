package com.hackbot.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.hackbot.R;
import com.facebook.stetho.Stetho;
import com.hackbot.businessLogic.Algo;
import com.hackbot.dao.DBHelper;
import com.hackbot.entity.EventIdConstant;
import com.hackbot.entity.Events;
import com.hackbot.services.ActionService;
import com.hackbot.services.EventListenerService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventSettingsActivity extends Activity {

    private CheckBox chkAudio, chk3G, chkBluetooth;
    private Button btnDisplay;
    private DBHelper dbh;
    private EventListenerService mService;
    boolean mBound = false;

    private final static String LOG = "HackBot" +EventSettingsActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG, "in onCreate");
        setContentView(R.layout.activity_event_settings);
        this.dbh = DBHelper.getInstance(this);
        startService();
        addListenerOnButton();

        new AlertDialog.Builder(this)
                .setTitle(Html.fromHtml("<font color='#2196F3'>Hello</font>"))
                .setMessage("Hi! There, HackBot is your personal assistant, which will learn your activities and replicate them. "
                        + "Just get ready for a hassle free experience by selecting the checkboxes and press start.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .show();

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());

    }


    public void addListenerOnButton() {
        Log.d(LOG, "in addListenerOnButton");

        chkAudio = (CheckBox) findViewById(R.id.chkAudio);
        chk3G = (CheckBox) findViewById(R.id.chk3G);
        chkBluetooth = (CheckBox) findViewById(R.id.chkBluetooth);
        btnDisplay = (Button) findViewById(R.id.btnDisplay);

        btnDisplay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(LOG, "start button pressed");
                List<Events> eventList = new ArrayList<>();

                //Audio checkbox
                int isAudioChecked = 0;
                if (chkAudio.isChecked()) {
                    Log.d(LOG, "in chkAudio.isChecked()");
                    isAudioChecked = 1;
                    eventList.add(new Events(EventIdConstant.AUDIO_ON, "AUDIO_ON", isAudioChecked));
                } else {
                    Log.d(LOG, "in chkAudio not checked");
                    eventList.add(new Events(EventIdConstant.AUDIO_OFF, "AUDIO_OFF", isAudioChecked));
                }

                //3G checkbox
                int is3GChecked = 0;
                if (chk3G.isChecked()) {
                    Log.d(LOG, "in chk3G.isChecked()");
                    is3GChecked = 1;
                    eventList.add(new Events(EventIdConstant.DATA_ON, "3G_ON", is3GChecked));
                } else {
                    Log.d(LOG, "in is3GChecked not checked");
                    eventList.add(new Events(EventIdConstant.DATA_OFF, "3G_OFF", is3GChecked));
                }

                //Bluetooth checkbox
                int isBluetoothChecked = 0;
                if (chkBluetooth.isChecked()) {
                    Log.d(LOG, "in chkBluetooth.isChecked()");
                    isBluetoothChecked = 1;
                    eventList.add(new Events(EventIdConstant.BLUETOOTH_ON, "BLUETOOTH_ON", isBluetoothChecked));
                } else {
                    Log.d(LOG, "in isBluetoothChecked not checked");
                    eventList.add(new Events(EventIdConstant.BLUETOOTH_OFF, "BLUETOOTH_OFF", isBluetoothChecked));
                }

                StringBuffer result = new StringBuffer();
                result.append("IPhone check : ").append(chkAudio.isChecked());
                result.append("\nAndroid check : ").append(chk3G.isChecked());
                result.append("\nWindows Mobile check :").append(chkBluetooth.isChecked());

                Toast.makeText(EventSettingsActivity.this, result.toString(), Toast.LENGTH_LONG).show();

                // Calling Listener Service class
                if (mBound)
                    mService.setBroadCastReceiver(eventList);
            }
        });

    }

    public void getListenerService() {
        Log.d(LOG, "in getListenerService");
        Intent intent = new Intent(getBaseContext(), EventListenerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(LOG, "in onServiceConnected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance 
            EventListenerService.LocalBinder binder = (EventListenerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(LOG, "in onServiceDisconnected");
            mBound = false;
        }
    };


    @Override
    protected void onDestroy() {
        Log.d(LOG, "in onDestroy");
        super.onDestroy();
        if (mConnection != null) {
            unbindService(mConnection);
        }
        stopService();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.event_settings, menu);
        return true;
    }

    // Method to start the services
    public void startService() {
        Log.d(LOG, "in startService");
        startService(new Intent(getBaseContext(), EventListenerService.class));
        startService(new Intent(getBaseContext(), ActionService.class));
        getListenerService();

    }

    // Method to stop the service
    public void stopService() {
        Log.d(LOG, "in stopService");
        stopService(new Intent(getBaseContext(), EventListenerService.class));
        stopService(new Intent(getBaseContext(), ActionService.class));
    }

    public void cleanSlate(View view) {

        //This will wipe the entire DB
        Log.d(LOG, "in cleanSlate");
        dbh.deleteAllData();
        Log.d(LOG, "all data deleted");
    }

    public void changeSettings(View view) {
        //This will enable to change the settings
        Log.d(LOG, "in changeSettings");
        //this is making the dummy calls for now
        /*TextView eventId = (TextView) findViewById(R.id.eventId);
        TextView time = (TextView) findViewById(R.id.time);
        TextView value = (TextView) findViewById(R.id.value);*/

     //   int eventIdInteger = Integer.parseInt(eventId.getText().toString());
     //   long timeLong = Long.parseLong(time.getText().toString());
     //   int valueInteger = Integer.parseInt(value.getText().toString());

        Algo algo = new Algo(this);
        //valueInteger should always be 1

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE) + 2;

        long timeLong = (1436121000000L + (hours * 60 * 60 + minutes * 60) * 1000 );         //6th July Monday 00:00 AM
        int eventIdInteger = 5;
        int valueInteger = 1;
        long durationOfEvent = 3*60*1000;

        calendar.setTime(new Date(timeLong));

        Toast.makeText(this, "Time entered : "
                + calendar.get(Calendar.DATE) + ":"
                + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                + calendar.get(Calendar.MINUTE) + ":"
                + calendar.get(Calendar.SECOND)
                , Toast.LENGTH_SHORT).show();

        Log.d(LOG, "the time being set i the DB is :" + timeLong);
        Log.d(LOG, "Time entered : "
                + calendar.get(Calendar.DATE) + ":"
                + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                + calendar.get(Calendar.MINUTE) + ":"
                + calendar.get(Calendar.SECOND));

        for(int i=0;i<5;i++){
            algo.writeToDB(5, timeLong , valueInteger);

            //enter the end case of the above call
            algo.writeToDB(eventIdInteger, (timeLong + durationOfEvent), 0);     //add 5 minutes to the end event, duration 5 min


            Log.d(LOG, "taking a nap for 2 seconds");
/*            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                Log.d(LOG, "the nap got fucked up!!");
                e.printStackTrace();
            }*/
            timeLong += 24*60*60*1000;

        }

        Log.d(LOG, "this should be learned");
        long otherTime = 1436725800000L + (hours * 60 * 60 + minutes * 60) * 1000 ;        //13th July Friday 00:00 AM
        algo.writeToDB(5, otherTime, valueInteger);
        algo.writeToDB(5, otherTime + durationOfEvent, 0);     //end this event

    }

    public void killServices(View view) {
        Log.d(LOG, "in killServices");
        //this will remove the entire DB, will stop all the services and unlearn all the learned events.
    }
}


