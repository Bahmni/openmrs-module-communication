package org.bahmni.module.communication.api;

import org.bahmni.module.communication.model.MailContent;
import org.openmrs.annotation.Authorized;

public interface CommunicationService {

    @Authorized({"Send Email"})
    void sendEmail(MailContent mailContent, String patientUuid);

}
