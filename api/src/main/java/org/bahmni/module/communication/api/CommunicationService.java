package org.bahmni.module.communication.api;

import org.bahmni.module.communication.model.MailContent;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;

public interface CommunicationService {
    void sendEmail(MailContent mailContent) throws MessagingException;

//    void sendSMS();
}
