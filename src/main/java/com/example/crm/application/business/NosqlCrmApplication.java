package com.example.crm.application.business;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.crm.application.CrmApplication;
import com.example.crm.application.business.exception.CustomerAlreadyExistException;
import com.example.crm.application.business.exception.CustomerNotFoundException;
import com.example.crm.application.event.CustomerAcquiredEvent;
import com.example.crm.application.event.CustomerDeleteEvent;
import com.example.crm.document.CustomerDocument;
import com.example.crm.dto.request.AcquireCustomerRequest;
import com.example.crm.dto.request.UpdateCustomerRequest;
import com.example.crm.dto.response.AcquireCustomerResponse;
import com.example.crm.dto.response.CustomerResponse;
import com.example.crm.dto.response.DeleteCustomerResponse;
import com.example.crm.dto.response.DetailedCustomerResponse;
import com.example.crm.dto.response.PatchCustomerResponse;
import com.example.crm.dto.response.UpdateCustomerResponse;
import com.example.crm.entity.Customer;
import com.example.crm.repository.CustomerMongoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
@ConditionalOnProperty(name = "crm.persistence", havingValue="mongodb")
public class NosqlCrmApplication implements CrmApplication {
	private static final Logger logger = LoggerFactory.getLogger(NosqlCrmApplication.class);
	// injection işlemi
	private CustomerMongoRepository customerMongoRepository;
	private ModelMapper modelMapper;
	private ApplicationEventPublisher eventPublisher;
	private ObjectMapper objectMapper;
	private KafkaTemplate<String, String> kafkaTemplate;
	public NosqlCrmApplication(CustomerMongoRepository customerMongoRepository, ModelMapper modelMapper,
			ApplicationEventPublisher eventPublisher, ObjectMapper objectMapper,KafkaTemplate<String, String> kafkaTemplate) {
		this.customerMongoRepository = customerMongoRepository;
		this.modelMapper = modelMapper;
		this.eventPublisher = eventPublisher;
		this.objectMapper = objectMapper;
		this.kafkaTemplate = kafkaTemplate;
	}
	@Override
	public DetailedCustomerResponse findCustomerByIdentity(String identity) {
		var customer = customerMongoRepository.findById(identity)
				.orElseThrow( () -> new CustomerNotFoundException());
		return modelMapper.map(customer, DetailedCustomerResponse.class);
	}

	@Override
	public List<CustomerResponse> findAllByPage(int pageNo, int pageSize) {
		var page = PageRequest.of(pageNo, pageSize);
		return customerMongoRepository.findAll(page)
				                      .stream()
				                      .map(cust -> modelMapper.map(cust, CustomerResponse.class))
				                      .toList();
	}

	@Override
	public AcquireCustomerResponse addCustomer(AcquireCustomerRequest request) {
		String identity = request.getIdentity();
		if (customerMongoRepository.existsById(identity )) {
			throw new CustomerAlreadyExistException();
		}
		var customer = modelMapper.map(request, CustomerDocument.class);
		// event sınıfı yazdık 
		
		var customerAcquiredEvent = new CustomerAcquiredEvent(UUID.randomUUID().toString(), identity);
		customer = customerMongoRepository.save(customer);
			eventPublisher.publishEvent(customerAcquiredEvent);
			try {
				var eventAsJson = objectMapper.writeValueAsString(customerAcquiredEvent);
				kafkaTemplate.send("crm-events",eventAsJson);}
			catch(JsonProcessingException e) {
				logger.error("error while converting the event to json : {}", e.getMessage());
			}
					
		return modelMapper.map(customer,
				AcquireCustomerResponse.class);
	}

	@Override
	public UpdateCustomerResponse updateCustomer(String identity, UpdateCustomerRequest request) {
		var customer = customerMongoRepository.findById(identity )
				            .orElseThrow( () -> new CustomerNotFoundException());
        modelMapper.map(request, customer);
		customer.setIdentity(identity);
		return modelMapper.map(customerMongoRepository.save(customer),
					UpdateCustomerResponse.class);
	}

	@Override
	public PatchCustomerResponse patchCustomer(String identity, Map<String, Object> request) {
		var managedCustomer = customerMongoRepository.findById(identity)
				.orElseThrow(() -> new CustomerNotFoundException());
		for (var entry : request.entrySet()) {
			var attribute = entry.getKey();
			var value = entry.getValue();
			System.err.println(entry);
			try {
				var field = Customer.class.getDeclaredField(attribute);
				field.setAccessible(true);
				field.set(managedCustomer, value);
				field.setAccessible(false);
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				System.err.println(e.getMessage());
			}
		}
		return modelMapper.map(customerMongoRepository.save(managedCustomer),
				PatchCustomerResponse.class);
	}

	@Override
	public DeleteCustomerResponse removeCustomerByIdentity(String identity) {
		var customer = customerMongoRepository.findById(identity)
		   		.orElseThrow( () -> new CustomerNotFoundException());
		var customerDeletedEvent = new CustomerDeleteEvent(UUID.randomUUID().toString(), identity);
		eventPublisher.publishEvent(customerDeletedEvent);
		customerMongoRepository.delete(customer);
		
	    return modelMapper.map(customer,DeleteCustomerResponse.class);	
	}

}