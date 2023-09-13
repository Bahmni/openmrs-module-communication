package org.bahmni.module.communication.service.impl;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentVisitLocation;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bahmni.module.communication.util.DateUtil.convertUTCToGivenFormat;

@Service
public class SMSTemplateService {

	@Autowired
	AppointmentsService appointmentsService;
	
	public Map<String, Object> createArgumentsMapForPatientRegistration(Patient patient) {
        String helpdeskNumber = Context.getAdministrationService().getGlobalPropertyObject("clinic.helpDeskNumber").getPropertyValue();
        String clinicTime = Context.getAdministrationService().getGlobalPropertyObject("clinic.clinicTimings").getPropertyValue();
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("location", Context.getUserContext().getLocation().getName());//change to new location
        arguments.put("identifier", patient.getPatientIdentifier().getIdentifier());
        arguments.put("patientname", patient.getGivenName() + " " + patient.getFamilyName());
        arguments.put("gender", patient.getGender());
        arguments.put("age", patient.getAge().toString());
        arguments.put("helpdesknumber", helpdeskNumber);
        arguments.put("facilitytimings", clinicTime);
        return arguments;
    }
	
	public Map<String, Object> createArgumentsMapForRecurringAppointmentBooking(Appointment appointment) {
		Map<String, Object> arguments = createArgumentsMapForAppointmentBooking(appointment);
		arguments.put("recurringperiod",appointment.getAppointmentRecurringPattern().getPeriod());
		arguments.put("recurringtype",appointment.getAppointmentRecurringPattern().getType());
		arguments.put("recurringdays",appointment.getAppointmentRecurringPattern().getDaysOfWeek());
		arguments.put("recurringfrequency",appointment.getAppointmentRecurringPattern().getFrequency());

		return arguments;
	}
	
	public Map<String, Object> createArgumentsMapForAppointmentBooking(Appointment appointment) {

		Map<String, Object> arguments = new HashMap<>();
		Patient patient = appointment.getPatient();
		String givenName = patient.getGivenName();
		String familyName = patient.getFamilyName();
		String identifier = patient.getPatientIdentifier().getIdentifier();
		Date appointmentDate = appointment.getStartDateTime();
		String service = appointment.getService().getName();
		String teleLink=appointment.getTeleHealthVideoLink();
		String smsTimeZone = Context.getMessageSourceService().getMessage(Context.getAdministrationService().getGlobalProperty("bahmni.sms.timezone"), null, new Locale("en"));
		String smsDateFormat = Context.getMessageSourceService().getMessage(Context.getAdministrationService().getGlobalProperty("bahmni.sms.dateformat"), null, new Locale("en"));
		String date = convertUTCToGivenFormat(appointmentDate, smsDateFormat, smsTimeZone);
		String helpdeskNumber = Context.getAdministrationService().getGlobalPropertyObject("clinic.helpDeskNumber").getPropertyValue();
		String facilityName = getFacilityName(appointment.getLocation());

		arguments.put("patientname", givenName + " " + familyName);
		arguments.put("identifier", identifier);
		arguments.put("gender", appointment.getPatient().getGender());
		arguments.put("date", date);
		arguments.put("service", service);
		arguments.put("facilityname", facilityName);
		arguments.put("teleconsultationlink", teleLink);
		arguments.put("helpdesknumber", helpdeskNumber);
		return arguments;
    }
	
	public String getFacilityName(Location location) {
		LocationTag visitLocationTag = Context.getLocationService().getLocationTagByName("Visit Location");
		List<Location> locations = Context.getLocationService().getLocationsHavingAnyTag(
		    Collections.singletonList(visitLocationTag));
		String facilityName = (visitLocationTag != null && !locations.isEmpty()) ? locations.get(0).getName() : "xxxxx";
		
		if (location != null) {
			String facilityNameFromVisitLocation = Context.getService(AppointmentVisitLocation.class).getFacilityName(location.getUuid());
			
			if (StringUtils.isNotEmpty(facilityNameFromVisitLocation)) {
				facilityName = facilityNameFromVisitLocation;
			}
		}
		return facilityName;
	}
	
	public String templateMessage(String smsTemplate, Map<String, Object> arguments) {
		String template = Context.getAdministrationService().getGlobalProperty(smsTemplate);
		String formattedMessage = StringUtils.isBlank(template) ? Context.getMessageSourceService().getMessage(smsTemplate,
		    null, new Locale("en")) : template;
		
		Pattern pattern = Pattern.compile("\\{([^}]*)\\}");
		Matcher matcher = pattern.matcher(formattedMessage);
		while (matcher.find()) {
			String placeholder = matcher.group(1);
			String modifiedPlaceholder = placeholder.toLowerCase().replaceAll("\\s", "");
			Object value = arguments.get(modifiedPlaceholder);
			placeholder = String.format("{%s}", placeholder);
			formattedMessage = formattedMessage.replace(placeholder, String.valueOf(value));
		}
		
		return formattedMessage.replace("\\n", System.lineSeparator());
	}
	
}
