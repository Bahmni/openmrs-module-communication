package org.bahmni.module.communication.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class MessageBuilderImplTest {

    @InjectMocks
    MessageBuilderServiceImpl messageBuilderService;

    @Mock
    SMSTemplateService smsTemplateService;

    @Test
    public void testGetRegistrationMessage() {
        Map<String, String> placeholderValues = Collections.singletonMap("key", "value");
        String expectedTemplate = "Registration SMS Template";

        when(smsTemplateService.message(MessageBuilderServiceImpl.PATIENT_REGISTRATION_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn(expectedTemplate);

        String result = messageBuilderService.getRegistrationMessage(placeholderValues);

        assertEquals(expectedTemplate, result);
    }

    @Test
    public void testGetAppointmentBookingMessage() {
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("key", "value");
        placeholderValues.put("appointmentKind", "Virtual");
        List<String> providers = Collections.singletonList("Provider1");
        String expectedTemplate = "Appointment Booking SMS Template";

        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_BOOKING_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn(expectedTemplate);

        String result = messageBuilderService.getAppointmentBookingMessage(placeholderValues, providers);
        String expectedResult = expectedTemplate + "nullnullnull";
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetRecurringAppointmentBookingMessage() {
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("key", "value");
        placeholderValues.put("appointmentKind", "Virtual");
        List<String> providers = Collections.singletonList("Provider1");
        String expectedTemplate = "Recurring Appointment Booking SMS Template";

        when(smsTemplateService.message(MessageBuilderServiceImpl.RECURRING_APPOINTMENT_BOOKING_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn(expectedTemplate);

        String result = messageBuilderService.getRecurringAppointmentBookingMessage(placeholderValues, providers);

        String expectedResult = expectedTemplate + "nullnullnull";
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetAppointmentReminderMessage() {
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("key", "value");
        placeholderValues.put("appointmentKind", "Virtual");
        List<String> providers = Collections.singletonList("Provider1");
        String expectedTemplate = "Appointment Reminder SMS Template";

        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_REMINDER_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn(expectedTemplate);

        String result = messageBuilderService.getAppointmentReminderMessage(placeholderValues, providers);

        String expectedResult = expectedTemplate + "nullnullnull";
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGenerateAppointmentMessage() {
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("key", "value");
        placeholderValues.put("appointmentKind", "Virtual");
        List<String> providers = Collections.singletonList("Provider1");
        String smsTemplateKey = "sms.appointmentTemplateKey";
        String expectedMessage = "Generated Appointment SMS Template";

        when(smsTemplateService.message(smsTemplateKey, placeholderValues)).thenReturn(expectedMessage);
        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_PROVIDER_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Provider Template");
        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_TELECONSULTATION_LINK_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Teleconsultation Link Template");
        when(smsTemplateService.message(MessageBuilderServiceImpl.HELPDESK_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Helpdesk Template");

        String result = messageBuilderService.generateAppointmentMessage(placeholderValues, providers, smsTemplateKey);

        assertEquals("Generated Appointment SMS TemplateProvider TemplateTeleconsultation Link TemplateHelpdesk Template", result);
    }

    @Test
    public void testGenerateAppointmentMessageWhenProviderIsEmpty() {
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("key", "value");
        placeholderValues.put("appointmentKind", "Virtual");
        List<String> providers = new ArrayList<>();
        String smsTemplateKey = "sms.appointmentTemplateKey";
        String expectedMessage = "Generated Appointment SMS Template";

        when(smsTemplateService.message(smsTemplateKey, placeholderValues)).thenReturn(expectedMessage);
        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_TELECONSULTATION_LINK_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Teleconsultation Link Template");
        when(smsTemplateService.message(MessageBuilderServiceImpl.HELPDESK_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Helpdesk Template");

        String result = messageBuilderService.generateAppointmentMessage(placeholderValues, providers, smsTemplateKey);

        assertEquals("Generated Appointment SMS TemplateTeleconsultation Link TemplateHelpdesk Template", result);
    }

    @Test
    public void testGenerateAppointmentMessageWhenAppointmentKindIsNotVirtual() {
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("key", "value");
        placeholderValues.put("appointmentKind", "InPerson");
        List<String> providers = Collections.singletonList("Provider1");
        String smsTemplateKey = "sms.appointmentTemplateKey";
        String expectedMessage = "Generated Appointment SMS Template";

        when(smsTemplateService.message(smsTemplateKey, placeholderValues)).thenReturn(expectedMessage);
        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_PROVIDER_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Provider Template");
        when(smsTemplateService.message(MessageBuilderServiceImpl.HELPDESK_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Helpdesk Template");

        String result = messageBuilderService.generateAppointmentMessage(placeholderValues, providers, smsTemplateKey);

        assertEquals("Generated Appointment SMS TemplateProvider TemplateHelpdesk Template", result);
    }
}
