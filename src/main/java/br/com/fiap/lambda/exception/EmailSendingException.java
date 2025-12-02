package br.com.fiap.lambda.exception;

/**
 * Exceção lançada quando ocorre um erro ao tentar enviar um e-mail.
 */
public class EmailSendingException extends RuntimeException {
    
    /**
     * Constrói uma nova exceção com a mensagem de erro especificada.
     * 
     * @param message A mensagem de erro
     */
    public EmailSendingException(String message) {
        super(message);
    }
    
    /**
     * Constrói uma nova exceção com a mensagem de erro e a causa especificadas.
     * 
     * @param message A mensagem de erro
     * @param cause A causa da exceção
     */
    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
