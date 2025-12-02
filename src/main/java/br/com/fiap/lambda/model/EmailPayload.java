package br.com.fiap.lambda.model;

import java.util.Map;

public class EmailPayload {

    private String recipientEmail;
    private String subject;
    private String templateName;
    private Map<String, String> templateData;

    public EmailPayload() {
    }

    public EmailPayload(String recipientEmail, String subject, String templateName, Map<String, String> templateData) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.templateName = templateName;
        this.templateData = templateData;
    }
    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Map<String, String> getTemplateData() {
        return templateData;
    }

    public void setTemplateData(Map<String, String> templateData) {
        this.templateData = templateData;
    }
}
