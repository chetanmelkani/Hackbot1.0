package com.hackbot.entity;

public class EventRunningAfterLearned {
	
	private int id;
	private int eventId;
	private int hbeId;
	private long timeToStop;
	private int value;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getEventId() {
		return eventId;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;

	}

	public int getHbeId() {
		return hbeId;
	}

	public void setHbeId(int hbeId) {
		this.hbeId = hbeId;
	}

	public long getTimeToStop() {
		return timeToStop;
	}

	public void setTimeToStop(long timeToStop) {
		this.timeToStop = timeToStop;
	}
}
