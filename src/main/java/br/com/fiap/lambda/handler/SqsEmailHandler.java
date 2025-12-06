package br.com.fiap.lambda.handler;

import br.com.fiap.lambda.gateway.EmailSender;
import br.com.fiap.lambda.gateway.SesEmailSender;
import br.com.fiap.lambda.model.EmailPayload;
import br.com.fiap.lambda.repository.UsuarioRepository;
import br.com.fiap.lambda.service.EmailBroadcastService;
import br.com.fiap.lambda.service.EmailFormatter;
import br.com.fiap.lambda.util.JsonMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.amazonaws.services.rdsdata.AWSRDSData;
import com.amazonaws.services.rdsdata.AWSRDSDataClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;

public class SqsEmailHandler implements RequestHandler<SQSEvent, Void> {

    private final EmailBroadcastService emailBroadcastService;

    public SqsEmailHandler() {
        String fromEmail = System.getenv("FROM_EMAIL");
        if (fromEmail == null || fromEmail.trim().isEmpty()) {
            fromEmail = "noreply@fiap.com.br";
        }

        String clusterArn = System.getenv("DB_CLUSTER_ARN");
        String secretArn = System.getenv("DB_SECRET_ARN");
        String database = System.getenv("DB_NAME");
        
        if (clusterArn == null || secretArn == null || database == null) {
            throw new IllegalStateException(
                "Variáveis de ambiente do banco de dados não configuradas: DB_CLUSTER_ARN, DB_SECRET_ARN, DB_NAME"
            );
        }

        AmazonSimpleEmailService sesClient = AmazonSimpleEmailServiceClientBuilder.defaultClient();
        AWSRDSData rdsDataClient = AWSRDSDataClientBuilder.defaultClient();

        EmailSender sesSender = new SesEmailSender(sesClient);
        EmailFormatter formatter = new EmailFormatter();
        UsuarioRepository usuarioRepository = new UsuarioRepository(rdsDataClient, clusterArn, secretArn, database);
        
        this.emailBroadcastService = new EmailBroadcastService(usuarioRepository, sesSender, formatter, fromEmail);
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

                EmailBroadcastService.BroadcastResult result = emailBroadcastService.broadcastCriticalFeedback(payload);
                
                context.getLogger().log(String.format(
                    "Broadcast concluído para mensagem %s. Total: %d usuários, Sucesso: %d, Falhas: %d",
                    messageId, result.getTotalUsuarios(), result.getSuccessCount(), result.getFailureCount()
                ));
                
                if (result.getFailureCount() > 0) {
                    context.getLogger().log("E-mails com falha: " + String.join(", ", result.getFailedEmails()));
                }
                
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
