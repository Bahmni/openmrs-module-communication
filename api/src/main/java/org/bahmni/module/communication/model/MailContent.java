package org.bahmni.module.communication.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailContent {
    private String subject;
    private String body;
    private Recipient recipient;
    private String[] cc;
    private String[] bcc;
    private List<MailAttachment> mailAttachments;
}
