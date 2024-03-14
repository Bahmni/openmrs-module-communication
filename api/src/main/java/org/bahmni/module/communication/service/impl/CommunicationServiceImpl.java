package org.bahmni.module.communication.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bahmni.module.communication.model.MailAttachment;
import org.bahmni.module.communication.model.MailContent;
import org.bahmni.module.communication.model.SMSRequest;
import org.bahmni.module.communication.service.CommunicationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Session;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CommunicationServiceImpl implements CommunicationService {

	MessagingUtility messagingUtility;

	//	private final static String SMS_URI = "bahmni.sms.url";

	private final static String SMS_TOKEN_KEY_FILE = "sms-communications-token.txt";

	private final Log log = LogFactory.getLog(this.getClass());

	public void setMessagingUtility(MessagingUtility messagingUtility) {
		this.messagingUtility = messagingUtility;
	}

	@Override
	public void sendEmail(MailContent mailContent) {
		try {
			Session session = messagingUtility.getSession();
			if (!Objects.equals(session.getProperty("mail.send"), "true"))
				return;
			MimeMessage mail = new MimeMessage(session);
			mail.setFrom(new InternetAddress(session.getProperty("mail.from")));
			mail.setRecipients(javax.mail.Message.RecipientType.TO, mailContent.getRecipient().getEmail());

			if (mailContent.getCc() != null && mailContent.getCc().length > 0) {
				mail.setRecipients(javax.mail.Message.RecipientType.CC, messagingUtility.getAddresses(mailContent.getCc()));
			}
			if (mailContent.getBcc() != null && mailContent.getBcc().length > 0) {
				mail.setRecipients(javax.mail.Message.RecipientType.BCC, messagingUtility.getAddresses(mailContent.getBcc()));
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

	@Override
	public void sendSMS(String phoneNumber, String message) {
		try {
			SMSRequest smsRequest = new SMSRequest();
			smsRequest.setPhoneNumber(phoneNumber);
			smsRequest.setMessage(message);
			ObjectMapper objMapper = new ObjectMapper();
			String jsonObject = objMapper.writeValueAsString(smsRequest);
			StringEntity params = new StringEntity(jsonObject);
			String smsUrl = Context.getAdministrationService().getGlobalProperty("bahmni.sms.url");
			if (smsUrl == null || smsUrl.isEmpty()) {
				log.info("Since SMSUrl property not set .SMS not sent.");
				return;
			}
			HttpPost request = new HttpPost(Context.getMessageSourceService().getMessage(smsUrl, null, new Locale("en")));
			request.addHeader("content-type", "application/json");
			String tokenFilePath = new File(OpenmrsUtil.getApplicationDataDirectory() + "/sms-token", SMS_TOKEN_KEY_FILE)
			        .getAbsolutePath();
			String token = messagingUtility.getSMSTokenFromTokenFile(tokenFilePath);

			if (token == null) {
				throw new RuntimeException("Token not found in the token file: " + tokenFilePath);
			}

			request.addHeader("Authorization", "Bearer " + token);
			request.setEntity(params);
			CloseableHttpClient httpClient = HttpClients.createDefault();
			httpClient.execute(request);
			httpClient.close();
		}
		catch (Exception e) {
			log.error("Exception occurred in sending SMS ", e);
			throw new RuntimeException("Exception occurred in sending SMS ", e);
		}
	}
}
