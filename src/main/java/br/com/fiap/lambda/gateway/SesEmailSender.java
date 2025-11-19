package br.com.fiap.lambda.gateway;

import br.com.fiap.lambda.model.EmailDetails;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;

public class SesEmailSender implements EmailSender {

    private final AmazonSimpleEmailService sesClient;

    public SesEmailSender(AmazonSimpleEmailService sesClient) {
        this.sesClient = sesClient;
    }

    @Override
    public void send(EmailDetails details) {
        Destination destination = new Destination()
                .withToAddresses(details.getTo());

        Content subjectContent = new Content().withData(details.getSubject());

        Body body = new Body().withHtml(new Content().withData(details.getBodyHtml()));

        Message message = new Message()
                .withSubject(subjectContent)
                .withBody(body);

        SendEmailRequest request = new SendEmailRequest()
                .withSource(details.getFrom())
                .withDestination(destination)
                .withMessage(message);

        try {
            sesClient.sendEmail(request);
            System.out.println("Email enviado via SES. Destinatário: " + details.getTo());
        } catch (Exception e) {
            System.err.println("Falha ao enviar e-mail via SES para " + details.getTo() + ": " + e.getMessage());
            throw new RuntimeException("Erro no SES Gateway", e);
        }
    }
}
