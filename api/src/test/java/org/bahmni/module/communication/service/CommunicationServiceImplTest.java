package org.bahmni.module.communication.service;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.bahmni.module.communication.model.SMSRequest;
import org.bahmni.module.communication.service.impl.CommunicationServiceImpl;
import org.bahmni.module.communication.model.MailContent;
import org.bahmni.module.communication.model.MailAttachment;
import org.bahmni.module.communication.model.Recipient;
import org.bahmni.module.communication.service.impl.MessagingUtility;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Properties;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class, Session.class, OpenmrsUtil.class })
public class CommunicationServiceImplTest {
	
	@Mock
	private AdministrationService administrationService;
	
	@InjectMocks
	CommunicationServiceImpl communicationService;
	
	@Mock
	private MessageSourceService messageSourceService;
	
	@Mock
	private MessagingUtility messagingUtility;
	
	@Mock
	Session session;
	
	@Mock
	MimeMessage mail;
	
	@Mock
	private Transport mailTransport;
	
	@Before
	public void init() throws NoSuchProviderException {
		MockitoAnnotations.initMocks(this);
		PowerMockito.mockStatic(Context.class);
		PowerMockito.mockStatic(Session.class);
		when(Context.getAdministrationService()).thenReturn(administrationService);
		when(Context.getMessageSourceService()).thenReturn(messageSourceService);
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
		when(mail.getSession()).thenReturn(session);
		when(session.getProperty("mail.from")).thenReturn("noreply@bahmni.org");
		when(mail.getSession().getProperty("mail.send")).thenReturn("true");
		PowerMockito.mockStatic(OpenmrsUtil.class);
	}
	
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	
	@Test
	public void shouldReadFromGlobalPropertyAndSendDummyMail() throws IOException {
		Path tempDir = Files.createTempDirectory(null);
		OpenmrsUtil.setApplicationDataDirectory(tempDir.toString());
		MailAttachment mailAttachment = new MailAttachment("text/html", "Hello", "welcome", null);
		Recipient recipient = new Recipient("abc", "noreply2@bahmni.org");
		MailContent mailContent = new MailContent("Test", "Test", recipient, null, null, Arrays.asList(mailAttachment));
		communicationService.sendEmail(mailContent);
		OpenmrsUtil.setApplicationDataDirectory("");
		FileUtils.deleteDirectory(tempDir.toFile());
	}
	
	@Test
	public void shouldReadFromMailConfigPropertiesAndSendDummyEmail() throws IOException {
		String TMP_FOLDER = "/tmp";
		File file = new File(TMP_FOLDER + "/mail-config.properties");
		FileUtils.writeStringToFile(file, "mail.send=true\n" + "mail.transport_protocol=smtp\n"
		        + "mail.smtp_host=localhost\n" + "mail.smtp_port=25\n" + "mail.smtp_auth=false\n"
		        + "mail.smtp.starttls.enable=true\n" + "mail.from=dummy@bahmni.org\n" + "mail.user=test\n"
		        + "mail.password=random\n");
		OpenmrsUtil.setApplicationDataDirectory(TMP_FOLDER);
		verify(administrationService, times(0)).getGlobalProperty("mail.smtp_host");
		
		MailAttachment mailAttachment = new MailAttachment("text/plain", "welcome.txt", String.valueOf(Base64.getEncoder()
		        .encodeToString("Hello".getBytes())), null);
		Recipient recipient = new Recipient("abc", "dummy@bahmni.org");
		MailContent mailContent = new MailContent("Test", "Test", recipient, new String[] { "dummy@bahmni.org" },
		        new String[] { "dummy@bahmni.org" }, Arrays.asList(mailAttachment));
		communicationService.sendEmail(mailContent);
		FileUtils.forceMkdirParent(file);
	}
	
	@Test
	public void shouldSendDummySMS() throws IOException, NoSuchAlgorithmException {
		String phoneNumber = "1234567890";
		String message = "This is a test message";
		SMSRequest expectedSmsRequest = new SMSRequest();
		expectedSmsRequest.setPhoneNumber(phoneNumber);
		expectedSmsRequest.setMessage(message);
		String SMS_TOKEN_KEY = "communications";
		String tokenFilePath = new File(OpenmrsUtil.getApplicationDataDirectory() + "/sms-tokens", SMS_TOKEN_KEY
		        + "-token.txt").getAbsolutePath();
		String expectedToken = "expected_token";
		
		when(messagingUtility.getSMSTokenFromTokenFile(tokenFilePath)).thenReturn(expectedToken);
		String SMS_URI = "bahmni.sms.url";
		String smsUrl = "http://localhost:25/sms";
		when(administrationService.getGlobalProperty("bahmni.sms.url")).thenReturn(smsUrl);
		when(Context.getMessageSourceService().getMessage(smsUrl, null, new Locale("en"))).thenReturn(smsUrl);
		CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
		CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
		HttpPost httpPost = mock(HttpPost.class);
		when(httpClient.execute(eq(httpPost))).thenReturn(httpResponse);
		when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null));
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("Exception occurred in sending SMS");
		communicationService.sendSMS(phoneNumber, message);
	}
	
}
