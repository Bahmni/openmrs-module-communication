package org.bahmni.module.communication.service;

import java.util.List;
import java.util.Map;

public interface MessageBuilderService {
	
	String getRegistrationMessage(Map<String, String> placeholderValues);
	
	String getAppointmentBookingMessage(Map<String, String> placeholderValues, List<String> providers);
	
	String getRecurringAppointmentBookingMessage(Map<String, String> placeholderValues, List<String> providers);
	
	String getAppointmentReminderMessage(Map<String, String> placeholderValues, List<String> providers);
	
}
