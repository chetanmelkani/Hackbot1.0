package com.hackbot.dao;

import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.hackbot.entity.EventsTracked;
import com.hackbot.entity.HackBotEvent;
import com.hackbot.utility.Constants;


/**
 * Created by SwatiSh on 4/8/2015.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String LOG = "DBHelper";

    public static final String DATABASE_NAME = "HackBotDB";
    public static final String KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER = "time_to_trigger";
    public static final String KEY_HACK_BOT_EVENTS_FIRST_OCCURRENCE = "first_occurrence";
    public static final String KEY_HACK_BOT_EVENTS_LAST_OCCURRENCE = "last_occurrence";
    public static final String KEY_HACK_BOT_EVENTS_TIMES_OCCURRED = "times_occurred";
    public static final String KEY_HACK_BOT_EVENTS_PROBABILITY = "probability";
    public static final String KEY_HACK_BOT_EVENTS_DURATION = "duration";
    public static final String KEY_HACK_BOT_EVENTS_PATTERN = "pattern";
    public static final String KEY_HACK_BOT_EVENTS_REPEATED_WEEKLY = "repeated_weekly";
    public static final String KEY_HACK_BOT_EVENTS_REPEATED_DAYS = "repeat_in_days";
    public static final String KEY_HACK_BOT_EVENTS_DAYS_TRACKED = "days_tracked";
    public static final String KEY_HACK_BOT_EVENTS_IS_EXECUTING = "is_executing";
    public static final String KEY_HACK_BOT_EVENTS_IS_LEARNED = "is_learned";
    public static final String KEY_HACK_BOT_EVENTS_DAYS_FULFILLED = "days_fulfilled";
    public static final String KEY_HACK_BOT_EVENTS_VALUE = "value";

    private static DBHelper mInstance = null;


    //table names
    private static final String TABLE_EVENTS_TRACKED = "events_tracked";
    private static final String TABLE_EVENTS = "events";
    private static final String TABLE_HACK_BOT_EVENTS = "hack_bot_events";
    private static final String TABLE_EVENTS_RUNNING = "events_running";

    //common columns
    private static final String KEY_ID = "id";
    public static final String KEY_EVENTS_ID = "eventId";

    //TABLE_EVENTS_TRACKED columns
    private static final String KEY_EVENTS_TRACKED_HBEID = "hbe_id";

    //TABLE_EVENTS columns
    private static final String KEY_EVENTS_EVENTS_NAME = "events_name";
    private static final String KEY_EVENTS_IS_ENABLED = "is_enabled";

    //TABLE_EVENTS_RUNNING
    private static final String KEY_EVENTS_RUNNING_TIME_STARTED = "time_started";

    //create table statements
    private static final String CREATE_EVENTS_TRACKED = "CREATE TABLE "
            + TABLE_EVENTS_TRACKED + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_EVENTS_ID
            + " INTEGER, " + KEY_EVENTS_TRACKED_HBEID + " INTEGER)";

    private static final String CREATE_EVENTS = "CREATE TABLE "
            + TABLE_EVENTS + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_EVENTS_EVENTS_NAME
            + " TEXT, " + KEY_EVENTS_IS_ENABLED + " INTEGER)";

    private static final String CREATE_EVENTS_RUNNING = "CREATE TABLE "
            + TABLE_EVENTS_RUNNING + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_EVENTS_ID
            + " INTEGER, " + KEY_EVENTS_TRACKED_HBEID + " INTEGER, " + KEY_EVENTS_RUNNING_TIME_STARTED + " INTEGER)";

    private static final String CREATE_HACK_BOT_EVENTS = "CREATE TABLE IF NOT EXISTS " + TABLE_HACK_BOT_EVENTS +
            " (" + KEY_ID
            + " INTEGER PRIMARY KEY," + KEY_EVENTS_ID
            + " INTEGER ," + KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER
            + " INTEGER," + KEY_HACK_BOT_EVENTS_FIRST_OCCURRENCE
            + " INTEGER," + KEY_HACK_BOT_EVENTS_LAST_OCCURRENCE
            + " INTEGER," + KEY_HACK_BOT_EVENTS_TIMES_OCCURRED
            + " TEXT," + KEY_HACK_BOT_EVENTS_PROBABILITY
            + " INTEGER, " + KEY_HACK_BOT_EVENTS_DURATION
            + " INTEGER, " + KEY_HACK_BOT_EVENTS_PATTERN
            + " TEXT, " + KEY_HACK_BOT_EVENTS_REPEATED_WEEKLY
            + " INTEGER, " + KEY_HACK_BOT_EVENTS_REPEATED_DAYS
            + " INTEGER, " + KEY_HACK_BOT_EVENTS_IS_EXECUTING
            + " INTEGER, " + KEY_HACK_BOT_EVENTS_IS_LEARNED
            + " INTEGER, " + KEY_HACK_BOT_EVENTS_DAYS_TRACKED
            + " INTEGER," + KEY_HACK_BOT_EVENTS_DAYS_FULFILLED
            + " INTEGER," + KEY_HACK_BOT_EVENTS_VALUE
            + " INTEGER )";


//    public DBHelper(Context context)
//    {
//        super(context, DATABASE_NAME, null, 1);
//    }

    private DBHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, 1);
    }

    public static DBHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you 
        // don't accidentally leak an Activity's context.
        if (mInstance == null) {
            mInstance = new DBHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG, "onCreate enter");
        db.execSQL(CREATE_HACK_BOT_EVENTS);
        db.execSQL(CREATE_EVENTS_TRACKED);
        db.execSQL(CREATE_EVENTS);
        db.execSQL(CREATE_EVENTS_RUNNING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG, "onUpgrade enter");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HACK_BOT_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS_TRACKED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS_RUNNING);
        onCreate(db);
    }


    public HackBotEvent getHbeObject(long eventId, long timeTriggered) {

        SQLiteDatabase db = this.getReadableDatabase();
        long timeInMinutes = convertLongTimeToMinutes(timeTriggered);

        String selectQuery = "SELECT  * FROM " + TABLE_HACK_BOT_EVENTS + " WHERE "
                + KEY_EVENTS_ID + " = " + eventId + " AND " + KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER + " BETWEEN "
                + (timeInMinutes - Constants.TIME_RANGE_MINUTES) + " AND " + (timeInMinutes + Constants.TIME_RANGE_MINUTES);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            HackBotEvent hbe = parseRecord(c);
            return hbe;
        } else
            return null;

    }


    public Integer deleteEvent(long id, long time, long range) {
        Log.d(LOG, "deleteEvent enter");
        SQLiteDatabase db = this.getWritableDatabase();
        //return db.query(EVENTS_TABLE_NAME, null, " BETWEEN ? AND ?", new String[] {Long.toString(time - range), Long.toString(time + range)}, null, null, null, null);
        return 0;
    }

    private HackBotEvent parseRecord(Cursor cursor) {
        Log.d(LOG, "parseRecord enter");
        if (cursor.moveToFirst()) {
            HackBotEvent event = new HackBotEvent();
            event.setId(cursor.getInt(0));
            event.setEventId(cursor.getInt(1));
            event.setTimeToTrigger(cursor.getInt(2));
            event.setFirstOccurrence(cursor.getLong(3));
            event.setLastOccurrence(cursor.getLong(4));
            event.setTimesOccurred(cursor.getString(5));
            event.setProbability(cursor.getInt(6));
            event.setDuration(cursor.getLong(7));
            event.setPattern(cursor.getString(8));
            event.setRepeatedWeekly(cursor.getInt(9));
            event.setRepeatInDays(cursor.getInt(10));
            event.setIsExecuting(cursor.getInt(11));
            event.setIsLearned(cursor.getInt(12));
            event.setDaysTracked(cursor.getInt(13));
            event.setDaysFulfilled(cursor.getInt(14));
            event.setValue(cursor.getInt(15));
            return event;
        }
        return null;
    }


    //EventsTracked Table
    public EventsTracked getEventTrackedById(int eventId) {
        Log.d(LOG, "getEventTrackedById enter");
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_EVENTS_TRACKED + " WHERE "
                + KEY_EVENTS_ID + " = " + eventId;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            EventsTracked et = new EventsTracked();
            et.setEventId(c.getInt(c.getColumnIndex(KEY_EVENTS_ID)));
            et.setHbeId(c.getInt(c.getColumnIndex(KEY_EVENTS_TRACKED_HBEID)));
            et.setId(c.getInt(c.getColumnIndex(KEY_ID)));
            return et;
        } else
            return null;

    }

    //HBE Table

    /**
     * this deletes all the events of this eventId in the HBE table and returns there object
     *
     * @param eventId
     * @return
     */
    public void deleteAllEventsByEventId(int eventId) {

        Log.d(LOG, "deleteAllEventsByEventId enter");
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_HACK_BOT_EVENTS, KEY_EVENTS_ID + " = ?",
                new String[]{String.valueOf(eventId)});

    }

    /**
     * get the HBE Object from the hbe unique id
     *
     * @param
     * @return
     */
    public HackBotEvent getHbeById(int id) {
        Log.d(LOG, "getHbeById enter");
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_HACK_BOT_EVENTS + " WHERE "
                + KEY_ID + " = " + id;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            HackBotEvent event = parseRecord(cursor);
            return event;
        }
        return null;

    }


    public int updateHackBotEvent(HackBotEvent hbe) {
        Log.d(LOG, "updateHackBotEvent enter");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ID, hbe.getId());
        contentValues.put(KEY_EVENTS_ID, hbe.getEventId());
        contentValues.put(KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER, hbe.getTimeToTrigger());
        contentValues.put(KEY_HACK_BOT_EVENTS_FIRST_OCCURRENCE, hbe.getFirstOccurrence());
        contentValues.put(KEY_HACK_BOT_EVENTS_LAST_OCCURRENCE, hbe.getLastOccurrence());
        contentValues.put(KEY_HACK_BOT_EVENTS_TIMES_OCCURRED, hbe.getTimesOccurred());
        contentValues.put(KEY_HACK_BOT_EVENTS_PROBABILITY, hbe.getProbability());
        contentValues.put(KEY_HACK_BOT_EVENTS_DURATION, hbe.getDuration());
        contentValues.put(KEY_HACK_BOT_EVENTS_PATTERN, hbe.getPattern());
        contentValues.put(KEY_HACK_BOT_EVENTS_REPEATED_WEEKLY, hbe.getRepeatedWeekly());
        contentValues.put(KEY_HACK_BOT_EVENTS_REPEATED_DAYS, hbe.getRepeatInDays());
        contentValues.put(KEY_HACK_BOT_EVENTS_DAYS_TRACKED, hbe.getDaysTracked());
        contentValues.put(KEY_HACK_BOT_EVENTS_IS_EXECUTING, hbe.getIsExecuting());
        contentValues.put(KEY_HACK_BOT_EVENTS_IS_LEARNED, hbe.getIsLearned());
        contentValues.put(KEY_HACK_BOT_EVENTS_DAYS_FULFILLED, hbe.getDaysFulfilled());
        contentValues.put(KEY_HACK_BOT_EVENTS_VALUE, hbe.getValue());
        Log.d(LOG, "database updated");
        // updating row
        return db.update(TABLE_HACK_BOT_EVENTS, contentValues, KEY_ID + " = ?",
                new String[]{String.valueOf(hbe.getId())});
    }

    private long convertLongTimeToMinutes(long time) {
        Date d = new Date(time);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        long timeToTrigger = c.get(Calendar.HOUR_OF_DAY) * 60;
        timeToTrigger += c.get(Calendar.MINUTE);
        return timeToTrigger;
    }

    /**
     * return 1 if it is a new event, which has no previous entry in the DB at this triggerTime
     * else return 0
     *
     * @param eventId
     * @param timeTriggered
     * @return
     */
    public int isNewEvent(int eventId, long timeTriggered) {
        Log.d(LOG, "isNewEvent enter");
        SQLiteDatabase db = this.getReadableDatabase();

        //check if this event id is already in events tracking table
        EventsTracked et = getEventTrackedById(eventId);
        if (et != null) {
            return 0;
        }
        //KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER this is in minutes
        // convert time triggered to minutes
        long timeInMinutes = convertLongTimeToMinutes(timeTriggered);

        String selectQuery = "SELECT  * FROM " + TABLE_HACK_BOT_EVENTS + " WHERE "
                + KEY_EVENTS_ID + " = " + eventId + " AND " + KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER
                + " BETWEEN " + (timeInMinutes - Constants.TIME_RANGE_MINUTES) + " AND " + (timeInMinutes + Constants.TIME_RANGE_MINUTES);
        Log.d(LOG, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            int i = c.getInt(c.getColumnIndex(KEY_EVENTS_ID));
            return 0;
        } else
            return 1;

    }

    /**
     * will return 1 if it has to be learned if not then 0 and -1 if it has an entry in the EventsTracked table
     * It will be learned when an event already occurs at the same approx time and also the event is currently not in the EventsTracked Table
     *
     * @param eventId
     * @param timeTriggered
     * @return
     */
    public int isEventToLearn(int eventId, long timeTriggered) {

        Log.d(LOG, "isEventToLearn enter");
        SQLiteDatabase db = this.getReadableDatabase();

        EventsTracked et = getEventTrackedById(eventId);
        if (et != null) {
            return -1;
        } else {
            long timeInMinutes = convertLongTimeToMinutes(timeTriggered);
            String selectQuery = "SELECT  * FROM " + TABLE_HACK_BOT_EVENTS + " WHERE "
                    + KEY_EVENTS_ID + " = " + eventId + " AND " + KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER
                    + " BETWEEN " + (timeInMinutes - Constants.TIME_RANGE_MINUTES) + " AND " + (timeInMinutes + Constants.TIME_RANGE_MINUTES)
                    + " AND " + timeTriggered + " - " + KEY_HACK_BOT_EVENTS_LAST_OCCURRENCE + " > 900000";            //rd22 latest change, not tested
            //to stop if the same event is continuous
            Log.d(LOG, selectQuery);
            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
                int i = c.getInt(c.getColumnIndex(KEY_EVENTS_ID));
                return 1;
            } else
                return 0;
        }
    }

    /**
     * Insert to HBE table
     *
     * @param hbe
     * @return
     */
    public long insertToHackBotEvents(HackBotEvent hbe) {
        Log.d(LOG, "insertToHackBotEvents enter");
        printTheHBETest(hbe);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_EVENTS_ID, hbe.getEventId());
        contentValues.put(KEY_HACK_BOT_EVENTS_TIME_TO_TRIGGER, hbe.getTimeToTrigger());
        contentValues.put(KEY_HACK_BOT_EVENTS_FIRST_OCCURRENCE, hbe.getFirstOccurrence());
        contentValues.put(KEY_HACK_BOT_EVENTS_LAST_OCCURRENCE, hbe.getLastOccurrence());
        contentValues.put(KEY_HACK_BOT_EVENTS_TIMES_OCCURRED, hbe.getTimesOccurred());
        contentValues.put(KEY_HACK_BOT_EVENTS_PROBABILITY, hbe.getProbability());
        contentValues.put(KEY_HACK_BOT_EVENTS_DURATION, hbe.getDuration());
        contentValues.put(KEY_HACK_BOT_EVENTS_PATTERN, hbe.getPattern());
        contentValues.put(KEY_HACK_BOT_EVENTS_REPEATED_WEEKLY, hbe.getRepeatedWeekly());
        contentValues.put(KEY_HACK_BOT_EVENTS_REPEATED_DAYS, hbe.getRepeatInDays());
        contentValues.put(KEY_HACK_BOT_EVENTS_DAYS_TRACKED, hbe.getDaysTracked());
        contentValues.put(KEY_HACK_BOT_EVENTS_IS_EXECUTING, hbe.getIsExecuting());
        contentValues.put(KEY_HACK_BOT_EVENTS_IS_LEARNED, hbe.getIsLearned());
        contentValues.put(KEY_HACK_BOT_EVENTS_DAYS_FULFILLED, hbe.getDaysFulfilled());
        contentValues.put(KEY_HACK_BOT_EVENTS_VALUE, hbe.getValue());
        Log.d(LOG, "database updated");
        return db.insert(TABLE_HACK_BOT_EVENTS, null, contentValues);
    }

    //EVENTS_TRACKED Table
    public long insertEventsTracked(EventsTracked et) {
        Log.d(LOG, "insertEventsTracked enter");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EVENTS_ID, et.getEventId());
        values.put(KEY_EVENTS_TRACKED_HBEID, et.getHbeId());

        // insert row
        long id = db.insert(TABLE_EVENTS_TRACKED, null, values);
        Log.d(LOG, "database updated");
        return id;
    }

    public void deleteEventsTracked(EventsTracked et) {
        Log.d(LOG, "deleteEventsTracked enter");
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_EVENTS_TRACKED, KEY_ID + " = ?",
                new String[]{String.valueOf(et.getId())});
        Log.d(LOG, "database updated");
    }

    //EVENTS_RUNNING Table

    /**
     * @param er HackBotEvent uniqueId and the time it started
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public int insertEventsRunning(int hbeId, long timeStarted) {
        Log.d(LOG, "insertEventsTracked enter");
        SQLiteDatabase db = this.getWritableDatabase();

        HackBotEvent hbe = getHbeById(hbeId);
        ContentValues values = new ContentValues();
        values.put(KEY_EVENTS_ID, hbe.getEventId());
        values.put(KEY_EVENTS_TRACKED_HBEID, hbeId);
        values.put(KEY_EVENTS_RUNNING_TIME_STARTED, timeStarted);

        // insert row
        long id = db.insert(TABLE_EVENTS_RUNNING, null, values);
        Log.d(LOG, "database updated");
        return (int) id;
    }

    /**
     * delete this entry from the eventsRunning table for the eventId
     * at any moment this table will have only one entry for for any eventId
     *
     * @param hbeId
     * @return
     */
    public int deleteEventRunning(int id) {
        Log.d(LOG, "deleteEventRunning enter");
        SQLiteDatabase db = this.getWritableDatabase();

        Log.d(LOG, "database updated");
        return db.delete(TABLE_EVENTS_RUNNING, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});

    }

    //EventRunning Table

    public int isLearned(HackBotEvent hbe) {
        Log.d(LOG, "setLearned " + hbe.getEventId());
        if (hbe.getDaysFulfilled() == 1 && hbe.getProbability() >= 75)
            return 1;
        else if (hbe.getDaysFulfilled() == 1 && hbe.getProbability() < 75)
            return 0;
        else
            return -1;
    }

    /**
     * returns 1 if the event is currently running else 0, this takes as input only those event
     *
     * @param eventId
     * @param timeTriggered
     * @param range
     * @return
     */
    public int isEventRunning(int eventId) {            // get if this event is currently running or not
        Log.d(LOG, "isEventRunning enter");
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_EVENTS_RUNNING + " WHERE "
                + KEY_EVENTS_ID + " = " + eventId;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            return 1;
        }
        return 0;
    }

    /**
     * This returns HBE object if it has to be unlearned
     *
     * @param eventId
     * @param timeTriggeredOff
     * @return
     */
    public HackBotEvent getUnlearnEvent(int eventId, long timeTriggeredOff) {
        Log.d(LOG, "getUnlearnEvent enter");
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_EVENTS_RUNNING + " WHERE "
                + KEY_EVENTS_ID + " = " + eventId;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            int hbeId = c.getInt(c.getColumnIndex(KEY_EVENTS_TRACKED_HBEID));
            long timeStarted = c.getInt(c.getColumnIndex(KEY_EVENTS_RUNNING_TIME_STARTED));

            HackBotEvent hbe = getHbeById(hbeId);
            if (Math.abs(hbe.getDuration() - (timeTriggeredOff - timeStarted)) > Constants.TIME_RANGE)
                return hbe;
        }

        return null;
    }

    private void printTheHBETest(HackBotEvent hbe) {
        Log.d(LOG,
                "hbe.getDaysFulfilled()" + hbe.getDaysFulfilled() +
                        "hbe.getDaysTracked()" + hbe.getDaysTracked() +
                        "hbe.getDuration()" + hbe.getDuration() +
                        "hbe.getEventId()" + hbe.getEventId() +
                        "hbe.getFirstOccurrence()" + hbe.getFirstOccurrence() +
                        "hbe.getId()" + hbe.getId() +
                        "hbe.getIsExecuting()" + hbe.getIsExecuting() +
                        "hbe.getIsLearned()" + hbe.getIsLearned() +
                        "hbe.getLastOccurrence()" + hbe.getLastOccurrence() +
                        "hbe.getPattern()" + hbe.getPattern() +
                        "hbe.getProbability()" + hbe.getProbability() +
                        "hbe.getRepeatedWeekly()" + hbe.getRepeatedWeekly() +
                        "hbe.getRepeatInDays()" + hbe.getRepeatInDays() +
                        "hbe.getTimesOccurred()" + hbe.getTimesOccurred() +
                        "hbe.getTimeToTrigger()" + hbe.getTimeToTrigger() +
                        "hbe.getValue()" + hbe.getValue());
    }

}