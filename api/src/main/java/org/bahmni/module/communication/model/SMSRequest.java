package org.bahmni.module.communication.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SMSRequest {
	
	private String phoneNumber;
	
	private String message;
	
	@JsonProperty
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	@JsonProperty
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
