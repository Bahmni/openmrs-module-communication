package org.bahmni.module.communication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bahmni.module.communication.model.MailAttachment;
import org.bahmni.module.communication.model.MailContent;
import org.bahmni.module.communication.model.Recipient;
import org.bahmni.module.communication.model.SMSRequest;
import org.bahmni.module.communication.service.impl.CommunicationServiceImpl;
import org.bahmni.module.communication.service.impl.MessagingUtility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class, Session.class, OpenmrsUtil.class, HttpClients.class})
public class CommunicationServiceImplTest {

    @Mock
    private AdministrationService administrationService;

    @Mock
    private MessageSourceService messageSourceService;

    @Mock
    private MessagingUtility messagingUtility;

    @Mock
    Session session;

    @Mock
    private Transport mailTransport;

    @Mock
    private CloseableHttpClient httpClient;

    @InjectMocks
    CommunicationServiceImpl communicationService;

    @Before
    public void init() throws NoSuchProviderException {
        PowerMockito.mockStatic(Context.class);
        PowerMockito.mockStatic(Session.class);
        PowerMockito.mockStatic(HttpClients.class);
        PowerMockito.mockStatic(OpenmrsUtil.class);

        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(Context.getMessageSourceService()).thenReturn(messageSourceService);
        when(HttpClients.createDefault()).thenReturn(httpClient);
        when(messagingUtility.getSession()).thenReturn(session);
        when(session.getTransport()).thenReturn(mailTransport);
        Properties p = new Properties();
        p.put("mail.transport.protocol", "smtp");
        p.put("mail.smtp.host", "localhost");
        p.put("mail.smtp.port", "25");
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.debug", "false");
        p.put("mail.from", "noreply@bahmni.org");
        p.put("mail.user", "test");
        p.put("mail.password", "random");
        p.put("mail.send", "true");
        when(session.getProperties()).thenReturn(p);
        when(session.getProperty(anyString())).thenAnswer(prop -> p.getProperty(prop.getArguments()[0].toString()));
    }

    @Test
    public void shouldAbortRequestIfMailSendPropertyIsFalse() throws Exception {
        when(session.getProperty("mail.send")).thenReturn("false");

        communicationService.sendEmail(new MailContent());

        verify(mailTransport, never()).connect();
    }

    @Test
    public void shouldSendEmail() throws Exception {
        MailAttachment mailAttachment = new MailAttachment("text/plain", "welcome.txt", String.valueOf(Base64.getEncoder()
                .encodeToString("Hello".getBytes())), null);
        Recipient recipient = new Recipient("abc", "dummy@bahmni.org");
        MailContent mailContent = new MailContent("Test Subject", "Test Body", recipient, new String[]{"dummy_cc@bahmni.org"},
                new String[]{"dummy_bcc@bahmni.org"}, Collections.singletonList(mailAttachment));
        when(messagingUtility.getAddresses(mailContent.getCc())).thenReturn(new Address[]{new InternetAddress("dummy_cc@bahmni.org")});
        when(messagingUtility.getAddresses(mailContent.getBcc())).thenReturn(new Address[]{new InternetAddress("dummy_bcc@bahmni.org")});

        communicationService.sendEmail(mailContent);

        verify(mailTransport).connect("localhost", "test", "random");
        ArgumentCaptor<MimeMessage> mimeMessageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailTransport).sendMessage(mimeMessageCaptor.capture(), any());
        verify(mailTransport).close();
        MimeMessage capturedMessage = mimeMessageCaptor.getValue();
        assertEquals("Test Subject", capturedMessage.getSubject());
        assertEquals("Test Body", ((MimeMultipart) capturedMessage.getContent()).getBodyPart(0).getContent());
        assertEquals("dummy@bahmni.org", ((InternetAddress) capturedMessage.getRecipients(javax.mail.Message.RecipientType.TO)[0]).getAddress());
        assertEquals("dummy_cc@bahmni.org", ((InternetAddress) capturedMessage.getRecipients(javax.mail.Message.RecipientType.CC)[0]).getAddress());
        assertEquals("dummy_bcc@bahmni.org", ((InternetAddress) capturedMessage.getRecipients(javax.mail.Message.RecipientType.BCC)[0]).getAddress());
        assertNotNull(capturedMessage.getSentDate());
        ByteArrayOutputStream attachmentDataStream = new ByteArrayOutputStream();
        BodyPart attachment = ((MimeMultipart) capturedMessage.getContent()).getBodyPart(1);
        attachment.getDataHandler().writeTo(attachmentDataStream);
        assertEquals("welcome.txt", attachment.getFileName());
        assertEquals("text/plain", attachment.getContentType());
        assertEquals("Hello", attachmentDataStream.toString());
    }

    @Test
    public void shouldSkipAttachmentIfDataIsNotPresent() throws Exception {
        MailAttachment mailAttachment = new MailAttachment("text/plain", "welcome.txt", null, null);
        Recipient recipient = new Recipient("abc", "dummy@bahmni.org");
        MailContent mailContent = new MailContent("Test Subject", "Test Body", recipient, new String[]{"dummy_cc@bahmni.org"},
                new String[]{"dummy_bcc@bahmni.org"}, Collections.singletonList(mailAttachment));
        when(messagingUtility.getAddresses(mailContent.getCc())).thenReturn(new Address[]{new InternetAddress("dummy_cc@bahmni.org")});
        when(messagingUtility.getAddresses(mailContent.getBcc())).thenReturn(new Address[]{new InternetAddress("dummy_bcc@bahmni.org")});

        communicationService.sendEmail(mailContent);

        ArgumentCaptor<MimeMessage> mimeMessageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailTransport).sendMessage(mimeMessageCaptor.capture(), any());
        int bodyPartCount = ((MimeMultipart) mimeMessageCaptor.getValue().getContent()).getCount();
        assertEquals(bodyPartCount, 1); //only contains email body
    }

    @Test
    public void shouldThrowExceptionWhenItFailsToSendEmail() throws MessagingException {
        doThrow(new MessagingException()).when(mailTransport).sendMessage(any(), any());
        assertThrows(RuntimeException.class, () -> communicationService.sendEmail(new MailContent()));
    }

    @Test
    public void shouldSendSMS() throws IOException {
        String phoneNumber = "1234567890";
        String message = "This is a test message";
        SMSRequest expectedSmsRequestObject = new SMSRequest();
        expectedSmsRequestObject.setPhoneNumber(phoneNumber);
        expectedSmsRequestObject.setMessage(message);
        String expectedSmsRequest = new ObjectMapper().writeValueAsString(expectedSmsRequestObject);
        String tokenFilePath = new File(OpenmrsUtil.getApplicationDataDirectory() + "/sms-token/sms-communications-token.txt").getAbsolutePath();
        String expectedToken = "expected_token";
        String smsUrl = "http://localhost:25/sms";

        when(messagingUtility.getSMSTokenFromTokenFile(tokenFilePath)).thenReturn(expectedToken);
        when(administrationService.getGlobalProperty("bahmni.sms.url")).thenReturn(smsUrl);
        when(messageSourceService.getMessage(smsUrl, null, new Locale("en"))).thenReturn(smsUrl);

        communicationService.sendSMS(phoneNumber, message);

        ArgumentCaptor<HttpPost> captorRequest = ArgumentCaptor.forClass(HttpPost.class);
        verify(httpClient).execute(captorRequest.capture());
        verify(httpClient).close();
        HttpPost actualRequest = captorRequest.getValue();
        assertEquals(smsUrl, actualRequest.getURI().toString());
        assertEquals("content-type: application/json", actualRequest.getAllHeaders()[0].toString());
        assertEquals("Authorization: Bearer " + expectedToken, actualRequest.getAllHeaders()[1].toString());
        assertEquals(expectedSmsRequest, EntityUtils.toString(actualRequest.getEntity()));
    }

    @Test
    public void shouldAbortRequestWhenSMSUrlIsEmpty() throws IOException {
        when(administrationService.getGlobalProperty("bahmni.sms.url")).thenReturn("");

        communicationService.sendSMS("1234567890", "This is a test message");

        verify(httpClient, never()).execute(any());
    }

    @Test
    public void shouldAbortRequestWhenSMSUrlIsNull() throws IOException {
        when(administrationService.getGlobalProperty("bahmni.sms.url")).thenReturn(null);

        communicationService.sendSMS("1234567890", "This is a test message");

        verify(httpClient, never()).execute(any());
    }

    @Test
    public void shouldThrowExceptionWhenTokenNotFound() {
        String tokenFilePath = new File(OpenmrsUtil.getApplicationDataDirectory() + "/sms-token/sms-communications-token.txt").getAbsolutePath();
        String smsUrl = "http://localhost:25/sms";

        when(messagingUtility.getSMSTokenFromTokenFile(tokenFilePath)).thenReturn(null);
        when(administrationService.getGlobalProperty("bahmni.sms.url")).thenReturn(smsUrl);
        when(messageSourceService.getMessage(smsUrl, null, new Locale("en"))).thenReturn(smsUrl);

        assertThrows(RuntimeException.class, () -> communicationService.sendSMS("1234567890", "This is a test message"));
    }
}
