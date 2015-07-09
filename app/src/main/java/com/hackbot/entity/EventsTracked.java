package com.hackbot.entity;

public class EventsTracked {
	
	private int id;
	private int eventId;		//this is the id of the event from the Events Table
	private int hbeId;
	
	public int getHbeId() {
		return hbeId;
	}
	public void setHbeId(int hbeId) {
		this.hbeId = hbeId;
	}
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
	

}
