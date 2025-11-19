package br.com.fiap.lambda.model;

public class EmailDetails {

    private String from;
    private String to;
    private String subject;
    private String bodyHtml;

    public EmailDetails(String from, String to, String subject, String bodyHtml) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.bodyHtml = bodyHtml;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }
}
