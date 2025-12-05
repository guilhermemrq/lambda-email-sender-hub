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
 * Serviço para processar e enviar emails de feedbacks críticos.
 * Todos os feedbacks processados são considerados críticos.
 */
public class EmailService {
    private static final Logger logger = LogManager.getLogger(EmailService.class);

    private final EmailSender emailSender;
    private final EmailFormatter emailFormatter;
    private final String defaultFromEmail;
    private final String destinatarioEmail;

    public EmailService(EmailSender emailSender, EmailFormatter emailFormatter, String defaultFromEmail) {
        this(emailSender, emailFormatter, defaultFromEmail, "suporte@fiap.com.br");
    }

    public EmailService(EmailSender emailSender, EmailFormatter emailFormatter, String defaultFromEmail, String destinatarioEmail) {
        this.emailSender = Objects.requireNonNull(emailSender, "EmailSender não pode ser nulo");
        this.emailFormatter = Objects.requireNonNull(emailFormatter, "EmailFormatter não pode ser nulo");
        this.defaultFromEmail = Objects.requireNonNull(defaultFromEmail, "O e-mail remetente padrão não pode ser nulo").trim();
        this.destinatarioEmail = Objects.requireNonNull(destinatarioEmail, "O e-mail destinatário não pode ser nulo").trim();

        if (this.defaultFromEmail.isEmpty()) {
            throw new IllegalArgumentException("O e-mail remetente padrão não pode estar vazio");
        }
        
        if (this.destinatarioEmail.isEmpty()) {
            throw new IllegalArgumentException("O e-mail destinatário não pode estar vazio");
        }
    }

    /**
     * Processa e envia email de feedback crítico.
     * 
     * @param payload Dados do feedback crítico
     * @throws EmailSendingException se houver erro no envio
     * @throws ValidationException se os dados forem inválidos
     */
    public void processAndSend(EmailPayload payload) {
        try {
            validatePayload(payload);

            logger.debug("Iniciando processamento do feedback crítico do estudante: {}", payload.getNomeEstudante());

            String htmlBody = emailFormatter.format(payload);
            logger.debug("E-mail formatado com sucesso");

            String subject = buildSubject(payload);

            EmailDetails details = new EmailDetails(
                    defaultFromEmail,
                    destinatarioEmail,
                    subject,
                    htmlBody
            );

            logger.info("⚠️ Enviando alerta de feedback CRÍTICO para: {}", destinatarioEmail);
            emailSender.send(details);
            logger.info("✅ E-mail de feedback crítico enviado com sucesso para: {}", destinatarioEmail);

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

    /**
     * Constrói o assunto do email baseado nos dados do feedback.
     */
    private String buildSubject(EmailPayload payload) {
        String nome = payload.getNomeEstudante() != null ? payload.getNomeEstudante() : "Estudante";
        return String.format("🚨 FEEDBACK CRÍTICO - %s - Nota: %d/10", nome, payload.getNota());
    }

    /**
     * Valida os dados do payload de feedback crítico.
     */
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
    }
}