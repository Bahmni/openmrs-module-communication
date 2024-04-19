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
    public void shouldGenerateRegistrationMessage() {
        Map<String, String> placeholderValues = Collections.singletonMap("key", "value");
        String expectedMessage = "Test Registration Message";
        when(smsTemplateService.message(MessageBuilderServiceImpl.PATIENT_REGISTRATION_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn(expectedMessage);

        String result = messageBuilderService.getRegistrationMessage(placeholderValues);

        assertEquals(expectedMessage, result);
    }

    @Test
    public void shouldGenerateAppointmentBookingMessage() {
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("appointmentKind", "Virtual");
        List<String> providers = Collections.singletonList("Provider1");

        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_BOOKING_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Your appointment is booked.");
        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_PROVIDER_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Appointment is booked with Provider1.");
        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_TELECONSULTATION_LINK_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Click on this link to join the consultation.");
        when(smsTemplateService.message(MessageBuilderServiceImpl.HELPDESK_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("For any queries contact us.");

        String result = messageBuilderService.getAppointmentBookingMessage(placeholderValues, providers);

        assertEquals("Your appointment is booked.Appointment is booked with Provider1.Click on this link to join the consultation." +
                "For any queries contact us.", result);
    }

    @Test
    public void shouldGenerateRecurringAppointmentBookingMessage() {
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("appointmentKind", "Virtual");
        List<String> providers = Collections.singletonList("Provider1");

        when(smsTemplateService.message(MessageBuilderServiceImpl.RECURRING_APPOINTMENT_BOOKING_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Your recurring appointment is booked.");
        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_PROVIDER_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Appointment is booked with Provider1.");
        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_TELECONSULTATION_LINK_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Click on this link to join the consultation.");
        when(smsTemplateService.message(MessageBuilderServiceImpl.HELPDESK_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("For any queries contact us.");

        String result = messageBuilderService.getRecurringAppointmentBookingMessage(placeholderValues, providers);

        assertEquals("Your recurring appointment is booked.Appointment is booked with Provider1.Click on this link to join the consultation." +
                "For any queries contact us.", result);
    }

    @Test
    public void shouldGenerateAppointmentReminderMessage() {
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("appointmentKind", "Virtual");
        List<String> providers = Collections.singletonList("Provider1");

        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_REMINDER_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Reminder for your appointment.");
        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_PROVIDER_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Appointment is booked with Provider1.");
        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_TELECONSULTATION_LINK_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Click on this link to join the consultation.");
        when(smsTemplateService.message(MessageBuilderServiceImpl.HELPDESK_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("For any queries contact us.");

        String result = messageBuilderService.getAppointmentReminderMessage(placeholderValues, providers);

        assertEquals("Reminder for your appointment.Appointment is booked with Provider1.Click on this link to join the consultation." +
                "For any queries contact us.", result);
    }

    @Test
    public void shouldNotIncludeProviderInformationInAppointmentMessageWhenProviderIsEmpty() {
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("appointmentKind", "Virtual");
        List<String> providers = new ArrayList<>();
        String smsTemplateKey = "sms.appointmentTemplateKey";

        when(smsTemplateService.message(smsTemplateKey, placeholderValues)).thenReturn("Your appointment is booked.");
        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_TELECONSULTATION_LINK_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Click on this link to join the consultation.");
        when(smsTemplateService.message(MessageBuilderServiceImpl.HELPDESK_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("For any queries contact us.");

        String result = messageBuilderService.generateAppointmentMessage(placeholderValues, providers, smsTemplateKey);

        assertEquals("Your appointment is booked.Click on this link to join the consultation.For any queries contact us.", result);
    }

    @Test
    public void shouldNotIncludeTeleconsultationLinkInAppointmentMessageWhenAppointmentKindIsNotVirtual() {
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("appointmentKind", "InPerson");
        List<String> providers = Collections.singletonList("Provider1");
        String smsTemplateKey = "sms.appointmentTemplateKey";

        when(smsTemplateService.message(smsTemplateKey, placeholderValues)).thenReturn("Your appointment is booked.");
        when(smsTemplateService.message(MessageBuilderServiceImpl.APPOINTMENT_PROVIDER_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("Appointment is booked with Provider1.");
        when(smsTemplateService.message(MessageBuilderServiceImpl.HELPDESK_SMS_TEMPLATE_KEY, placeholderValues))
                .thenReturn("For any queries contact us.");

        String result = messageBuilderService.generateAppointmentMessage(placeholderValues, providers, smsTemplateKey);

        assertEquals("Your appointment is booked.Appointment is booked with Provider1.For any queries contact us.", result);
    }
}
