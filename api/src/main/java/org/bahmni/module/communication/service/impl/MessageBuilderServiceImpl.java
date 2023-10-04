package org.bahmni.module.communication.service.impl;

import org.bahmni.module.communication.service.MessageBuilderService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class MessageBuilderServiceImpl implements MessageBuilderService {
	
	SMSTemplateService smsTemplateService;
	
	public static final String PATIENT_REGISTRATION_SMS_TEMPLATE = "sms.registrationSMSTemplate";
	
	public static final String APPOINTMENT_PROVIDER_TEMPLATE = "sms.providersTemplate";
	
	public static final String APPOINTMENT_TELECONSULTATION_LINK_TEMPLATE = "sms.teleconsultationLinkTemplate";
	
	public static final String RECURRING_APPOINTMENT_BOOKING_SMS_TEMPLATE = "sms.recurringAppointmentTemplate";
	
	public final static String APPOINTMENT_BOOKING_SMS_TEMPLATE = "sms.appointmentBookingSMSTemplate";
	
	public final static String APPOINTMENT_REMINDER_SMS_TEMPLATE = "sms.appointmentReminderSMSTemplate";
	
	public static final String HELPDESK_TEMPLATE = "sms.helpdeskTemplate";
	
	public void setSmsTemplateService(SMSTemplateService smsTemplateService) {
		this.smsTemplateService = smsTemplateService;
	}
	
	@Override
	public String getRegistrationMessage(Map<String, String> arguments) {
		return generatePatientMessage(arguments, PATIENT_REGISTRATION_SMS_TEMPLATE);
	}
	
	@Override
	public String getAppointmentBookingMessage(Map<String, String> arguments, List<String> providers) {
		return generateAppointmentMessage(arguments, providers, APPOINTMENT_BOOKING_SMS_TEMPLATE);
	}
	
	@Override
	public String getRecurringAppointmentBookingMessage(Map<String, String> arguments, List<String> providers) {
		return generateAppointmentMessage(arguments, providers, RECURRING_APPOINTMENT_BOOKING_SMS_TEMPLATE);
	}
	
	@Override
	public String getAppointmentReminderMessage(Map<String, String> arguments, List<String> providers) {
		return generateAppointmentMessage(arguments, providers, APPOINTMENT_REMINDER_SMS_TEMPLATE);
	}
	
	public String generateAppointmentMessage(Map<String, String> arguments, List<String> providers, String smsTemplate) {
		String smsTemplateMessage = smsTemplateService.templateMessage(smsTemplate, arguments);
		if (!providers.isEmpty()) {
			arguments.put("providername", org.springframework.util.StringUtils.collectionToCommaDelimitedString(providers));
			smsTemplateMessage += smsTemplateService.templateMessage(APPOINTMENT_PROVIDER_TEMPLATE, arguments);
		}
		if (arguments.get("appointmentKind").equals("Virtual")) {
			smsTemplateMessage += smsTemplateService.templateMessage(APPOINTMENT_TELECONSULTATION_LINK_TEMPLATE, arguments);
		}
		String helpdeskTemplate = smsTemplateService.templateMessage(HELPDESK_TEMPLATE, arguments);
		return smsTemplateMessage + helpdeskTemplate;
	}
	
	public String generatePatientMessage(Map<String, String> arguments, String smsTemplate) {
		return smsTemplateService.templateMessage(smsTemplate, arguments);
	}
}
