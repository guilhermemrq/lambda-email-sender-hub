package br.com.fiap.lambda.service;

import br.com.fiap.lambda.exception.EmailSendingException;
import br.com.fiap.lambda.exception.ValidationException;
import br.com.fiap.lambda.gateway.EmailSender;
import br.com.fiap.lambda.model.EmailDetails;
import br.com.fiap.lambda.model.EmailPayload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;


public class EmailService {
    private static final Logger logger = LogManager.getLogger(EmailService.class);
    
    private final EmailSender emailSender;
    private final EmailFormatter emailFormatter;
    private final String defaultFromEmail;

    public EmailService(EmailSender emailSender, EmailFormatter emailFormatter, String defaultFromEmail) {
        this.emailSender = Objects.requireNonNull(emailSender, "EmailSender não pode ser nulo");
        this.emailFormatter = Objects.requireNonNull(emailFormatter, "EmailFormatter não pode ser nulo");
        this.defaultFromEmail = Objects.requireNonNull(defaultFromEmail, "O e-mail remetente padrão não pode ser nulo").trim();
        
        if (this.defaultFromEmail.isEmpty()) {
            throw new IllegalArgumentException("O e-mail remetente padrão não pode estar vazio");
        }
    }

    public void processAndSend(EmailPayload payload) {
        try {
            validatePayload(payload);
            
            logger.debug("Iniciando processamento do feedback do estudante: {}", payload.getNomeEstudante());
            
            String htmlBody = emailFormatter.format(payload);
            logger.debug("E-mail formatado com sucesso");

            String to = determinarDestinatario(payload.getUrgencia());
            String subject = String.format("Feedback %s - %s", 
                payload.getUrgencia().name().toLowerCase(), 
                payload.getAssuntoResumido());

            EmailDetails details = new EmailDetails(
                defaultFromEmail,
                to,
                subject,
                htmlBody
            );
            
            logger.info("Enviando e-mail para: {}", to);
            emailSender.send(details);
            logger.info("E-mail de feedback enviado com sucesso para: {}", to);
            
        } catch (Exception e) {
            String errorMsg = String.format("Falha ao processar/enviar e-mail: %s", e.getMessage());
            String studentInfo = payload != null ? 
                String.format("Estudante: %s <%s>", 
                    payload.getNomeEstudante() != null ? payload.getNomeEstudante() : "[sem nome]", 
                    payload.getEmailEstudante()) : 
                "[dados do estudante não disponíveis]";
            
            logger.error("{} - {}", studentInfo, errorMsg, e);
            throw new EmailSendingException(errorMsg, e);
        }
    }

    private void validatePayload(EmailPayload payload) {
        if (payload == null) {
            throw new ValidationException("payload", "O payload do feedback não pode ser nulo");
        }
        
        if (payload.getEmailEstudante() == null || payload.getEmailEstudante().trim().isEmpty()) {
            throw new ValidationException("emailEstudante", "O e-mail do estudante é obrigatório");
        }
        
        if (payload.getDescricao() == null || payload.getDescricao().trim().isEmpty()) {
            throw new ValidationException("descricao", "A descrição do feedback é obrigatória");
        }
        
        if (payload.getNota() < 0 || payload.getNota() > 10) {
            throw new ValidationException("nota", "A nota deve estar entre 0 e 10");
        }
        
        if (payload.getUrgencia() == null) {
            throw new ValidationException("urgencia", "O nível de urgência é obrigatório");
        }
    }
    
    /**
     * Determina o destinatário do e-mail com base no nível de urgência
     */
    private String determinarDestinatario(EmailPayload.Urgencia urgencia) {
        switch (urgencia) {
            case ALTA:
                return "suporte@fiap.com.br";
            case MEDIA:
                return "feedback@fiap.com.br";
            case BAIXA:
            default:
                return "relatorios@fiap.com.br";
        }
    }