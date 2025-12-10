$ErrorActionPreference = "Stop"

$QUEUE_URL = "https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue"
$REGION = "sa-east-1"

$timestamp = Get-Date -Format "yyyy-MM-ddTHH:mm:ss.fffffff00"
$testId = Get-Date -Format 'HHmmss'

$jsonPayload = "{`"feedbackId`":`"test-FINAL-$testId`",`"studentEmail`":`"aluno@example.com`",`"className`":`"Arquitetura de Software`",`"teacherName`":`"Prof. Carlos`",`"rating`":1,`"description`":`"Teste de feedback critico - Lambda criada com sucesso! Este e um teste de integracao do sistema de notificacoes de feedbacks criticos.`",`"urgencia`":`"CRITICA`",`"dataEnvio`":`"$timestamp`",`"correlationId`":`"test-corr-FINAL-$testId`"}"

$tempFile = [System.IO.Path]::GetTempFileName()
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($tempFile, $jsonPayload, $utf8NoBom)

try {
    $messageId = aws sqs send-message `
        --queue-url $QUEUE_URL `
        --message-body "file://$tempFile" `
        --region $REGION `
        --query "MessageId" `
        --output text
    
    Write-Host "Mensagem enviada com sucesso!" -ForegroundColor Green
    Write-Host "Message ID: $messageId" -ForegroundColor Cyan
} finally {
    Remove-Item $tempFile -ErrorAction SilentlyContinue
}
