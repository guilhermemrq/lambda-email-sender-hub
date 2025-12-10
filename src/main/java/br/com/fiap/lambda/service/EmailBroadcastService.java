package br.com.fiap.lambda.service;

import br.com.fiap.lambda.gateway.EmailSender;
import br.com.fiap.lambda.model.EmailDetails;
import br.com.fiap.lambda.model.EmailPayload;
import br.com.fiap.lambda.model.Usuario;
import br.com.fiap.lambda.repository.UsuarioRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EmailBroadcastService {
    private static final Logger logger = LogManager.getLogger(EmailBroadcastService.class);
    
    private final UsuarioRepository usuarioRepository;
    private final EmailSender emailSender;
    private final EmailFormatter emailFormatter;
    private final String defaultFromEmail;

    public EmailBroadcastService(
            UsuarioRepository usuarioRepository,
            EmailSender emailSender,
            EmailFormatter emailFormatter,
            String defaultFromEmail) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "UsuarioRepository não pode ser nulo");
        this.emailSender = Objects.requireNonNull(emailSender, "EmailSender não pode ser nulo");
        this.emailFormatter = Objects.requireNonNull(emailFormatter, "EmailFormatter não pode ser nulo");
        this.defaultFromEmail = Objects.requireNonNull(defaultFromEmail, "E-mail remetente não pode ser nulo");
    }

    public BroadcastResult broadcastCriticalFeedback(EmailPayload payload) {
        logger.info("Iniciando broadcast de feedback crítico para todos os administradores ativos");
        
        BroadcastResult result = new BroadcastResult();
        
        try {
            List<Usuario> usuarios = usuarioRepository.findAllAtivos();
            result.setTotalUsuarios(usuarios.size());
            
            if (usuarios.isEmpty()) {
                logger.warn("Nenhum administrador ativo encontrado para envio de e-mail");
                return result;
            }
            
            logger.info("Enviando e-mail para {} administradores ativos", usuarios.size());

            String htmlBody = emailFormatter.format(payload);
            String subject = buildSubject(payload);

            for (Usuario usuario : usuarios) {
                try {
                    logger.debug("Enviando e-mail para: {} <{}>", usuario.getNome(), usuario.getEmail());
                    
                    EmailDetails details = new EmailDetails(
                        defaultFromEmail,
                        usuario.getEmail(),
                        subject,
                        htmlBody
                    );
                    
                    emailSender.send(details);
                    result.incrementSuccess();
                    result.addSuccessEmail(usuario.getEmail());
                    
                    logger.info("E-mail enviado com sucesso para: {}", usuario.getEmail());
                    
                } catch (Exception e) {
                    result.incrementFailure();
                    result.addFailedEmail(usuario.getEmail(), e.getMessage());
                    logger.error("Falha ao enviar e-mail para {}: {}", usuario.getEmail(), e.getMessage(), e);
                }
            }
            
            logger.info("Broadcast concluído. Sucessos: {}, Falhas: {}", 
                result.getSuccessCount(), result.getFailureCount());
            
        } catch (Exception e) {
            logger.error("Erro crítico durante broadcast: {}", e.getMessage(), e);
            throw new RuntimeException("Falha no broadcast de e-mails", e);
        }
        
        return result;
    }

    private String buildSubject(EmailPayload payload) {
        String email = payload.getEmailEstudante() != null ? payload.getEmailEstudante() : "Estudante";
        return String.format("FEEDBACK CRITICO - %s - Nota: %d/10", email, payload.getNota());
    }

    public static class BroadcastResult {
        private int totalUsuarios;
        private int successCount;
        private int failureCount;
        private List<String> successEmails = new ArrayList<>();
        private List<String> failedEmails = new ArrayList<>();
        private List<String> errorMessages = new ArrayList<>();

        public int getTotalUsuarios() {
            return totalUsuarios;
        }

        public void setTotalUsuarios(int totalUsuarios) {
            this.totalUsuarios = totalUsuarios;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void incrementSuccess() {
            this.successCount++;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public void incrementFailure() {
            this.failureCount++;
        }

        public void addSuccessEmail(String email) {
            this.successEmails.add(email);
        }

        public void addFailedEmail(String email, String error) {
            this.failedEmails.add(email);
            this.errorMessages.add(String.format("%s: %s", email, error));
        }

        public List<String> getSuccessEmails() {
            return successEmails;
        }

        public List<String> getFailedEmails() {
            return failedEmails;
        }

        public List<String> getErrorMessages() {
            return errorMessages;
        }

        @Override
        public String toString() {
            return String.format("BroadcastResult{total=%d, sucesso=%d, falhas=%d}", 
                totalUsuarios, successCount, failureCount);
        }
    }
}
