package br.com.fiap.lambda.gateway;

import br.com.fiap.lambda.model.EmailDetails;

public interface EmailSender {
    void send(EmailDetails details);
}