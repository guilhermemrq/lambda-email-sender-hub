package br.com.fiap.lambda.model;

import br.com.fiap.lambda.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailDetailsTest {

    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_SUBJECT = "Test Subject";
    private static final String VALID_BODY = "<html><body>Test</body></html>";

    @Test
    void shouldCreateEmailDetailsWithValidParameters() {
        // Act
        EmailDetails details = new EmailDetails(VALID_EMAIL, "recipient@example.com", VALID_SUBJECT, VALID_BODY);

        // Assert
        assertNotNull(details);
        assertEquals(VALID_EMAIL, details.getFrom());
        assertEquals("recipient@example.com", details.getTo());
        assertEquals(VALID_SUBJECT, details.getSubject());
        assertEquals(VALID_BODY, details.getBodyHtml());
    }

    @Test
    void shouldThrowExceptionWhenFromIsNull() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
            () -> new EmailDetails(null, "recipient@example.com", VALID_SUBJECT, VALID_BODY));
        
        assertEquals("from", exception.getField());
        assertEquals("O e-mail não pode estar vazio", exception.getError());
    }

    @Test
    void shouldThrowExceptionWhenToIsNull() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
            () -> new EmailDetails(VALID_EMAIL, null, VALID_SUBJECT, VALID_BODY));
        
        assertEquals("to", exception.getField());
        assertEquals("O e-mail não pode estar vazio", exception.getError());
    }

    @Test
    void shouldThrowExceptionWhenSubjectIsNull() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
            () -> new EmailDetails(VALID_EMAIL, "recipient@example.com", null, VALID_BODY));
        
        assertEquals("subject", exception.getField());
        assertEquals("O assunto não pode estar vazio", exception.getError());
    }

    @Test
    void shouldThrowExceptionWhenBodyIsNull() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
            () -> new EmailDetails(VALID_EMAIL, "recipient@example.com", VALID_SUBJECT, null));
        
        assertEquals("bodyHtml", exception.getField());
        assertEquals("O conteúdo do e-mail não pode estar vazio", exception.getError());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        " ",
        "invalid-email",
        "@example.com",
        "test@",
        "test@.com",
        "test@com",
        "test@example."
    })
    void shouldThrowExceptionForInvalidEmailFormat(String invalidEmail) {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
            () -> new EmailDetails(invalidEmail, "recipient@example.com", VALID_SUBJECT, VALID_BODY));
        
        assertEquals("from", exception.getField());
        assertTrue(exception.getError().contains("Formato de e-mail inválido") || 
                  exception.getError().contains("O e-mail não pode estar vazio"));
    }

    @Test
    void shouldTrimWhitespaceFromParameters() {
        // Act
        EmailDetails details = new EmailDetails(
            "  test@example.com  ", 
            "  recipient@example.com  ", 
            "  Test Subject  ", 
            "  <html>  "
        );

        // Assert
        assertEquals("test@example.com", details.getFrom());
        assertEquals("recipient@example.com", details.getTo());
        assertEquals("Test Subject", details.getSubject());
        assertEquals("<html>", details.getBodyHtml());
    }

    @Test
    void shouldThrowExceptionForInvalidDomain() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
            () -> new EmailDetails("test@example.", "recipient@example.com", VALID_SUBJECT, VALID_BODY));
        
        assertEquals("from", exception.getField());
        assertTrue(exception.getError().contains("Domínio de e-mail inválido"));
    }

    @Test
    void shouldHaveInformativeToString() {
        // Arrange
        EmailDetails details = new EmailDetails(VALID_EMAIL, "recipient@example.com", VALID_SUBJECT, VALID_BODY);
        
        // Act
        String toStringResult = details.toString();
        
        // Assert
        assertTrue(toStringResult.contains("from='test@example.com'"));
        assertTrue(toStringResult.contains("to='recipient@example.com'"));
        assertTrue(toStringResult.contains("subject='Test Subject'"));
        assertTrue(toStringResult.contains("bodyHtml.length()=" + VALID_BODY.length()));
    }
}
