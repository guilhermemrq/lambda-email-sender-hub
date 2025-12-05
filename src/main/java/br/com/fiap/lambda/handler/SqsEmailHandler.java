package br.com.fiap.lambda.handler;

import br.com.fiap.lambda.gateway.EmailSender;
import br.com.fiap.lambda.gateway.SesEmailSender;
import br.com.fiap.lambda.model.EmailPayload;
import br.com.fiap.lambda.service.EmailFormatter;
import br.com.fiap.lambda.service.EmailService;
import br.com.fiap.lambda.util.JsonMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
public class SqsEmailHandler implements RequestHandler<SQSEvent, Void> {

    private final EmailService emailService;

    private static final String DEFAULT_FROM_EMAIL = "noreply@fiap.com.br";

    public SqsEmailHandler() {
        AmazonSimpleEmailService sesClient = AmazonSimpleEmailServiceClientBuilder.defaultClient();
        EmailSender sesSender = new SesEmailSender(sesClient);
        EmailFormatter formatter = new EmailFormatter();
        this.emailService = new EmailService(sesSender, formatter, DEFAULT_FROM_EMAIL);
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        if (event == null || event.getRecords() == null) {
            throw new IllegalArgumentException("Evento SQS inválido ou sem mensagens");
        }

        context.getLogger().log("Recebido evento SQS com " + event.getRecords().size() + " mensagens.");

        for (SQSMessage message : event.getRecords()) {
            String messageBody = message.getBody();
            String messageId = message.getMessageId();
            context.getLogger().log("Processando Mensagem ID: " + messageId);

            try {
                EmailPayload payload = JsonMapper.fromJson(messageBody, EmailPayload.class);
                emailService.processAndSend(payload);
                context.getLogger().log("Mensagem " + messageId + " processada com sucesso");

            } catch (Exception e) {
                String errorMsg = String.format("Falha no processamento da mensagem %s. Causa: %s", 
                    messageId, e.getMessage());
                context.getLogger().log(errorMsg);
                throw new RuntimeException(errorMsg, e);
            }
        }

        context.getLogger().log("Processamento do lote SQS concluído com sucesso.");
        return null;
    }
}
