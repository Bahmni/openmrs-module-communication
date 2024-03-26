package org.bahmni.module.communication.service.impl;

import org.bahmni.module.communication.service.MessageBuilderService;

import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.collectionToCommaDelimitedString;

public class MessageBuilderServiceImpl implements MessageBuilderService {
	
	SMSTemplateService smsTemplateService;
	
	public static final String PATIENT_REGISTRATION_SMS_TEMPLATE_KEY = "sms.registrationSMSTemplate";
	
	public static final String APPOINTMENT_PROVIDER_SMS_TEMPLATE_KEY = "sms.providersTemplate";
	
	public static final String APPOINTMENT_TELECONSULTATION_LINK_SMS_TEMPLATE_KEY = "sms.teleconsultationLinkTemplate";
	
	public static final String RECURRING_APPOINTMENT_BOOKING_SMS_TEMPLATE_KEY = "sms.recurringAppointmentTemplate";
	
	public final static String APPOINTMENT_BOOKING_SMS_TEMPLATE_KEY = "sms.appointmentBookingSMSTemplate";
	
	public final static String APPOINTMENT_REMINDER_SMS_TEMPLATE_KEY = "sms.appointmentReminderSMSTemplate";
	
	public static final String HELPDESK_SMS_TEMPLATE_KEY = "sms.helpdeskTemplate";
	
	public void setSmsTemplateService(SMSTemplateService smsTemplateService) {
		this.smsTemplateService = smsTemplateService;
	}
	
	@Override
	public String getRegistrationMessage(Map<String, String> placeholderValues) {
		return smsTemplateService.message(PATIENT_REGISTRATION_SMS_TEMPLATE_KEY, placeholderValues);
	}
	
	@Override
	public String getAppointmentBookingMessage(Map<String, String> placeholderValues, List<String> providers) {
		return generateAppointmentMessage(placeholderValues, providers, APPOINTMENT_BOOKING_SMS_TEMPLATE_KEY);
	}
	
	@Override
	public String getRecurringAppointmentBookingMessage(Map<String, String> placeholderValues, List<String> providers) {
		return generateAppointmentMessage(placeholderValues, providers, RECURRING_APPOINTMENT_BOOKING_SMS_TEMPLATE_KEY);
	}
	
	@Override
	public String getAppointmentReminderMessage(Map<String, String> placeholderValues, List<String> providers) {
		return generateAppointmentMessage(placeholderValues, providers, APPOINTMENT_REMINDER_SMS_TEMPLATE_KEY);
	}
	
	public String generateAppointmentMessage(Map<String, String> placeholderValues, List<String> providers, String smsTemplateKey) {
		String smsTemplateMessage = smsTemplateService.message(smsTemplateKey, placeholderValues);
		if (!providers.isEmpty()) {
			placeholderValues.put("providername", collectionToCommaDelimitedString(providers));
			smsTemplateMessage += smsTemplateService.message(APPOINTMENT_PROVIDER_SMS_TEMPLATE_KEY, placeholderValues);
		}
		if (placeholderValues.get("appointmentKind").equals("Virtual")) {
			smsTemplateMessage += smsTemplateService.message(APPOINTMENT_TELECONSULTATION_LINK_SMS_TEMPLATE_KEY, placeholderValues);
		}
		String helpdeskTemplate = smsTemplateService.message(HELPDESK_SMS_TEMPLATE_KEY, placeholderValues);
		return smsTemplateMessage + helpdeskTemplate;
	}
}
