package com.example.crm.application.event;

public class CustomerDeleteEvent {
	private String eventId;
	private String identity;
	public CustomerDeleteEvent(String eventId, String identity) {
		this.eventId = eventId;
		this.identity = identity;
	}
	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	public String getIdentity() {
		return identity;
	}
	public void setIdentity(String identity) {
		this.identity = identity;
	}
	
}
