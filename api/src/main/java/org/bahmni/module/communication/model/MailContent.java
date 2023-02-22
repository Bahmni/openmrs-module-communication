package org.bahmni.module.communication.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailContent {
    private String pdf;
    private String fileName;
    private String subject;
    private String body;
    private Recipient recipient;
    private String[] cc;
    private String[] bcc;
}
