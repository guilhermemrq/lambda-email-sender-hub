package br.com.fiap.lambda.gateway;

import br.com.fiap.lambda.exception.EmailSendingException;
import br.com.fiap.lambda.model.EmailDetails;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
            SendRawEmailRequest request = createSendRawEmailRequest(details);
            sesClient.sendRawEmail(request);
            
            logger.info("E-mail enviado com sucesso para: {}", details.getTo());
        } catch (Exception e) {
            String errorMsg = String.format("Falha ao enviar e-mail para %s: %s", 
                details.getTo(), e.getMessage());
            
            logger.error(errorMsg, e);
            throw new EmailSendingException(errorMsg, e);
        }
    }
    
    private SendRawEmailRequest createSendRawEmailRequest(EmailDetails details) {
        try {
            String encodedSubject = java.util.Base64.getEncoder()
                .encodeToString(details.getSubject().getBytes(StandardCharsets.UTF_8));
            
            String encodedBody = java.util.Base64.getEncoder()
                .encodeToString(details.getBodyHtml().getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            sb.append("From: ").append(details.getFrom()).append("\r\n");
            sb.append("To: ").append(details.getTo()).append("\r\n");
            sb.append("Subject: =?UTF-8?B?").append(encodedSubject).append("?=\r\n");
            sb.append("MIME-Version: 1.0\r\n");
            sb.append("Content-Type: text/html; charset=UTF-8\r\n");
            sb.append("Content-Transfer-Encoding: base64\r\n");
            sb.append("\r\n");
            sb.append(encodedBody);
            
            ByteBuffer rawMessage = ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8));
            RawMessage message = new RawMessage(rawMessage);
            
            return new SendRawEmailRequest()
                .withRawMessage(message)
                .withSource(details.getFrom())
                .withDestinations(details.getTo());
                
        } catch (Exception e) {
            throw new EmailSendingException("Erro ao criar mensagem raw: " + e.getMessage(), e);
        }
    }
}
