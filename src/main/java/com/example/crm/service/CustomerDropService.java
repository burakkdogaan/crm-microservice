package com.example.crm.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.example.crm.application.CrmApplication;
import com.example.crm.application.event.CustomerDeleteEvent;

@Service
public class CustomerDropService {
	private CrmApplication crmApplication;

	public CustomerDropService(CrmApplication crmApplication) {
		this.crmApplication = crmApplication;
	}
	
	@EventListener
	public void handleCustomerDeleteEvent(CustomerDeleteEvent event) {
		var identity = event.getIdentity();
		var customer = crmApplication.findCustomerByIdentity(identity);
		var email = customer.getEmail();
		System.err.println("sending Delete the database System message to customer..." + email);
	}
}
