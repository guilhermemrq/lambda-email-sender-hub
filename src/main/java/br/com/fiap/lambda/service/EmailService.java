package br.com.fiap.lambda.service;

import br.com.fiap.lambda.exception.EmailSendingException;
import br.com.fiap.lambda.exception.ValidationException;
import br.com.fiap.lambda.gateway.EmailSender;
import br.com.fiap.lambda.model.EmailDetails;
import br.com.fiap.lambda.model.EmailPayload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Serviço responsável por processar e enviar e-mails.
 * Inclui validações e tratamento de erros robusto.
 */

public class EmailService {
    private static final Logger logger = LogManager.getLogger(EmailService.class);
    
    private final EmailSender emailSender;
    private final EmailFormatter emailFormatter;
    private final String defaultFromEmail;

    /**
     * Constrói uma nova instância de EmailService.
     * 
     * @param emailSender Implementação do serviço de envio de e-mail
     * @param emailFormatter Formatador de e-mail
     * @param defaultFromEmail E-mail remetente padrão
     * @throws IllegalArgumentException se algum parâmetro for nulo
     */
    public EmailService(EmailSender emailSender, EmailFormatter emailFormatter, String defaultFromEmail) {
        this.emailSender = Objects.requireNonNull(emailSender, "EmailSender não pode ser nulo");
        this.emailFormatter = Objects.requireNonNull(emailFormatter, "EmailFormatter não pode ser nulo");
        this.defaultFromEmail = Objects.requireNonNull(defaultFromEmail, "O e-mail remetente padrão não pode ser nulo").trim();
        
        if (this.defaultFromEmail.isEmpty()) {
            throw new IllegalArgumentException("O e-mail remetente padrão não pode estar vazio");
        }
    }

    /**
     * Processa e envia um e-mail com base no payload fornecido.
     * 
     * @param payload Dados do e-mail a ser enviado
     * @throws ValidationException se o payload for inválido
     * @throws EmailSendingException se ocorrer um erro ao enviar o e-mail
     */
    public void processAndSend(EmailPayload payload) {
        try {
            validatePayload(payload);
            
            logger.debug("Iniciando processamento do e-mail para: {}", payload.getRecipientEmail());
            
            String htmlBody = emailFormatter.format(payload);
            logger.debug("E-mail formatado com sucesso");

            EmailDetails details = new EmailDetails(
                defaultFromEmail,
                payload.getRecipientEmail(),
                payload.getSubject(),
                htmlBody
            );
            
            logger.info("Enviando e-mail para: {}", details.getTo());
            emailSender.send(details);
            logger.info("E-mail enviado com sucesso para: {}", details.getTo());
            
        } catch (Exception e) {
            String errorMsg = String.format("Falha ao processar/enviar e-mail para %s: %s", 
                payload != null ? payload.getRecipientEmail() : "[destinatário desconhecido]", 
                e.getMessage());
            
            logger.error(errorMsg, e);
            throw new EmailSendingException(errorMsg, e);
        }
    }
    
    /**
     * Valida o payload do e-mail.
     * 
     * @param payload Payload a ser validado
     * @throws ValidationException se o payload for inválido
     */
    private void validatePayload(EmailPayload payload) {
        if (payload == null) {
            throw new ValidationException("payload", "O payload não pode ser nulo");
        }
        
        if (payload.getRecipientEmail() == null || payload.getRecipientEmail().trim().isEmpty()) {
            throw new ValidationException("recipientEmail", "O e-mail do destinatário não pode estar vazio");
        }
        
        if (payload.getSubject() == null || payload.getSubject().trim().isEmpty()) {
            throw new ValidationException("subject", "O assunto do e-mail não pode estar vazio");
        }
        
        // Se houver templateData, garante que não seja vazio
        if (payload.getTemplateData() != null && payload.getTemplateData().isEmpty()) {
            logger.warn("TemplateData está vazio para o e-mail: {}", payload.getRecipientEmail());
        }
    }
    }