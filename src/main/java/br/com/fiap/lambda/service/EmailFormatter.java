package br.com.fiap.lambda.service;

import br.com.fiap.lambda.model.EmailPayload;

public class EmailFormatter {
    public String format(EmailPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload não pode ser nulo");
        }
        
        String templateName = payload.getTemplateName();
        if (templateName == null) {
            templateName = "DEFAULT";
        }
        
        if ("URGENT_FEEDBACK".equals(templateName)) {
            return buildUrgentFeedbackBody(payload);
        }
        return "<html><body><h1>Olá!</h1><p>Sua mensagem: " + payload.getSubject() + "</p></body></html>";
    }

    private String buildUrgentFeedbackBody(EmailPayload payload) {
        String urgency = payload.getTemplateData().getOrDefault("urgency_level", "Alta");
        String comment = payload.getTemplateData().getOrDefault("comment", "Sem comentário.");

        return "<html>" +
                "<body>" +
                "<h2>🚨 Alerta de Feedback Urgente!</h2>" +
                "<p><strong>Nível de Urgência:</strong> " + urgency + "</p>" +
                "<p><strong>Comentário do Aluno:</strong></p>" +
                "<p style='border: 1px solid red; padding: 10px;'>" + comment + "</p>" +
                "<p>Ação imediata é necessária.</p>" +
                "</body>" +
                "</html>";
    }

}