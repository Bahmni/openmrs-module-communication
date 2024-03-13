package org.bahmni.module.communication.service;

import org.bahmni.module.communication.service.impl.MessageBuilderServiceImpl;
import org.bahmni.module.communication.service.impl.SMSTemplateService;
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
        Map<String, String> arguments = Collections.singletonMap("key", "value");
        String expectedTemplate = "Registration SMS Template";

        when(smsTemplateService.templateMessage(MessageBuilderServiceImpl.PATIENT_REGISTRATION_SMS_TEMPLATE, arguments))
                .thenReturn(expectedTemplate);

        String result = messageBuilderService.getRegistrationMessage(arguments);

        assertEquals(expectedTemplate, result);
    }

    @Test
    public void testGetAppointmentBookingMessage() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("key", "value");
        arguments.put("appointmentKind", "Virtual");
        List<String> providers = Collections.singletonList("Provider1");
        String expectedTemplate = "Appointment Booking SMS Template";

        when(smsTemplateService.templateMessage(MessageBuilderServiceImpl.APPOINTMENT_BOOKING_SMS_TEMPLATE, arguments))
                .thenReturn(expectedTemplate);

        String result = messageBuilderService.getAppointmentBookingMessage(arguments, providers);
        String expectedResult = expectedTemplate + "nullnullnull";
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetRecurringAppointmentBookingMessage() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("key", "value");
        arguments.put("appointmentKind", "Virtual");
        List<String> providers = Collections.singletonList("Provider1");
        String expectedTemplate = "Recurring Appointment Booking SMS Template";

        when(smsTemplateService.templateMessage(MessageBuilderServiceImpl.RECURRING_APPOINTMENT_BOOKING_SMS_TEMPLATE, arguments))
                .thenReturn(expectedTemplate);

        String result = messageBuilderService.getRecurringAppointmentBookingMessage(arguments, providers);

        String expectedResult = expectedTemplate + "nullnullnull";
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetAppointmentReminderMessage() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("key", "value");
        arguments.put("appointmentKind", "Virtual");
        List<String> providers = Collections.singletonList("Provider1");
        String expectedTemplate = "Appointment Reminder SMS Template";

        when(smsTemplateService.templateMessage(MessageBuilderServiceImpl.APPOINTMENT_REMINDER_SMS_TEMPLATE, arguments))
                .thenReturn(expectedTemplate);

        String result = messageBuilderService.getAppointmentReminderMessage(arguments, providers);

        String expectedResult = expectedTemplate + "nullnullnull";
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGenerateAppointmentMessage() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("key", "value");
        arguments.put("appointmentKind", "Virtual");
        List<String> providers = Collections.singletonList("Provider1");
        String smsTemplate = "Appointment SMS Template";
        String expectedTemplate = "Generated Appointment SMS Template";

        when(smsTemplateService.templateMessage(smsTemplate, arguments)).thenReturn(expectedTemplate);
        when(smsTemplateService.templateMessage(MessageBuilderServiceImpl.APPOINTMENT_PROVIDER_TEMPLATE, arguments))
                .thenReturn("Provider Template");
        when(smsTemplateService.templateMessage(MessageBuilderServiceImpl.APPOINTMENT_TELECONSULTATION_LINK_TEMPLATE, arguments))
                .thenReturn("Teleconsultation Link Template");
        when(smsTemplateService.templateMessage(MessageBuilderServiceImpl.HELPDESK_TEMPLATE, arguments))
                .thenReturn("Helpdesk Template");

        String result = messageBuilderService.generateAppointmentMessage(arguments, providers, smsTemplate);

        assertEquals("Generated Appointment SMS TemplateProvider TemplateTeleconsultation Link TemplateHelpdesk Template", result);
    }

    @Test
    public void testGenerateAppointmentMessageWhenProviderIsEmpty() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("key", "value");
        arguments.put("appointmentKind", "Virtual");
        List<String> providers = new ArrayList<>();
        String smsTemplate = "Appointment SMS Template";
        String expectedTemplate = "Generated Appointment SMS Template";

        when(smsTemplateService.templateMessage(smsTemplate, arguments)).thenReturn(expectedTemplate);
        when(smsTemplateService.templateMessage(MessageBuilderServiceImpl.APPOINTMENT_TELECONSULTATION_LINK_TEMPLATE, arguments))
                .thenReturn("Teleconsultation Link Template");
        when(smsTemplateService.templateMessage(MessageBuilderServiceImpl.HELPDESK_TEMPLATE, arguments))
                .thenReturn("Helpdesk Template");

        String result = messageBuilderService.generateAppointmentMessage(arguments, providers, smsTemplate);

        assertEquals("Generated Appointment SMS TemplateTeleconsultation Link TemplateHelpdesk Template", result);
    }

    @Test
    public void testGenerateAppointmentMessageWhenAppointmentKindIsNotVirtual() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("key", "value");
        arguments.put("appointmentKind", "InPerson");
        List<String> providers = Collections.singletonList("Provider1");
        String smsTemplate = "Appointment SMS Template";
        String expectedTemplate = "Generated Appointment SMS Template";

        when(smsTemplateService.templateMessage(smsTemplate, arguments)).thenReturn(expectedTemplate);
        when(smsTemplateService.templateMessage(MessageBuilderServiceImpl.APPOINTMENT_PROVIDER_TEMPLATE, arguments))
                .thenReturn("Provider Template");
        when(smsTemplateService.templateMessage(MessageBuilderServiceImpl.HELPDESK_TEMPLATE, arguments))
                .thenReturn("Helpdesk Template");

        String result = messageBuilderService.generateAppointmentMessage(arguments, providers, smsTemplate);

        assertEquals("Generated Appointment SMS TemplateProvider TemplateHelpdesk Template", result);
    }


    @Test
    public void testGeneratePatientMessage() {
        Map<String, String> arguments = Collections.singletonMap("key", "value");
        String smsTemplate = "Patient SMS Template";
        String expectedTemplate = "Generated Patient SMS Template";

        when(smsTemplateService.templateMessage(smsTemplate, arguments)).thenReturn(expectedTemplate);

        String result = messageBuilderService.generatePatientMessage(arguments, smsTemplate);

        assertEquals(expectedTemplate, result);
    }
}
