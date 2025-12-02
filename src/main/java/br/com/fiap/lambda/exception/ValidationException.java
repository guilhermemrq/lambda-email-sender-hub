package br.com.fiap.lambda.exception;

public class ValidationException extends RuntimeException {
    
    private final String field;
    private final String error;

    public ValidationException(String field, String error) {
        super(String.format("Validation error on field '%s': %s", field, error));
        this.field = field;
        this.error = error;
    }

    public String getField() {
        return field;
    }

    public String getError() {
        return error;
    }
}
