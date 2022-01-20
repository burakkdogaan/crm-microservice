package com.example.crm.application.event;

// eventlerin arasında öncelik sırası var // log gibi düşünülebilir - domainde gerçekleşen değişikleri loglamak gibi
// event sourcing - microservice
public class CustomerAcquiredEvent {
	private String eventId;
	private String identity;

	public CustomerAcquiredEvent(String eventId, String identity) {
		this.identity = identity;
		this.eventId = eventId;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

}
