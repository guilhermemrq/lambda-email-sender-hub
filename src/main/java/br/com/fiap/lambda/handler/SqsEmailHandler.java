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
    public SqsEmailHandler() {
        AmazonSimpleEmailService sesClient = AmazonSimpleEmailServiceClientBuilder.defaultClient();

        EmailSender sesSender = new SesEmailSender(sesClient);

        EmailFormatter formatter = new EmailFormatter();

        this.emailService = new EmailService(sesSender, formatter);
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        context.getLogger().log("Recebido evento SQS com " + event.getRecords().size() + " mensagens.");

        for (SQSMessage message : event.getRecords()) {
            String messageBody = message.getBody();
            context.getLogger().log("Processando Mensagem ID: " + message.getMessageId() + " | Body: " + messageBody);

            try {
                EmailPayload payload = JsonMapper.fromJson(messageBody, EmailPayload.class);

                emailService.processAndSend(payload);

            } catch (Exception e) {
                context.getLogger().log("Falha no processamento da mensagem " + message.getMessageId() + ". Mensagem voltará para a fila.");

                throw new RuntimeException("Erro ao processar mensagem SQS: " + e.getMessage(), e);
            }
        }

        context.getLogger().log("Processamento do lote SQS concluído com sucesso.");
        return null;
    }
}
