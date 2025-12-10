package br.com.fiap.lambda.service;

import br.com.fiap.lambda.model.EmailPayload;
import java.time.format.DateTimeFormatter;

public class EmailFormatter {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public String format(EmailPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload não pode ser nulo");
        }
        
        return buildCriticalFeedbackBody(payload);
    }

    private String buildCriticalFeedbackBody(EmailPayload payload) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
            .append("<html lang=\"pt-BR\">")
            .append("<head>")
            .append("<meta charset=\"UTF-8\">")
            .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            .append("<title>Feedback Crítico</title>")
            .append("<style>")
            .append("  body { font-family: Arial, sans-serif; line-height: 1.6; background-color: #fff3cd; }")
            .append("  .header { background-color: #b71c1c; color: white; padding: 20px; text-align: center; }")
            .append("  .content { padding: 20px; background-color: white; margin: 20px; border: 3px solid #b71c1c; }")
            .append("  .footer { margin-top: 20px; font-size: 0.9em; color: #666; }")
            .append("  .urgency { color: #b71c1c; font-weight: bold; font-size: 1.2em; }")
            .append("  .note { color: #b71c1c; font-style: italic; background-color: #ffebee; padding: 15px; border-left: 5px solid #b71c1c; margin: 15px 0; }")
            .append("  .metadata { background-color: #ffebee; padding: 15px; margin: 15px 0; border-left: 4px solid #b71c1c; }")
            .append("  .alert-box { background-color: #b71c1c; color: white; padding: 15px; margin: 15px 0; text-align: center; font-weight: bold; font-size: 1.1em; }")
            .append("</style>")
            .append("</head>")
            .append("<body>")
            .append("<div class=\"header\"><h1>FEEDBACK CRÍTICO - AÇÃO IMEDIATA NECESSÁRIA</h1></div>")
            .append("<div class=\"content\">");
        
        html.append("<div class=\"alert-box\">ESTE FEEDBACK REQUER ATENÇÃO URGENTE E PRIORITÁRIA</div>");

        html.append("  <p><strong>Estudante:</strong> ")
            .append(payload.getEmailEstudante() != null ? payload.getEmailEstudante() : "N/A")
            .append("</p>");

        if (payload.getClassName() != null || payload.getTeacherName() != null) {
            html.append("  <div class=\"metadata\">")
                .append("    <p style=\"margin: 5px 0;\"><strong>Turma:</strong> ")
                .append(payload.getClassName() != null ? payload.getClassName() : "N/A")
                .append("</p>")
                .append("    <p style=\"margin: 5px 0;\"><strong>Professor:</strong> ")
                .append(payload.getTeacherName() != null ? payload.getTeacherName() : "N/A")
                .append("</p>")
                .append("  </div>");
        }
        
        html.append("  <p><strong>Data/Hora:</strong> ")
            .append(payload.getDataHora() != null ? payload.getDataHora().format(DATE_FORMATTER) : "Data não informada")
            .append("</p>")
            .append("  <p class=\"urgency\"><strong>Nível de Urgência:</strong> CRÍTICA</p>")
            .append("  <p><strong>Nota:</strong> <span style=\"color: #b71c1c; font-size: 1.3em; font-weight: bold;\">")
            .append(payload.getNota())
            .append("/10</span></p>")
            .append("  <div style=\"background-color: #fff3cd; padding: 15px; border: 2px solid #b71c1c; margin: 15px 0;\">")
            .append("    <p><strong style=\"color: #b71c1c; font-size: 1.1em;\">Feedback Detalhado:</strong></p>")
            .append("    <p style=\"font-size: 1.05em;\">")
            .append(escapeHtml(payload.getDescricao()).replace("\n", "<br>"))
            .append("</p>")
            .append("  </div>")
            .append("  <div class=\"note\">")
            .append("    <p style=\"margin: 0; font-size: 1.1em;\"><strong>AÇÃO REQUERIDA:</strong></p>")
            .append("    <p style=\"margin: 5px 0 0 0;\">Este feedback foi classificado como CRÍTICO e requer atenção imediata da equipe de gestão. ")
            .append("Por favor, entre em contato com o estudante o mais rápido possível e tome as medidas necessárias.</p>")
            .append("  </div>");

        if (payload.getFeedbackId() != null || payload.getCorrelationId() != null) {
            html.append("  <div style=\"margin-top: 20px; padding: 10px; background-color: #f5f5f5; border-left: 4px solid #b71c1c;\">")
                .append("    <p style=\"margin: 2px 0; font-size: 0.9em;\"><strong>ID do Feedback:</strong> ")
                .append(payload.getFeedbackId() != null ? payload.getFeedbackId() : "N/A")
                .append("</p>")
                .append("    <p style=\"margin: 2px 0; font-size: 0.9em;\"><strong>Correlation ID:</strong> ")
                .append(payload.getCorrelationId() != null ? payload.getCorrelationId() : "N/A")
                .append("</p>")
                .append("  </div>");
        }
        
        html.append("</div>")
            .append("<div class=\"footer\" style=\"text-align: center; padding: 20px;\">")
            .append("  <p style=\"color: #b71c1c; font-weight: bold;\">Este é um alerta automático de feedback crítico</p>")
            .append("  <p>Sistema de Gestão de Feedbacks - FIAP</p>")
            .append("</div>")
            .append("</body>")
            .append("</html>");
        
        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}