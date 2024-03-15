package org.bahmni.module.communication.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class SMSTemplateServiceTest {

    @Mock
    private AdministrationService administrationService;

    @Mock
    private MessageSourceService messageSourceService;

    @InjectMocks
    CommunicationServiceImpl communicationService;

    private SMSTemplateService smsTemplateService;


    @Before
    public void setUp() {
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(Context.getMessageSourceService()).thenReturn(messageSourceService);
        smsTemplateService = new SMSTemplateService();
    }

    @Test
    public void shouldReturnFormattedMessageWhenGlobalPropertyIsSet() {
        String smsTemplate = "template.key";
        String expectedTemplate = "Hello {name}, Your appointment is on {date}.";
        Map<String, String> arguments = new HashMap<>();
        arguments.put("name", "John");
        arguments.put("date", "2024-03-15");
        when(administrationService.getGlobalProperty(smsTemplate)).thenReturn(expectedTemplate);

        String result = smsTemplateService.templateMessage(smsTemplate, arguments);

        assertEquals("Hello John, Your appointment is on 2024-03-15.", result);
    }

    @Test
    public void shouldReturnFormattedMessageWhenGlobalPropertyIsNotSet() {
        String smsTemplate = "template.key";
        String expectedTemplate = "Hello {name}, Your appointment is on {date}.";
        Map<String, String> arguments = new HashMap<>();
        arguments.put("name", "John");
        arguments.put("date", "2024-03-15");
        when(administrationService.getGlobalProperty(smsTemplate)).thenReturn("");
        when(messageSourceService.getMessage(smsTemplate, null, new Locale("en"))).thenReturn(expectedTemplate);

        String result = smsTemplateService.templateMessage(smsTemplate, arguments);

        assertEquals("Hello John, Your appointment is on 2024-03-15.", result);
    }

    @Test
    public void shouldHandlePlaceholderWithSpacesAndUpperCase() {
        String smsTemplate = "template.key";
        String expectedTemplate = "Hello {name}, Your appointment is on {PRESENT DATE}.";
        Map<String, String> arguments = new HashMap<>();
        arguments.put("name", "John");
        arguments.put("presentdate", "2024-03-15");
        when(administrationService.getGlobalProperty(smsTemplate)).thenReturn(expectedTemplate);

        String result = smsTemplateService.templateMessage(smsTemplate, arguments);

        assertEquals("Hello John, Your appointment is on 2024-03-15.", result);
    }

    @Test
    public void shouldReplaceNewlineWithSystemSpecificLineSeparator() {
        String smsTemplate = "template.key";
        String expectedTemplate = "Hello {name},\nYour appointment is on {date}.";
        Map<String, String> arguments = new HashMap<>();
        arguments.put("name", "John");
        arguments.put("date", "2024-03-15");
        when(administrationService.getGlobalProperty(smsTemplate)).thenReturn(expectedTemplate);

        String result = smsTemplateService.templateMessage(smsTemplate, arguments);

        assertEquals("Hello John," + System.lineSeparator() + "Your appointment is on 2024-03-15.", result);
    }
}