package br.com.fiap.lambda;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

@ApplicationScoped
public class QueueService {
    private final SqsClient sqsClient = SqsClient.create();

    @ConfigProperty(name = "sqs.feedback.queue.url")
    String queueUrl;

    @Inject
    FeedbackParser feedbackParser;

    @Inject
    EmailFormatter emailFormatter;

    @Inject
    EmailService emailService;

    public void processFeedbackQueue() {
        List<Message> messages = sqsClient.receiveMessage(r -> r.queueUrl(queueUrl).maxNumberOfMessages(10)).messages();

        for (Message message : messages) {
            try {
                String feedbackBody = message.body();
                EmailOutputModel emailOutputModel = feedbackParser.parse(feedbackBody);
                String emailBody = emailFormatter.format(emailOutputModel);

                emailService.sendEmail("juquinha@feijoada.com", "Novo Feedback Recebido", emailBody);

                sqsClient.deleteMessage(d -> d.queueUrl(queueUrl).receiptHandle(message.receiptHandle()));
            } catch (Exception e) {
                System.err.println("Erro ao processar mensagem: " + e.getMessage());
            }
        }
    }
}