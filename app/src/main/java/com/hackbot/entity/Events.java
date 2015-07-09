package com.hackbot.entity;

public class Events {
	
	private int id;
	private String eventName;
	private int isEnabled;
	
	public Events(int id, String eventName, int isEnabled) {
		super();
		this.id = id;
		this.eventName = eventName;
		this.isEnabled = isEnabled;
	}
	
	public Events(){
		
	}
	public int getIsEnabled() {
		return isEnabled;
	}
	public void setIsEnabled(int isEnabled) {
		this.isEnabled = isEnabled;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	
	

}
