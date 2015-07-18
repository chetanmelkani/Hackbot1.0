package com.hackbot.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.hackbot.R;
import com.hackbot.entity.EventIdConstant;
import com.hackbot.entity.Events;
import com.hackbot.services.ActionService;
import com.hackbot.services.EventListenerService;

import java.util.ArrayList;
import java.util.List;

public class EventSettingsActivity extends Activity {

    private CheckBox chkAudio, chk3G, chkBluetooth;
    private Button btnDisplay;
    EventListenerService mService;
    boolean mBound = false;

    private final static String LOG = EventSettingsActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG, "in onCreate");
        setContentView(R.layout.activity_event_settings);
        startService();
        addListenerOnButton();

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
                List<Events> eventList = new ArrayList<Events>();

                //Audio checkbox
                int isAudioChecked = 0;
                if (chkAudio.isChecked()) {
                    isAudioChecked = 1;
                    eventList.add(new Events(EventIdConstant.AUDIO_ON, "AUDIO_ON", isAudioChecked));
                } else {
                    eventList.add(new Events(EventIdConstant.AUDIO_OFF, "AUDIO_OFF", isAudioChecked));
                }                //3G checkbox
                int is3GChecked = 0;
                if (chk3G.isChecked()) {
                    is3GChecked = 1;
                    eventList.add(new Events(EventIdConstant.DATA_ON, "3G_ON", is3GChecked));
                } else {
                    eventList.add(new Events(EventIdConstant.DATA_OFF, "3G_OFF", is3GChecked));
                }

                //Bluetooth checkbox
                int isBluetoothChecked = 0;
                if (chkBluetooth.isChecked()) {
                    isBluetoothChecked = 1;
                    eventList.add(new Events(EventIdConstant.BLUETOOTH_ON, "BLUETOOTH_ON", isBluetoothChecked));
                } else {
                    eventList.add(new Events(EventIdConstant.BLUETOOTH_OFF, "BLUETOOTH_OFF", isBluetoothChecked));
                }

                StringBuffer result = new StringBuffer();
                result.append("IPhone check : ").append(chkAudio.isChecked());
                result.append("\nAndroid check : ").append(
                        chk3G.isChecked());
                result.append("\nWindows Mobile check :").append(
                        chkBluetooth.isChecked());

                Toast.makeText(EventSettingsActivity.this, result.toString(),
                        Toast.LENGTH_LONG).show();
                // Calling Listener Service class
                if (mBound)
                    mService.setBroadCastReciever(eventList);
            }
        });

    }

    public void getListenerService() {
        Log.d(LOG, "in getListenerService");
        Intent intent = new Intent(getBaseContext(), EventListenerService.class);
        getBaseContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    private ServiceConnection mConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(LOG, "in onServiceConnected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance 
            EventListenerService.LocalBinder binder = (EventListenerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }


        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
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
        Log.d(LOG, "in onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.event_settings, menu);
        return true;
    }

    // Method to start the service
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
}


