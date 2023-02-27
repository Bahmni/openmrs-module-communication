package org.bahmni.module.communication.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailAttachment {
    private String contentType;
    private String name;
    private String data;
    private String URL;
}
