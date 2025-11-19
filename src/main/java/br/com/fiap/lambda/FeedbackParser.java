package br.com.fiap.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FeedbackParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmailOutputModel parse(String feedbackBody) {
        try {
            return objectMapper.readValue(feedbackBody, EmailOutputModel.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao parsear o feedback: " + e.getMessage(), e);
        }
    }
}