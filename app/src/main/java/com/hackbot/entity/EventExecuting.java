package com.hackbot.entity;

public class EventExecuting {
	
	private int id;
	private int eventId;
	private int hbeId;
	private long timeStarted;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getEventId() {
		return eventId;
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
	public long getTimeStarted() {
		return timeStarted;
	}
	public void setTimeStarted(long timeStarted) {
		this.timeStarted = timeStarted;
	}
	
}
