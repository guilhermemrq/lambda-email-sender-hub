package br.com.fiap.lambda.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class EmailPayload {
    private String descricao;
    private int nota;
    private String emailEstudante;
    private String nomeEstudante;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHora;
    
    private Urgencia urgencia;
    
    public enum Urgencia {
        BAIXA, MEDIA, ALTA
    }

    public EmailPayload() {
    }


    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getNota() {
        return nota;
    }

    public void setNota(int nota) {
        this.nota = nota;
    }

    public String getEmailEstudante() {
        return emailEstudante;
    }

    public void setEmailEstudante(String emailEstudante) {
        this.emailEstudante = emailEstudante;
    }

    public String getNomeEstudante() {
        return nomeEstudante;
    }

    public void setNomeEstudante(String nomeEstudante) {
        this.nomeEstudante = nomeEstudante;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Urgencia getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(Urgencia urgencia) {
        this.urgencia = urgencia;
    }
    
    public void setUrgencia(String urgencia) {
        this.urgencia = Urgencia.valueOf(urgencia.toUpperCase());
    }

    public String getAssuntoResumido() {
        String nome = getNomeEstudante() != null ? getNomeEstudante() : "Estudante";
        String descricaoResumida = getDescricao().length() > 30 ? 
            getDescricao().substring(0, 30) + "..." : getDescricao();
            
        return String.format("Feedback de %s - Nota: %d - %s", 
            nome, getNota(), descricaoResumida);
    }
}
