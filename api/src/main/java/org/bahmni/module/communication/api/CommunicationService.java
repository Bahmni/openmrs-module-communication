package org.bahmni.module.communication.api;

import org.bahmni.module.communication.model.MailContent;

public interface CommunicationService {
    void sendEmail(MailContent mailContent);

}
