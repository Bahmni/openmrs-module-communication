package org.bahmni.module.communication.service.impl;


import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
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

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        messagingUtility = new MessagingUtility();
    }

    @Test
    public void shouldReturnSessionFromMailConfigPropertiesFile() throws IOException {
        String TMP_FOLDER = "/tmp";
        final File file = new File(TMP_FOLDER + "/mail-config.properties");
        FileUtils.writeStringToFile(file, "mail.send=true\n" + "mail.transport.protocol=smtp\n"
                + "mail.smtp.host=localhost\n" + "mail.smtp.port=25\n" + "mail.smtp.auth=false\n"
                + "mail.smtp.starttls.enable=true\n" + "mail.from=dummy@bahmni.org\n", Charset.defaultCharset());
        OpenmrsUtil.setApplicationDataDirectory(TMP_FOLDER);

        Session session = messagingUtility.getSession();

        assertEquals("localhost", session.getProperty("mail.smtp.host"));
        file.delete();
    }

    @Test
    public void shouldReturnSameSessionIfAlreadyInitialized() throws IOException {
        String TMP_FOLDER = "/tmp";
        final File file = new File(TMP_FOLDER + "/mail-config.properties");
        FileUtils.writeStringToFile(file, "mail.send=true\n" + "mail.transport.protocol=smtp\n"
                + "mail.smtp.host=localhost\n" + "mail.smtp.port=25\n" + "mail.smtp.auth=false\n"
                + "mail.smtp.starttls.enable=true\n" + "mail.from=dummy@bahmni.org\n", Charset.defaultCharset());
        OpenmrsUtil.setApplicationDataDirectory(TMP_FOLDER);

        Session session1 = messagingUtility.getSession();
        Session session2 = messagingUtility.getSession();

        assertSame(session1, session2);
        file.delete();
    }

    @Test
    public void shouldReturnSessionUsingOMRSPropertiesWhenMailConfigPropertiesFileDoesNotExist() {
        when(administrationService.getGlobalProperty("mail.transport_protocol", "smtp")).thenReturn("");
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

        assertEquals("localhost", session.getProperty("mail.smtp.host"));
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
        String[] givenAddresses = null;

        Address[] addresses = messagingUtility.getAddresses(givenAddresses);

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
        String testTokenFilePath = "test.txt";
        BufferedWriter writer = new BufferedWriter(new FileWriter(testTokenFilePath));
        writer.write("test_token");
        writer.close();

        String token = messagingUtility.getSMSTokenFromTokenFile(testTokenFilePath);
        assertEquals("test_token", token);

        new File(testTokenFilePath).delete();
    }

    @Test
    public void testGetSMSTokenFromTokenFileThrowsExceptionWhenFileNotFound() {
        assertThrows(RuntimeException.class, () -> messagingUtility.getSMSTokenFromTokenFile("nonexistent_file.txt"));
    }
}

