package br.com.fiap.lambda.service;

import br.com.fiap.lambda.exception.EmailSendingException;
import br.com.fiap.lambda.exception.ValidationException;
import br.com.fiap.lambda.gateway.EmailSender;
import br.com.fiap.lambda.model.EmailDetails;
import br.com.fiap.lambda.model.EmailPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_SUBJECT = "Test Subject";
    private static final String TEST_TEMPLATE = "template";
    private static final String TEST_FROM_EMAIL = "noreply@fiap.com.br";

    @Mock
    private EmailSender emailSender;

    @Mock
    private EmailFormatter emailFormatter;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(emailSender, emailFormatter, TEST_FROM_EMAIL);
    }

    @Test
    void shouldSendEmailSuccessfully() {
        // Arrange
        EmailPayload payload = new EmailPayload(TEST_EMAIL, TEST_SUBJECT, TEST_TEMPLATE, null);
        String htmlBody = "<html>Test HTML</html>";
        
        when(emailFormatter.format(payload)).thenReturn(htmlBody);
        doNothing().when(emailSender).send(any(EmailDetails.class));

        // Act
        emailService.processAndSend(payload);

        // Assert
        verify(emailFormatter).format(payload);
        verify(emailSender).send(any(EmailDetails.class));
    }

    @Test
    void shouldThrowExceptionWhenPayloadIsNull() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> emailService.processAndSend(null));
        
        assertEquals("O payload não pode ser nulo", exception.getMessage());
        verifyNoInteractions(emailFormatter, emailSender);
    }

    @Test
    void shouldThrowExceptionWhenRecipientEmailIsNull() {
        // Arrange
        EmailPayload payload = new EmailPayload(null, TEST_SUBJECT, TEST_TEMPLATE, null);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> emailService.processAndSend(payload));
        
        assertEquals("O e-mail do destinatário não pode estar vazio", exception.getMessage());
        verifyNoInteractions(emailFormatter, emailSender);
    }

    @Test
    void shouldThrowExceptionWhenSubjectIsNull() {
        // Arrange
        EmailPayload payload = new EmailPayload(TEST_EMAIL, null, TEST_TEMPLATE, null);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> emailService.processAndSend(payload));
        
        assertEquals("O assunto do e-mail não pode estar vazio", exception.getMessage());
        verifyNoInteractions(emailFormatter, emailSender);
    }

    @Test
    void shouldThrowEmailSendingExceptionWhenFormattingFails() {
        // Arrange
        EmailPayload payload = new EmailPayload(TEST_EMAIL, TEST_SUBJECT, TEST_TEMPLATE, null);
        
        when(emailFormatter.format(payload)).thenThrow(new RuntimeException("Formatting error"));

        // Act & Assert
        EmailSendingException exception = assertThrows(EmailSendingException.class, 
            () -> emailService.processAndSend(payload));
        
        assertTrue(exception.getMessage().contains("Falha ao processar/enviar e-mail para"));
        assertNotNull(exception.getCause());
        assertEquals("Formatting error", exception.getCause().getMessage());
        verify(emailFormatter).format(payload);
        verifyNoInteractions(emailSender);
    }

    @Test
    void shouldThrowEmailSendingExceptionWhenSendingFails() {
        // Arrange
        EmailPayload payload = new EmailPayload(TEST_EMAIL, TEST_SUBJECT, TEST_TEMPLATE, null);
        String htmlBody = "<html>Test HTML</html>";
        
        when(emailFormatter.format(payload)).thenReturn(htmlBody);
        doThrow(new RuntimeException("Sending error")).when(emailSender).send(any(EmailDetails.class));

        // Act & Assert
        EmailSendingException exception = assertThrows(EmailSendingException.class, 
            () -> emailService.processAndSend(payload));
        
        assertTrue(exception.getMessage().contains("Falha ao processar/enviar e-mail para"));
        assertNotNull(exception.getCause());
        assertEquals("Sending error", exception.getCause().getMessage());
        verify(emailFormatter).format(payload);
        verify(emailSender).send(any(EmailDetails.class));
    }
}
