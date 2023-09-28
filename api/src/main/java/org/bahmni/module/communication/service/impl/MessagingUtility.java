package org.bahmni.module.communication.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Component;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Component
public class MessagingUtility {
    private static final String EMAIL_PROPERTIES_FILENAME = "mail-config.properties";
    private final Log log = LogFactory.getLog(this.getClass());
    private volatile Session session = null;
    public Session getSession() {
        if (session == null) {
            synchronized (this) {
                if (session == null) {
                    Properties sessionProperties = mailSessionPropertiesFromPath();
                    if (sessionProperties == null) {
                        log.info("Could not load mail properties from application data directory. Loading from OMRS settings.");
                        sessionProperties = getSessionPropertiesFromOMRS();
                    }
                    final String user = sessionProperties.getProperty("mail.user");
                    final String password = sessionProperties.getProperty("mail.password");
                    if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) {
                        session = Session.getInstance(sessionProperties, new Authenticator() {

                            public PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(user, password);
                            }
                        });
                    } else {
                        session = Session.getInstance(sessionProperties);
                    }
                }
            }
        }
        return session;
    }

    public Address[] getAddresses(String[] givenAddresses) throws AddressException {
        if (givenAddresses != null && givenAddresses.length > 0) {
            Address[] addresses = new Address[givenAddresses.length];
            for (int i = 0; i < givenAddresses.length; i++) {
                addresses[i] = new InternetAddress(givenAddresses[i]);
            }
            return addresses;
        }
        return new Address[0];
    }

    public Properties mailSessionPropertiesFromPath() {
        Path propertyFilePath = Paths.get(OpenmrsUtil.getApplicationDataDirectory(), EMAIL_PROPERTIES_FILENAME);
        if (Files.exists(propertyFilePath)) {
            Properties properties = new Properties();
            try {
                log.info("Reading properties from: " + propertyFilePath);
                properties.load(Files.newInputStream(propertyFilePath));
                return properties;
            }
            catch (IOException e) {
                log.error("Could not load email properties from: " + propertyFilePath, e);
            }
        } else {
            log.warn("No mail configuration defined at " + propertyFilePath);
        }
        return null;
    }

    private Properties getSessionPropertiesFromOMRS() {
        Properties p = new Properties();
        p.put("mail.transport.protocol",
                Context.getAdministrationService().getGlobalProperty("mail.transport_protocol", "smtp"));
        p.put("mail.smtp.host", Context.getAdministrationService().getGlobalProperty("mail.smtp_host", ""));
        p.put("mail.smtp.port", Context.getAdministrationService().getGlobalProperty("mail.smtp_port", "25")); // mail.smtp_port
        p.put("mail.smtp.auth", Context.getAdministrationService().getGlobalProperty("mail.smtp_auth", "false")); // mail.smtp_auth
        p.put("mail.smtp.starttls.enable",
                Context.getAdministrationService().getGlobalProperty("mail.smtp.starttls.enable", "true"));
        p.put("mail.smtp.ssl.enable", Context.getAdministrationService().getGlobalProperty("mail.smtp.ssl.enable", "true"));
        p.put("mail.debug", Context.getAdministrationService().getGlobalProperty("mail.debug", "false"));
        p.put("mail.from", Context.getAdministrationService().getGlobalProperty("mail.from", ""));
        p.put("mail.user", Context.getAdministrationService().getGlobalProperty("mail.user", ""));
        p.put("mail.password", Context.getAdministrationService().getGlobalProperty("mail.password", ""));
        return p;
    }

    public String getSMSTokenFromTokenFile(String tokenFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(tokenFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                return line;
            }
        } catch (IOException e) {
            log.error("Error loading token file: " + tokenFilePath, e);
            throw new RuntimeException("Error loading token file: " + tokenFilePath, e);
        }
        return null;
    }
}
