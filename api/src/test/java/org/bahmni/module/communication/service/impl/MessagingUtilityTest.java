package org.bahmni.module.communication.service.impl;


import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class MessagingUtilityTest {

    @Mock
    private AdministrationService administrationService;

    @InjectMocks
    CommunicationServiceImpl communicationService;

    private MessagingUtility messagingUtility;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        messagingUtility = new MessagingUtility();
    }

    @Test
    public void shouldReturnSessionFromMailConfigPropertiesFile() throws IOException {
        final File file = folder.newFile("mail-config.properties");
        FileUtils.writeStringToFile(file, "mail.send=true\n" + "mail.transport.protocol=smtp\n"
                + "mail.smtp.host=localhost\n" + "mail.smtp.port=25\n" + "mail.smtp.auth=false\n"
                + "mail.smtp.starttls.enable=true\n" + "mail.from=dummy@bahmni.org\n", Charset.defaultCharset());
        OpenmrsUtil.setApplicationDataDirectory(file.getParent());

        Session session = messagingUtility.getSession();

        assertEquals("true", session.getProperty("mail.send"));
        assertEquals("smtp", session.getProperty("mail.transport.protocol"));
        assertEquals("localhost", session.getProperty("mail.smtp.host"));
        assertEquals("25", session.getProperty("mail.smtp.port"));
        assertEquals("false", session.getProperty("mail.smtp.auth"));
        assertEquals("true", session.getProperty("mail.smtp.starttls.enable"));
        assertEquals("dummy@bahmni.org", session.getProperty("mail.from"));
    }

    @Test
    public void shouldReturnSameSessionIfAlreadyInitialized() throws IOException {
        final File file = folder.newFile("mail-config.properties");
        FileUtils.writeStringToFile(file, "mail.send=true", Charset.defaultCharset());
        OpenmrsUtil.setApplicationDataDirectory(file.getParent());

        Session session1 = messagingUtility.getSession();
        Session session2 = messagingUtility.getSession();

        assertSame(session1, session2);
    }

    @Test
    public void shouldReturnSessionUsingOMRSPropertiesWhenMailConfigPropertiesFileDoesNotExist() {
        when(administrationService.getGlobalProperty("mail.transport_protocol", "smtp")).thenReturn("smtp");
        when(administrationService.getGlobalProperty("mail.smtp_host", "")).thenReturn("localhost");
        when(administrationService.getGlobalProperty("mail.smtp_port", "25")).thenReturn("25");
        when(administrationService.getGlobalProperty("mail.smtp_auth", "false")).thenReturn("true");
        when(administrationService.getGlobalProperty("mail.smtp.starttls.enable", "true")).thenReturn("true");
        when(administrationService.getGlobalProperty("mail.smtp.ssl.enable", "true")).thenReturn("true");
        when(administrationService.getGlobalProperty("mail.debug", "false")).thenReturn("false");
        when(administrationService.getGlobalProperty("mail.from", "")).thenReturn("noreply@bahmni.org");
        when(administrationService.getGlobalProperty("mail.user", "")).thenReturn("test_user");
        when(administrationService.getGlobalProperty("mail.password", "")).thenReturn("test_password");
        OpenmrsUtil.setApplicationDataDirectory("/tmp");

        Session session = messagingUtility.getSession();

        assertEquals("smtp", session.getProperty("mail.transport.protocol"));
        assertEquals("localhost", session.getProperty("mail.smtp.host"));
        assertEquals("25", session.getProperty("mail.smtp.port"));
        assertEquals("true", session.getProperty("mail.smtp.auth"));
        assertEquals("true", session.getProperty("mail.smtp.starttls.enable"));
        assertEquals("true", session.getProperty("mail.smtp.ssl.enable"));
        assertEquals("false", session.getProperty("mail.debug"));
        assertEquals("noreply@bahmni.org", session.getProperty("mail.from"));
        assertEquals("test_user", session.getProperty("mail.user"));
        assertEquals("test_password", session.getProperty("mail.password"));
    }


    @Test
    public void testGetAddressesShouldReturnAddressArrayWhenGivenValidAddresses() throws AddressException {
        String[] givenAddresses = {"test1@example.com", "test2@example.com"};

        Address[] addresses = messagingUtility.getAddresses(givenAddresses);

        assertTrue(addresses[0] instanceof InternetAddress);
        assertEquals("test1@example.com", addresses[0].toString());
        assertEquals("test2@example.com", addresses[1].toString());
    }

    @Test
    public void testGetAddressesShouldReturnEmptyAddressArrayWhenGivenNullInput() throws AddressException {
        Address[] addresses = messagingUtility.getAddresses(null);
        assertEquals(0, addresses.length);
    }

    @Test
    public void testGetAddressesShouldReturnEmptyAddressArrayWhenGivenEmptyInput() throws AddressException {
        String[] givenAddresses = {};

        Address[] addresses = messagingUtility.getAddresses(givenAddresses);

        assertNotNull(addresses);
        assertEquals(0, addresses.length);
    }

    @Test
    public void testGetSMSTokenFromTokenFile() throws IOException {
        final File tokenFile = folder.newFile("test.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tokenFile));
        writer.write("test_token");
        writer.close();

        String token = messagingUtility.getSMSTokenFromTokenFile(tokenFile.getAbsolutePath());

        assertEquals("test_token", token);
    }

    @Test
    public void testGetSMSTokenFromTokenFileThrowsExceptionWhenFileNotFound() {
        assertThrows(RuntimeException.class, () -> messagingUtility.getSMSTokenFromTokenFile("nonexistent_file.txt"));
    }
}

