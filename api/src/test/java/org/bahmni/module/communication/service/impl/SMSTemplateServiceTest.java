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
        String templateKey = "template.key";
        String template = "Hello {name}, Your appointment is on {date}.";
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("name", "John");
        placeholderValues.put("date", "2024-03-15");
        when(administrationService.getGlobalProperty(templateKey)).thenReturn(template);

        String message = smsTemplateService.message(templateKey, placeholderValues);

        assertEquals("Hello John, Your appointment is on 2024-03-15.", message);
    }

    @Test
    public void shouldReturnFormattedMessageWhenGlobalPropertyIsNotSet() {
        String templateKey = "template.key";
        String template = "Hello {name}, Your appointment is on {date}.";
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("name", "John");
        placeholderValues.put("date", "2024-03-15");
        when(administrationService.getGlobalProperty(templateKey)).thenReturn("");
        when(messageSourceService.getMessage(templateKey, null, new Locale("en"))).thenReturn(template);

        String message = smsTemplateService.message(templateKey, placeholderValues);

        assertEquals("Hello John, Your appointment is on 2024-03-15.", message);
    }

    @Test
    public void shouldHandlePlaceholderWithSpacesAndUpperCase() {
        String templateKey = "template.key";
        String template = "Hello {name}, Your appointment is on {PRESENT DATE}.";
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("name", "John");
        placeholderValues.put("presentdate", "2024-03-15");
        when(administrationService.getGlobalProperty(templateKey)).thenReturn(template);

        String message = smsTemplateService.message(templateKey, placeholderValues);

        assertEquals("Hello John, Your appointment is on 2024-03-15.", message);
    }

    @Test
    public void shouldReplaceNewlineWithSystemSpecificLineSeparator() {
        String templateKey = "template.key";
        String template = "Hello {name},\nYour appointment is on {date}.";
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("name", "John");
        placeholderValues.put("date", "2024-03-15");
        when(administrationService.getGlobalProperty(templateKey)).thenReturn(template);

        String message = smsTemplateService.message(templateKey, placeholderValues);

        assertEquals("Hello John," + System.lineSeparator() + "Your appointment is on 2024-03-15.", message);
    }
}