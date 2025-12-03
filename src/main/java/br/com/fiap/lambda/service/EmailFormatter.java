package br.com.fiap.lambda.service;

import br.com.fiap.lambda.model.EmailPayload;
import java.time.format.DateTimeFormatter;

public class EmailFormatter {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    public String format(EmailPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload não pode ser nulo");
        }
        

        switch (payload.getUrgencia()) {
            case ALTA:
                return buildUrgentFeedbackBody(payload);
            case MEDIA:
                return buildStandardFeedbackBody(payload);
            case BAIXA:
            default:
                return buildSimpleFeedbackBody(payload);
        }
    }

    private String buildUrgentFeedbackBody(EmailPayload payload) {
        return String.format(""
            + "<html>"
            + "<head><style>"
            + "  body { font-family: Arial, sans-serif; line-height: 1.6; }"
            + "  .header { background-color: #d32f2f; color: white; padding: 15px; }"
            + "  .content { padding: 20px; }"
            + "  .footer { margin-top: 20px; font-size: 0.9em; color: #666; }"
            + "  .urgency { color: #d32f2f; font-weight: bold; }"
            + "  .note { color: #d32f2f; font-style: italic; }"
            + "</style></head>"
            + "<body>"
            + "<div class=\"header\"><h2>🚨 Feedback Requer Atenção Imediata</h2></div>"
            + "<div class=\"content\">"
            + "  <p><strong>Estudante:</strong> %s &lt;%s&gt;</p>"
            + "  <p><strong>Data/Hora:</strong> %s</p>"
            + "  <p class=\"urgency\"><strong>Nível de Urgência:</strong> %s</p>"
            + "  <p><strong>Nota:</strong> %d/10</p>"
            + "  <div>"
            + "    <p><strong>Feedback:</strong></p>"
            + "    <p>%s</p>"
            + "  </div>"
            + "  <p class=\"note\">Por favor, tome as providências necessárias o mais rápido possível.</p>"
            + "</div>"
            + "<div class=\"footer\">"
            + "  <p>Este é um e-mail automático, por favor não responda.</p>"
            + "</div>"
            + "</body>"
            + "</html>",
            payload.getNomeEstudante() != null ? payload.getNomeEstudante() : "Estudante",
            payload.getEmailEstudante(),
            payload.getDataHora() != null ? payload.getDataHora().format(DATE_FORMATTER) : "Data não informada",
            payload.getUrgencia().name(),
            payload.getNota(),
            payload.getDescricao().replace("\n", "<br>")
        );
    }
    
    private String buildStandardFeedbackBody(EmailPayload payload) {
        return String.format(""
            + "<html>"
            + "<head><style>"
            + "  body { font-family: Arial, sans-serif; line-height: 1.6; }"
            + "  .header { background-color: #1976d2; color: white; padding: 15px; }"
            + "  .content { padding: 20px; }"
            + "  .footer { margin-top: 20px; font-size: 0.9em; color: #666; }"
            + "</style></head>"
            + "<body>"
            + "<div class=\"header\"><h2>📝 Novo Feedback Recebido</h2></div>"
            + "<div class=\"content\">"
            + "  <p><strong>Estudante:</strong> %s &lt;%s&gt;</p>"
            + "  <p><strong>Data/Hora:</strong> %s</p>"
            + "  <p><strong>Nota:</strong> %d/10</p>"
            + "  <div>"
            + "    <p><strong>Feedback:</strong></p>"
            + "    <p>%s</p>"
            + "  </div>"
            + "</div>"
            + "<div class=\"footer\">"
            + "  <p>Este é um e-mail automático, por favor não responda.</p>"
            + "</div>"
            + "</body>"
            + "</html>",
            payload.getNomeEstudante() != null ? payload.getNomeEstudante() : "Estudante",
            payload.getEmailEstudante(),
            payload.getDataHora() != null ? payload.getDataHora().format(DATE_FORMATTER) : "Data não informada",
            payload.getNota(),
            payload.getDescricao().replace("\n", "<br>")
        );
    }
    
    private String buildSimpleFeedbackBody(EmailPayload payload) {
        return String.format(""
            + "<html>"
            + "<head><style>"
            + "  body { font-family: Arial, sans-serif; line-height: 1.6; }"
            + "  .header { background-color: #43a047; color: white; padding: 15px; }"
            + "  .content { padding: 20px; }"
            + "  .footer { margin-top: 20px; font-size: 0.9em; color: #666; }"
            + "</style></head>"
            + "<body>"
            + "<div class=\"header\"><h2> Feedback Recebido</h2></div>"
            + "<div class=\"content\">"
            + "  <p>Um novo feedback foi registrado no sistema.</p>"
            + "  <p><strong>Estudante:</strong> %s &lt;%s&gt;</p>"
            + "  <p><strong>Nota:</strong> %d/10</p>"
            + "  <p><strong>Resumo:</strong> %s</p>"
            + "</div>"
            + "<div class=\"footer\">"
            + "  <p>Este é um e-mail automático, por favor não responda.</p>"
            + "</div>"
            + "</body>"
            + "</html>",
            payload.getNomeEstudante() != null ? payload.getNomeEstudante() : "Estudante",
            payload.getEmailEstudante(),
            payload.getNota(),
            payload.getDescricao().length() > 100 ? 
                payload.getDescricao().substring(0, 100) + "..." : 
                payload.getDescricao()
        );
    }
}