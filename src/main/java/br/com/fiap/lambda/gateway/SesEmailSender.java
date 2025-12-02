package br.com.fiap.lambda.gateway;

import br.com.fiap.lambda.exception.EmailSendingException;
import br.com.fiap.lambda.model.EmailDetails;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class SesEmailSender implements EmailSender {
    private static final Logger logger = LogManager.getLogger(SesEmailSender.class);
    
    private final AmazonSimpleEmailService sesClient;

    public SesEmailSender(AmazonSimpleEmailService sesClient) {
        this.sesClient = Objects.requireNonNull(sesClient, "AmazonSimpleEmailService não pode ser nulo");
    }

    @Override
    public void send(EmailDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("EmailDetails não pode ser nulo");
        }

        logger.debug("Preparando para enviar e-mail para: {}", details.getTo());
        
        try {
            SendEmailRequest request = createSendEmailRequest(details);
            sesClient.sendEmail(request);
            
            logger.info("E-mail enviado com sucesso para: {}", details.getTo());
        } catch (Exception e) {
            String errorMsg = String.format("Falha ao enviar e-mail para %s: %s", 
                details.getTo(), e.getMessage());
            
            logger.error(errorMsg, e);
            throw new EmailSendingException(errorMsg, e);
        }
    }
    
    private SendEmailRequest createSendEmailRequest(EmailDetails details) {
        Destination destination = new Destination()
            .withToAddresses(details.getTo());

        Content subjectContent = new Content()
            .withData(details.getSubject());

        Body body = new Body()
            .withHtml(new Content().withData(details.getBodyHtml()));

        Message message = new Message()
            .withSubject(subjectContent)
            .withBody(body);

        return new SendEmailRequest()
            .withSource(details.getFrom())
            .withDestination(destination)
            .withMessage(message);
    }
}
