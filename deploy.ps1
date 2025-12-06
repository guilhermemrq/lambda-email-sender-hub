# ============================================
# Script de Deploy - Lambda Email Sender
# ============================================

param(
    [switch]$SkipBuild,
    [switch]$SendTestMessage,
    [switch]$Help
)

$ErrorActionPreference = "Stop"

# Configurações
$FUNCTION_NAME = "SqsEmailHandler"
$REGION = "sa-east-1"
$QUEUE_URL = "https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue"
$TEST_MESSAGE_FILE = "test-message.json"

function Show-Help {
    Write-Host ""
    Write-Host "=== Script de Deploy - Lambda Email Sender ===" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Uso:" -ForegroundColor Yellow
    Write-Host "  .\deploy.ps1                    # Build completo e deploy"
    Write-Host "  .\deploy.ps1 -SkipBuild         # Apenas deploy (sem rebuild)"
    Write-Host "  .\deploy.ps1 -SendTestMessage   # Deploy + enviar mensagem de teste"
    Write-Host "  .\deploy.ps1 -Help              # Mostra esta ajuda"
    Write-Host ""
    Write-Host "Exemplos:" -ForegroundColor Yellow
    Write-Host "  .\deploy.ps1                                    # Deploy normal"
    Write-Host "  .\deploy.ps1 -SendTestMessage                   # Deploy e teste"
    Write-Host "  .\deploy.ps1 -SkipBuild -SendTestMessage        # Apenas deploy e teste"
    Write-Host ""
    exit 0
}

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host ">>> $Message" -ForegroundColor Green
}

function Write-Error-Message {
    param([string]$Message)
    Write-Host ""
    Write-Host "ERRO: $Message" -ForegroundColor Red
    exit 1
}

if ($Help) {
    Show-Help
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   Lambda Email Sender - Deploy Automatizado   " -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

# ============================================
# 1. BUILD
# ============================================
if (-not $SkipBuild) {
    Write-Step "1/4 - Compilando projeto..."
    
    try {
        .\mvnw clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            Write-Error-Message "Falha no build do Maven"
        }
    } catch {
        Write-Error-Message "Erro ao executar Maven: $_"
    }
    
    if (-not (Test-Path "target\lambda-email-sender-1.0.0.jar")) {
        Write-Error-Message "JAR não foi gerado"
    }
    
    $jarSize = (Get-Item "target\lambda-email-sender-1.0.0.jar").Length / 1MB
    Write-Host "   Build concluido! JAR: $([math]::Round($jarSize, 2)) MB" -ForegroundColor Gray
} else {
    Write-Host ""
    Write-Host ">>> 1/4 - Build IGNORADO (usando JAR existente)" -ForegroundColor Yellow
}

# ============================================
# 2. CRIAR ZIP
# ============================================
Write-Step "2/4 - Criando pacote de deploy..."

Remove-Item "target\function.zip" -ErrorAction SilentlyContinue
Copy-Item "target\lambda-email-sender-1.0.0.jar" "target\function.zip"

if (-not (Test-Path "target\function.zip")) {
    Write-Error-Message "Falha ao criar function.zip"
}

$zipSize = (Get-Item "target\function.zip").Length / 1MB
Write-Host "   ZIP criado: $([math]::Round($zipSize, 2)) MB" -ForegroundColor Gray

# ============================================
# 3. DEPLOY NA AWS
# ============================================
Write-Step "3/4 - Fazendo deploy na AWS Lambda..."

try {
    $deployResult = aws lambda update-function-code `
        --function-name $FUNCTION_NAME `
        --zip-file fileb://target/function.zip `
        --region $REGION `
        --query "{Status:LastUpdateStatus,Modified:LastModified,CodeSize:CodeSize}" `
        --output json | ConvertFrom-Json
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Message "Falha no deploy da Lambda"
    }
    
    $codeSizeMB = [math]::Round($deployResult.CodeSize / 1MB, 2)
    Write-Host "   Deploy iniciado!" -ForegroundColor Gray
    Write-Host "   Status: $($deployResult.Status)" -ForegroundColor Gray
    Write-Host "   Tamanho: $codeSizeMB MB" -ForegroundColor Gray
    Write-Host "   Modificado: $($deployResult.Modified)" -ForegroundColor Gray
    
} catch {
    Write-Error-Message "Erro ao fazer deploy: $_"
}

# Aguardar Lambda ficar disponível
Write-Host ""
Write-Host "   Aguardando Lambda atualizar..." -ForegroundColor Gray
Start-Sleep -Seconds 10

# ============================================
# 4. TESTE (OPCIONAL)
# ============================================
Write-Step "4/4 - Finalizando..."

if ($SendTestMessage) {
    Write-Host ""
    Write-Host ">>> Enviando mensagem de teste para a fila SQS..." -ForegroundColor Yellow
    
    if (-not (Test-Path $TEST_MESSAGE_FILE)) {
        Write-Error-Message "Arquivo $TEST_MESSAGE_FILE não encontrado"
    }
    
    try {
        $messageResult = aws sqs send-message `
            --queue-url $QUEUE_URL `
            --message-body file://$TEST_MESSAGE_FILE `
            --region $REGION `
            --query "MessageId" `
            --output text
        
        if ($LASTEXITCODE -ne 0) {
            Write-Error-Message "Falha ao enviar mensagem para SQS"
        }
        
        Write-Host "   Mensagem enviada! ID: $messageResult" -ForegroundColor Gray
        Write-Host ""
        Write-Host "   Aguardando processamento (15s)..." -ForegroundColor Gray
        Start-Sleep -Seconds 15
        
        # Verificar logs
        Write-Host ""
        Write-Host ">>> Verificando logs..." -ForegroundColor Yellow
        
        $timestamp = [DateTimeOffset]::UtcNow.AddMinutes(-2).ToUnixTimeMilliseconds()
        
        $logs = aws logs filter-log-events `
            --log-group-name "/aws/lambda/$FUNCTION_NAME" `
            --start-time $timestamp `
            --filter-pattern $messageResult `
            --region $REGION `
            --query "events[*].message" `
            --output text
        
        if ($logs) {
            Write-Host ""
            Write-Host "=== LOGS DA EXECUÇÃO ===" -ForegroundColor Cyan
            Write-Host $logs -ForegroundColor White
        } else {
            Write-Host "   Nenhum log encontrado ainda. Verifique CloudWatch em alguns segundos." -ForegroundColor Yellow
        }
        
    } catch {
        Write-Error-Message "Erro ao enviar mensagem de teste: $_"
    }
}

# ============================================
# RESUMO FINAL
# ============================================
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   DEPLOY CONCLUÍDO COM SUCESSO!               " -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Função Lambda: $FUNCTION_NAME" -ForegroundColor White
Write-Host "Região: $REGION" -ForegroundColor White
Write-Host ""

if (-not $SendTestMessage) {
    Write-Host "Dica: Use -SendTestMessage para testar automaticamente" -ForegroundColor Yellow
    Write-Host "Exemplo: .\deploy.ps1 -SendTestMessage" -ForegroundColor Gray
    Write-Host ""
}

Write-Host "Para verificar logs manualmente:" -ForegroundColor Yellow
Write-Host "  aws logs tail /aws/lambda/$FUNCTION_NAME --follow --region $REGION" -ForegroundColor Gray
Write-Host ""
