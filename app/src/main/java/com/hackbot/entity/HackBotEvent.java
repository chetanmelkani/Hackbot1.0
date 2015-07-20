package com.hackbot.entity;

import java.util.Calendar;
import java.util.Date;

import com.hackbot.utility.Constants;

public class HackBotEvent
{

	private int id;						//unique id of each event, primary key 
    private int eventId;				//eventId from Events table:1, 3, 5
    private long timeToTrigger;			//this value is in minutes, if on any day the event has to be triggered on 2 AM then this value will be 120
    private long firstOccurrence;		//time in long when event first occurred
    private long lastOccurrence;		//time in long when event last occurred
    private String timesOccurred;		//11011 then the first one from the left is the latest value and so on, keeps tracks of the last 15 days
    private long probability;			//to execute an event check both the probability and eventLearned
    private long duration;				//duration of the event in milliseconds
    private String pattern;				//pattern to track the event weekly 0010000
    private int repeatedWeekly;			//a flag 1 or 0 based on recursion in a week
    private long repeatInDays;			//if not repeated weekly after how many days will be repeated
    private long daysTracked;			//number of days this event has been tracked
    private int isLearned;				//this has value -1, 0 and 1, -1 is when not learned, not unlearn, 0 is unlearn, 1 is learned
    private int isExecuting;			//is 1 when the event is running else 0
    private int daysFulfilled;			//to execute an event check both the probability and eventLearned; eventLearned is true when getDaysTracked>=7 and getTimesOccurred>=3
    private int value;					//this value is 1 when the the state is on and 0 when the state is off

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getDaysFulfilled() {
		return daysFulfilled;
	}

	public void setDaysFulfilled(int daysFulfilled) {
		this.daysFulfilled = daysFulfilled;
	}

	public int getIsExecuting() {
		return isExecuting;
	}

	public void setIsExecuting(int isExecuting) {
		this.isExecuting = isExecuting;
	}

	public int getIsLearned() {
		return isLearned;
	}

	public void setIsLearned(int eventLearned) {
		this.isLearned = eventLearned;
	}

	/**
	 * this is in minutes, the time in a day at which this event will occur
	 * @return
	 */
	public long getTimeToTrigger() {
        return timeToTrigger;
    }

    public void setTimeToTrigger(long timeToTrigger) {
        this.timeToTrigger = timeToTrigger;
    }

    public long getFirstOccurrence() {
        return firstOccurrence;
    }

    public void setFirstOccurrence(long firstOccurrence) {
        this.firstOccurrence = firstOccurrence;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public long getLastOccurrence() {
        return lastOccurrence;
    }

    public void setLastOccurrence(long lastOccurrence) {
        this.lastOccurrence = lastOccurrence;
    }

    public long getProbability() {
        return probability;
    }

    public void setProbability(long probability) {
        this.probability = probability;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public long getRepeatInDays() {
        return repeatInDays;
    }

    public void setRepeatInDays(long repeatInDays) {
        this.repeatInDays = repeatInDays;
    }

	public long getDaysTracked() {
		return daysTracked;
	}

	public void setDaysTracked(long daysTracked) {
		this.daysTracked = daysTracked;
	}

	public String getTimesOccurred() {
		return timesOccurred;
	}

	public void setTimesOccurred(String timesOccurred) {
		this.timesOccurred = timesOccurred;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRepeatedWeekly() {
		return repeatedWeekly;
	}

	public void setRepeatedWeekly(int repeatedWeekly) {
		this.repeatedWeekly = repeatedWeekly;
	}
	
	/**
	 * this method returns true when at this time the event has to be executed and false when it has not to be
	 * @param time
	 * @return
	 */
	public boolean toTriggerOrNot(long time){
		if(this.repeatedWeekly==1){
			
			Date d = new Date(time);
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			
			String patternArray[] = stringToArray(pattern);
			if(patternArray[dayOfWeek-1]=="1"){		//the time matches the pattern day
				long timeToTrigger = c.get(Calendar.HOUR_OF_DAY) * 60;
				timeToTrigger += c.get(Calendar.MINUTE);
				
				if(timeToTrigger==this.timeToTrigger)
					return true;
				else
					return false;
			}
			else
				return false;
		}
		else{
			if((time - this.lastOccurrence)/Constants.MILLISECONDS_A_DAY == this.repeatInDays){
				Date d = new Date(time);
				Calendar c = Calendar.getInstance();
				c.setTime(d);
				long timeToTrigger = c.get(Calendar.HOUR_OF_DAY) * 60;
				timeToTrigger += c.get(Calendar.MINUTE);
				
				if(timeToTrigger==this.timeToTrigger)
					return true;
				else
					return false;
			}
			else
				return false;
		}
	}
	
	
	private String[] stringToArray(String str){
		String arr[] = str.split("");  
		String patternArray[] = new String[arr.length-1];
		System.arraycopy(arr, 1, patternArray, 0, patternArray.length);
		return patternArray;
	}
}
