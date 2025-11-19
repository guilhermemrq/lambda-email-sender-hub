package br.com.fiap.lambda;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;

@ApplicationScoped
public class EmailFormatter {
    public String format(EmailOutputModel emailOutputModel) {
        return String.format(
                "Novo Feedback Recebido:\n\n" +
                        "Descrição: %s\n" +
                        "Data de envio: %s\n" +
                        "Urgência: %s",
                emailOutputModel.getDescricao(),
                LocalDate.now(),
                emailOutputModel.getUrgencia()
        );
    }
}