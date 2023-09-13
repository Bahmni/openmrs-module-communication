package org.bahmni.module.communication.eventListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.communication.service.SMSSenderService;
import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SMSEventListener {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	SMSSenderService smsService;

	@Autowired
	AppointmentsService appointmentsService;

	@Async("EventsAsyncCommunication")
	@EventListener
	public void onApplicationEvent(Event event) {
		try {
			Context.openSession();
			Context.setUserContext(event.userContext);
			Thread.sleep(100000);//added beacuse db was taking time to get the data reflected
			if (event.eventType == BahmniEventType.BAHMNI_PATIENT_CREATED) {
				handlePatientCreatedEvent((SimpleObject) event.payload);
			} else if (event.eventType == BahmniEventType.BAHMNI_APPOINTMENT_CREATED
			        || event.eventType == BahmniEventType.BAHMNI_RECURRING_APPOINTMENT_CREATED) {
				handleAppointmentCreatedEvent((AppointmentDefaultResponse) event.payload);
			}
		}
		catch (Exception e) {
			log.error("Exception occurred during event processing", e);
		}
		finally {
			Context.closeSession();
		}
	}
	
	private void handlePatientCreatedEvent(SimpleObject payload) {
		SimpleObject person = payload.get("person");
		if (person != null && person.containsKey("uuid")) {
			PatientService patientService=Context.getRegisteredComponent("patientService",PatientService.class);
			Patient patient = patientService.getPatientByUuid(person.get("uuid"));
			String phoneNumber = getPhoneNumber(patient);
			if (phoneNumber != null) {
				String message = smsService.getRegistrationMessage(patient);
				smsService.sendSMS(phoneNumber, message);
			}
		}
	}
	
	private void handleAppointmentCreatedEvent(AppointmentDefaultResponse appointment) {
		Patient patient = Context.getPatientService().getPatientByUuid((String) appointment.getPatient().get("uuid"));
		String phoneNumber = getPhoneNumber(patient);
		if (phoneNumber != null) {

			String message = smsService.getAppointmentBookingMessage(appointmentsService.getAppointmentByUuid(appointment.getUuid()));
			smsService.sendSMS(phoneNumber, message);
		}
	}
	
	private String getPhoneNumber(Patient patient) {
		PersonAttribute phoneNumber = patient.getAttribute("phoneNumber");
		if (phoneNumber == null) {
			log.info("No mobile number found for the patient. SMS not sent.");
			return null;
		}
		return phoneNumber.getValue();
	}
}
