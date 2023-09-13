package org.bahmni.module.communication.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bahmni.module.communication.model.SMSRequest;
//import org.bahmni.module.communication.properties.CommunicationProperties;
import org.bahmni.module.communication.service.SMSSenderService;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentProviderDetail;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SMSSenderServiceImpl implements SMSSenderService {

    @Autowired
    SMSTemplateService smsTemplateService;
    private Log log = LogFactory.getLog(this.getClass());

    private final static String SMS_URI = "bahmni.sms.url";
    private final static String TOKEN_KEY = "token1";
    public static final String PATIENT_REGISTRATION_SMS_TEMPLATE = "sms.registrationSMSTemplate";

    public static final String APPOINTMENT_PROVIDER_TEMPLATE = "sms.providersTemplate";

    public static final String APPOINTMENT_TELECONSULTATION_LINK_TEMPLATE = "sms.teleconsultationLinkTemplate";

    public static final String RECURRING_APPOINTMENT_BOOKING_SMS_TEMPLATE = "sms.recurringAppointmentTemplate";

    private final static String APPOINTMENT_BOOKING_SMS_TEMPLATE = "sms.appointmentBookingSMSTemplate";

    public static final String HELPDESK_TEMPLATE = "sms.helpdeskTemplate";

    private final Log logger = LogFactory.getLog(this.getClass());

    @Override
    public String getRegistrationMessage(Patient patient) {
        return generatePatientMessage(patient, PATIENT_REGISTRATION_SMS_TEMPLATE);
    }

    public String getAppointmentBookingMessage(Appointment appointment) {
        String template = appointment.isRecurring() ? RECURRING_APPOINTMENT_BOOKING_SMS_TEMPLATE
                : APPOINTMENT_BOOKING_SMS_TEMPLATE;
        return generateAppointmentMessage(appointment, template);
    }
    public String getAppointmentReminderMessage(Appointment appointment) {
        return generateAppointmentMessage(appointment, APPOINTMENT_BOOKING_SMS_TEMPLATE);
    }
    public String generateAppointmentMessage(Appointment appointment, String smsTemplate) {
        Map<String, Object> arguments;
        if (appointment.isRecurring()){
            arguments = smsTemplateService.createArgumentsMapForRecurringAppointmentBooking(appointment) ;
        }
        else
            arguments=smsTemplateService.createArgumentsMapForAppointmentBooking(appointment);
        List<AppointmentProvider> appointmentProviderList = new ArrayList<>();
        if (appointment.getProviders() != null) {
            appointmentProviderList.addAll(appointment.getProviders());
        }
        List<String> providers = new ArrayList<>();
        for (AppointmentProvider appointmentProvider : appointmentProviderList) {
            providers.add(appointmentProvider.getProvider().getName());
        }
        String smsTemplateMessage = smsTemplateService.templateMessage(smsTemplate, arguments);
        if (!providers.isEmpty()) {
            arguments.put("providername", org.springframework.util.StringUtils.collectionToCommaDelimitedString(providers));
            smsTemplateMessage += smsTemplateService.templateMessage(APPOINTMENT_PROVIDER_TEMPLATE, arguments);
        }
        if (appointment.getAppointmentKind().getValue().equals("Virtual")) {
            smsTemplateMessage += smsTemplateService.templateMessage(APPOINTMENT_TELECONSULTATION_LINK_TEMPLATE, arguments);
        }
        String helpdeskTemplate = smsTemplateService.templateMessage(HELPDESK_TEMPLATE, arguments);
        return smsTemplateMessage + helpdeskTemplate;
    }

    public String generatePatientMessage(Patient patient, String smsTemplate) {
        Map<String, Object> arguments = smsTemplateService.createArgumentsMapForPatientRegistration(patient);
        return smsTemplateService.templateMessage(smsTemplate, arguments);
    }

    @Override
    public String sendSMS(String phoneNumber, String message) {
        try {
            SMSRequest smsRequest = new SMSRequest();
            smsRequest.setPhoneNumber(phoneNumber);
            smsRequest.setMessage(message);
            ObjectMapper objMapper = new ObjectMapper();
            String jsonObject = objMapper.writeValueAsString(smsRequest);
            StringEntity params = new StringEntity(jsonObject);
            String smsUrl = Context.getAdministrationService().getGlobalProperty("bahmni.sms.url", SMS_URI);
            HttpPost request = new HttpPost(Context.getMessageSourceService().getMessage(smsUrl, null, new Locale("en")));
            request.addHeader("content-type", "application/json");
            String tokenFilePath = new File(OpenmrsUtil.getApplicationDataDirectory() + "/sms-tokens", "sms-tokens.txt")
                    .getAbsolutePath();
            String token1 = getTokenFromTokenFile(tokenFilePath, TOKEN_KEY);

            if (token1 == null) {
                throw new RuntimeException("Token 'token1' not found in the token file: " + tokenFilePath);
            }

            request.addHeader("Authorization", "Bearer " + token1);
            request.setEntity(params);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(request);
            httpClient.close();
            return response.getStatusLine().getReasonPhrase();
        } catch (Exception e) {
            logger.error("Exception occurred in sending SMS ", e);
            throw new RuntimeException("Exception occurred in sending SMS ", e);
        }
    }

    private String getTokenFromTokenFile(String tokenFilePath, String tokenKey) {
        try (BufferedReader reader = new BufferedReader(new FileReader(tokenFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(tokenKey + "=")) {
                    return line.substring(tokenKey.length() + 1).trim();
                }
            }
        } catch (IOException e) {
            logger.error("Error loading token file: " + tokenFilePath, e);
            throw new RuntimeException("Error loading token file: " + tokenFilePath, e);
        }
        return null;
    }
}
