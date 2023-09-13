package org.bahmni.module.communication.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.communication.model.MailAttachment;
import org.bahmni.module.communication.model.MailContent;
import org.bahmni.module.communication.service.CommunicationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Session;
import javax.mail.Address;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

public class CommunicationServiceImpl implements CommunicationService {
	
	private static final String EMAIL_PROPERTIES_FILENAME = "mail-config.properties";
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	private volatile Session session = null;
	
	@Override
	public void sendEmail(MailContent mailContent) {
		try {
			MimeMessage mail = new MimeMessage(getSession());
			if (!Objects.equals(mail.getSession().getProperty("mail.send"), "true"))
				return;
			mail.setFrom(new InternetAddress(session.getProperty("mail.from")));
			Address[] toAddresses = new Address[1];
			toAddresses[0] = new InternetAddress(mailContent.getRecipient().getEmail());
			mail.setRecipients(javax.mail.Message.RecipientType.TO, mailContent.getRecipient().getEmail());
			
			if (mailContent.getCc() != null && mailContent.getCc().length > 0) {
				mail.setRecipients(javax.mail.Message.RecipientType.CC, getAddresses(mailContent.getCc()));
			}
			if (mailContent.getBcc() != null && mailContent.getCc().length > 0) {
				mail.setRecipients(javax.mail.Message.RecipientType.BCC, getAddresses(mailContent.getBcc()));
			}
			
			mail.setSubject(mailContent.getSubject());
			mail.setSentDate(new Date());
			MimeBodyPart mailBody = new MimeBodyPart();
			mailBody.setText(mailContent.getBody());
			Multipart multiPart = new MimeMultipart();
			multiPart.addBodyPart(mailBody);
			mail.setContent(multiPart);
			MimeBodyPart attachment = new MimeBodyPart();
			for (MailAttachment mailAttachment : mailContent.getMailAttachments()) {
				if (mailAttachment.getData() != null) {
					DataSource ds = new ByteArrayDataSource(java.util.Base64.getDecoder().decode(mailAttachment.getData()),
					        mailAttachment.getContentType());
					attachment.setDataHandler(new DataHandler(ds));
					attachment.setFileName(mailAttachment.getName());
					multiPart.addBodyPart(attachment);
				}
			}
			Thread t = Thread.currentThread();
			ClassLoader ccl = t.getContextClassLoader();
			t.setContextClassLoader(session.getClass().getClassLoader());
			Transport transport = session.getTransport();
			log.info("Sending Mail");
			transport.connect(session.getProperty("mail.smtp.host"), session.getProperty("mail.user"),
			    session.getProperty("mail.password"));
			transport.sendMessage(mail, mail.getAllRecipients());
			log.info("Mail Sent");
			transport.close();
			t.setContextClassLoader(ccl);
			
		}
		catch (Exception exception) {
			throw new RuntimeException("Error occurred while sending email", exception);
		}
	}
	
	private Session getSession() {
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
	
	private Address[] getAddresses(String[] givenAddresses) throws AddressException {
		if (givenAddresses != null && givenAddresses.length > 0) {
			Address[] addresses = new Address[givenAddresses.length];
			for (int i = 0; i < givenAddresses.length; i++) {
				addresses[i] = new InternetAddress(givenAddresses[i]);
			}
			return addresses;
		}
		return new Address[0];
	}
	
	private Properties mailSessionPropertiesFromPath() {
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
}
