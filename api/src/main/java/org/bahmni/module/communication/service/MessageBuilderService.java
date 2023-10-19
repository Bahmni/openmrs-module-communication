package org.bahmni.module.communication.service;

import java.util.List;
import java.util.Map;

public interface MessageBuilderService {
	
	String getRegistrationMessage(Map<String, String> arguments);
	
	String getAppointmentBookingMessage(Map<String, String> arguments, List<String> providers);
	
	String getRecurringAppointmentBookingMessage(Map<String, String> arguments, List<String> providers);
	
	String getAppointmentReminderMessage(Map<String, String> arguments, List<String> providers);
	
}
