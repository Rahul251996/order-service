package com.quickshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quickshop.entity.OrderCreatedEvent;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class OrderEventSerializer {

	private final ObjectMapper objectMapper;

	public OrderEventSerializer() { 
	 this.objectMapper = new ObjectMapper(); 
     this.objectMapper.registerModule(new JavaTimeModule());
     }
 

	public String toJson(OrderCreatedEvent event) {
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize event", e);
		}
	}
}