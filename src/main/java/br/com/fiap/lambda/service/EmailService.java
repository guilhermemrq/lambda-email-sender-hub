package br.com.fiap.lambda.service;

import br.com.fiap.lambda.gateway.EmailSender;
import br.com.fiap.lambda.model.EmailDetails;
import br.com.fiap.lambda.model.EmailPayload;

public class EmailService {

    private final EmailSender emailSender;
    private final EmailFormatter emailFormatter;
    private static final String DEFAULT_FROM_EMAIL = "noreply@seutecchallenge.com";

    public EmailService(EmailSender emailSender, EmailFormatter emailFormatter) {
        this.emailSender = emailSender;
        this.emailFormatter = emailFormatter;
    }

    public void processAndSend(EmailPayload payload) {
        if (payload == null || payload.getRecipientEmail() == null || payload.getRecipientEmail().isEmpty()) {
            System.err.println("Erro: Payload ou destinatário inválido. Payload: " + payload);
            return;
        }

        String htmlBody = emailFormatter.format(payload);

        EmailDetails details = new EmailDetails(
                DEFAULT_FROM_EMAIL,
                payload.getRecipientEmail(),
                payload.getSubject(),
                htmlBody
        );

        System.out.println("Chamando o EmailSender para: " + details.getTo());
        emailSender.send(details);
        System.out.println("Email enviado com sucesso (ou delegado para envio) para: " + details.getTo());
    }
}