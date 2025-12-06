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
            StringBuilder sb = new StringBuilder();
            sb.append("From: ").append(details.getFrom()).append("\n");
            sb.append("To: ").append(details.getTo()).append("\n");
            sb.append("Subject: =?UTF-8?B?")
              .append(java.util.Base64.getEncoder().encodeToString(details.getSubject().getBytes(StandardCharsets.UTF_8)))
              .append("?=\n");
            sb.append("MIME-Version: 1.0\n");
            sb.append("Content-Type: text/html; charset=UTF-8\n");
            sb.append("Content-Transfer-Encoding: quoted-printable\n");
            sb.append("\n");
            sb.append(toQuotedPrintable(details.getBodyHtml()));
            
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
    
    private String toQuotedPrintable(String text) {
        if (text == null) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        int lineLength = 0;
        
        for (byte b : bytes) {
            int value = b & 0xFF;
            
            if ((value >= 33 && value <= 60) || (value >= 62 && value <= 126)) {
                result.append((char) value);
                lineLength++;
            } else if (value == 32) {
                result.append(' ');
                lineLength++;
            } else if (value == 10) {
                result.append("\n");
                lineLength = 0;
            } else {
                result.append(String.format("=%02X", value));
                lineLength += 3;
            }
            
            if (lineLength >= 75) {
                result.append("=\n");
                lineLength = 0;
            }
        }
        
        return result.toString();
    }
}
