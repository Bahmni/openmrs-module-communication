package org.bahmni.module.communication.service;

import org.bahmni.module.communication.model.MailContent;
import org.openmrs.annotation.Authorized;

public interface CommunicationService {
	
	@Authorized({ "Send Email" })
	void sendEmail(MailContent mailContent);
	
	void sendSMS(String phoneNumber, String message);
	
}
