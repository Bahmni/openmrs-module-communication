package org.bahmni.module.communication.model;

public class MailContent {
    private String pdf;
    private String subject;
    private String body;
    private Recipient recipient;
    private String[] cc;
    private String[] bcc;

    public MailContent(String pdf, Recipient recipient, String subject, String body, String[] cc, String[] bcc) {
        this.pdf = pdf;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.cc = cc;
        this.bcc = bcc;
    }

    public MailContent(){}

    public String getPdf() { return pdf; }

    public void setPdf(String pdf) { this.pdf = pdf; }

    public String getSubject() { return subject; }

    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }

    public void setBody(String body) { this.body = body; }

    public Recipient getRecipient() { return recipient; }

    public void setRecipient(Recipient recipient) { this.recipient = recipient; }

    public String[] getCc() { return cc; }

    public void setCc(String[] cc) { this.cc = cc; }

    public String[] getBcc() { return bcc; }

    public void setBcc(String[] bcc) { this.bcc = bcc; }
}
