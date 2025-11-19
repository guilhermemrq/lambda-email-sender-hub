package br.com.fiap.lambda;

import jakarta.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@ApplicationScoped
public class EmailService {
    private final SesClient sesClient = SesClient.create();

    public void sendEmail(String to, String subject, String body) {
        try {
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder().text(Content.builder().data(body).build()).build())
                            .build())
                    .build();

            sesClient.sendEmail(emailRequest);
        } catch (SesException e) {
            throw new RuntimeException("Erro ao enviar e-mail: " + e.awsErrorDetails().errorMessage(), e);
        }
    }
}