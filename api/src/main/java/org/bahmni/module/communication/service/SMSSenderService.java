package org.bahmni.module.communication.service;

import org.openmrs.Patient;
import org.openmrs.module.appointments.model.Appointment;

public interface SMSSenderService {
	
	String getRegistrationMessage(Patient patient);
	
	String getAppointmentBookingMessage(Appointment appointment);
	String getAppointmentReminderMessage(Appointment appointment);
	
	String sendSMS(String phoneNumber, String message);
}
