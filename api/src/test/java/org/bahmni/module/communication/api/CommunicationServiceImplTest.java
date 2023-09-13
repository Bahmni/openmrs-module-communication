package org.bahmni.module.communication.api;

import org.apache.commons.io.FileUtils;
import org.bahmni.module.communication.service.impl.CommunicationServiceImpl;
import org.bahmni.module.communication.model.MailContent;
import org.bahmni.module.communication.model.MailAttachment;
import org.bahmni.module.communication.model.MailContent;
import org.bahmni.module.communication.model.Recipient;
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
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class })
public class CommunicationServiceImplTest {
	
	@Mock
	private AdministrationService administrationService;
	
	@InjectMocks
	CommunicationServiceImpl communicationService;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		PowerMockito.mockStatic(Context.class);
		when(Context.getAdministrationService()).thenReturn(administrationService);
	}
	
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	
	@Test
	public void shouldReadFromGlobalPropertyAndThrowException() throws IOException {
		Path tempDir = Files.createTempDirectory(null);
		OpenmrsUtil.setApplicationDataDirectory(tempDir.toString());
		when(administrationService.getGlobalProperty(eq("mail.transport_protocol"), eq("smtp"))).thenReturn("smtp");
		when(administrationService.getGlobalProperty("mail.smtp_host", "")).thenReturn("localhost");
		when(administrationService.getGlobalProperty("mail.smtp_port", "25")).thenReturn("25");
		when(administrationService.getGlobalProperty("mail.smtp_auth", "false")).thenReturn("true");
		when(administrationService.getGlobalProperty("mail.smtp.starttls.enable", "true")).thenReturn("true");
		when(administrationService.getGlobalProperty("mail.debug", "false")).thenReturn("false");
		when(administrationService.getGlobalProperty("mail.from", "")).thenReturn("noreply@bahmni.org");
		when(administrationService.getGlobalProperty("mail.user", "")).thenReturn("test");
		when(administrationService.getGlobalProperty("mail.password", "")).thenReturn("random");
		
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("Error occurred while sending email");
		expectedEx.expectCause(instanceOf(java.lang.NullPointerException.class));
		MailAttachment mailAttachment = new MailAttachment("text/html", "Hello", "welcome.txt", null);
		Recipient recipient = new Recipient("abc", "");
		MailContent mailContent = new MailContent("Test", "Test", recipient, null, null, Arrays.asList(mailAttachment));
		communicationService.sendEmail(mailContent);
		OpenmrsUtil.setApplicationDataDirectory("");
		FileUtils.deleteDirectory(tempDir.toFile());
	}
	
	@Test
	public void shouldReadFromMailConfigPropertiesAndThrowException() throws IOException {
		String TMP_FOLDER = "/tmp";
		File file = new File(TMP_FOLDER + "/mail-config.properties");
		FileUtils.writeStringToFile(file, "mail.send=true\n" + "mail.transport_protocol=smtp\n"
		        + "mail.smtp_host=localhost\n" + "mail.smtp_port=25\n" + "mail.smtp_auth=false\n"
		        + "mail.smtp.starttls.enable=true\n" + "mail.from=dummy@bahmni.org\n" + "mail.user=test\n"
		        + "mail.password=random\n");
		OpenmrsUtil.setApplicationDataDirectory(TMP_FOLDER);
		
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("Error occurred while sending email");
		expectedEx.expectCause(instanceOf(com.sun.mail.util.MailConnectException.class));
		verify(administrationService, times(0)).getGlobalProperty("mail.smtp_host");
		
		MailAttachment mailAttachment = new MailAttachment("text/plain", "welcome.txt", String.valueOf(Base64.getEncoder()
		        .encodeToString("Hello".getBytes())), null);
		Recipient recipient = new Recipient("abc", "dummy@bahmni.org");
		MailContent mailContent = new MailContent("Test", "Test", recipient, new String[] { "dummy@bahmni.org" },
		        new String[] { "dummy@bahmni.org" }, Arrays.asList(mailAttachment));
		communicationService.sendEmail(mailContent);
		FileUtils.deleteDirectory(file);
	}
	
}
