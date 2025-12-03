package br.com.fiap.lambda.model;

import br.com.fiap.lambda.exception.ValidationException;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

public class EmailDetails {
    
    @NotBlank(message = "O remetente não pode estar em branco")
    @Email(message = "O endereço de e-mail do remetente é inválido")
    private final String from;
    
    @NotBlank(message = "O destinatário não pode estar em branco")
    @Email(message = "O endereço de e-mail do destinatário é inválido")
    private final String to;
    
    @NotBlank(message = "O assunto não pode estar em branco")
    private final String subject;
    
    @NotBlank(message = "O conteúdo do e-mail não pode estar em branco")
    private final String bodyHtml;

    public EmailDetails(String from, String to, String subject, String bodyHtml) {
        this.from = Objects.requireNonNull(from, "O remetente não pode ser nulo").trim();
        this.to = Objects.requireNonNull(to, "O destinatário não pode ser nulo").trim();
        this.subject = Objects.requireNonNull(subject, "O assunto não pode ser nulo").trim();
        this.bodyHtml = Objects.requireNonNull(bodyHtml, "O conteúdo do e-mail não pode ser nulo").trim();
        
        if (this.from.isEmpty()) {
            throw new ValidationException("from", "O remetente não pode estar vazio");
        }
        
        if (this.to.isEmpty()) {
            throw new ValidationException("to", "O destinatário não pode estar vazio");
        }
        
        if (this.subject.isEmpty()) {
            throw new ValidationException("subject", "O assunto não pode estar vazio");
        }
        
        if (this.bodyHtml.isEmpty()) {
            throw new ValidationException("bodyHtml", "O conteúdo do e-mail não pode estar vazio");
        }
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }
    
    @Override
    public String toString() {
        return "EmailDetails{" +
               "from='" + from + '\'' +
               ", to='" + to + '\'' +
               ", subject='" + subject + '\'' +
               ", bodyHtml.length()=" + (bodyHtml != null ? bodyHtml.length() : 0) +
               '}';
    }
}
