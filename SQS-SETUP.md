# Configuração da Fila SQS

Este documento explica como configurar a fila Amazon SQS para o projeto Lambda Email Sender.

## Pré-requisitos

1. **Fila SQS já criada**:
   - **Nome**: `feedback-critical-queue`
   - **URL**: `https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue`
   - **ARN**: `arn:aws:sqs:sa-east-1:992382492436:feedback-critical-queue`
   - **Região**: `sa-east-1`

2. **Verificar as permissões IAM**:
   A role `FeedbackHubLambdaEmailSender` precisa ter as seguintes permissões:
   - `sqs:ReceiveMessage`
   - `sqs:DeleteMessage`
   - `sqs:GetQueueAttributes`

   Exemplo de política IAM:
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Effect": "Allow",
         "Action": [
           "sqs:ReceiveMessage",
           "sqs:DeleteMessage",
           "sqs:GetQueueAttributes"
         ],
         "Resource": "arn:aws:sqs:sa-east-1:992382492436:feedback-critical-queue"
       }
     ]
   }
   ```

## Configuração

### Variáveis no deploy-lambda.sh

As seguintes variáveis foram configuradas no script `deploy-lambda.sh`:

- `AWS_ACCOUNT_ID`: ID da sua conta AWS (992382492436)
- `SQS_QUEUE_NAME`: Nome da fila SQS (`feedback-critical-queue`)
- `SQS_QUEUE_URL`: URL da fila (`https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue`)
- `SQS_QUEUE_ARN`: ARN completo da fila (`arn:aws:sqs:sa-east-1:992382492436:feedback-critical-queue`)
- `SQS_BATCH_SIZE`: Tamanho do lote de mensagens processadas por vez (10)

### Personalização

Se você quiser usar uma fila diferente, edite as variáveis no arquivo `deploy-lambda.sh`:

```bash
export SQS_QUEUE_NAME="minha-fila-personalizada"
export SQS_BATCH_SIZE="5"  # Processar 5 mensagens por vez
```

## Comandos Disponíveis

### 1. Criar Lambda com gatilho SQS automático
```bash
./deploy-lambda.sh create
```
Este comando cria a função Lambda e automaticamente configura o gatilho SQS.

### 2. Configurar gatilho SQS manualmente
Se a Lambda já existe e você quer adicionar o gatilho SQS:
```bash
./deploy-lambda.sh setup-sqs
```

### 3. Remover gatilho SQS
Para desconectar a fila SQS da Lambda:
```bash
./deploy-lambda.sh remove-sqs
```

### 4. Atualizar Lambda
```bash
./deploy-lambda.sh update
```

### 5. Deletar Lambda
```bash
./deploy-lambda.sh delete
```

## Testando a Integração

### 1. Enviar mensagem para a fila SQS

**Usando o script de teste (recomendado):**
```bash
./send-test-message.sh
```

**Manualmente via AWS CLI:**
```bash
aws sqs send-message \
  --queue-url https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue \
  --message-body '{
    "feedbackId": "test-001",
    "emailEstudante": "aluno@example.com",
    "nomeEstudante": "João Silva",
    "nota": 8,
    "descricao": "Excelente aula! Conteúdo muito bem explicado.",
    "urgencia": "MEDIA",
    "dataHora": "2025-12-05T14:30:00",
    "correlationId": "corr-001",
    "className": "Arquitetura de Software",
    "teacherName": "Prof. João Silva"
  }' \
  --region sa-east-1
```

### 2. Verificar logs da Lambda

```bash
aws logs tail /aws/lambda/SqsEmailHandler --follow --region sa-east-1
```

## Formato da Mensagem SQS

⚠️ **IMPORTANTE**: Apenas feedbacks com urgência **CRITICA** são enviados para a fila SQS e processados pela Lambda.

A mensagem enviada para a fila SQS deve estar no formato JSON esperado pela classe `EmailPayload`:

### Campos Obrigatórios
- `emailEstudante`: Email do estudante
- `nota`: Nota de 0 a 10 (feedbacks críticos geralmente têm notas baixas: 1-3)
- `descricao`: Descrição detalhada do problema crítico
- `urgencia`: **DEVE SER "CRITICA"** para ser processado

### Campos Opcionais
- `feedbackId`: ID único do feedback (UUID)
- `nomeEstudante`: Nome do estudante
- `dataHora`: Data/hora no formato ISO 8601 (yyyy-MM-dd'T'HH:mm:ss)
- `correlationId`: ID de correlação para rastreamento
- `className`: Nome da turma/disciplina
- `teacherName`: Nome do professor

### Exemplo Completo (Feedback Crítico)
```json
{
  "feedbackId": "b50f1ee-4e2a-4f9a-a3d2-0f1e2a3b4c5d",
  "emailEstudante": "aluno@example.com",
  "nomeEstudante": "João Silva",
  "nota": 1,
  "descricao": "Aluno solicitou encerramento de contrato e relatou problemas graves de conexão e falta de suporte adequado.",
  "urgencia": "CRITICA",
  "dataHora": "2025-12-05T14:30:00",
  "correlationId": "corr-987654",
  "className": "Arquitetura de Software",
  "teacherName": "Prof. João Silva"
}
```

### Exemplo Mínimo (Feedback Crítico)
```json
{
  "emailEstudante": "aluno@example.com",
  "nota": 2,
  "descricao": "Não consegui acessar a plataforma. Sistema apresenta erros constantes.",
  "urgencia": "CRITICA"
}
```

### Tipos de Feedbacks Críticos
Os feedbacks críticos geralmente envolvem:
- 🚫 **Solicitações de cancelamento**
- 🔧 **Problemas técnicos graves**
- 😠 **Insatisfação severa**
- 💰 **Problemas financeiros/cobranças**
- 📞 **Falta de comunicação/suporte**

**Consulte o arquivo `example-payloads.json` para mais exemplos.**

## Troubleshooting

### Erro: "Role não tem permissões"
Verifique se a role IAM tem as permissões necessárias para acessar a fila SQS.

### Erro: "Fila não encontrada"
Certifique-se de que a fila SQS existe na região correta (`sa-east-1`).

### Lambda não está sendo invocada
1. Verifique se o gatilho SQS está configurado:
   ```bash
   aws lambda list-event-source-mappings \
     --function-name SqsEmailHandler \
     --region sa-east-1
   ```

2. Verifique o estado do event source mapping (deve estar "Enabled").

### Mensagens não estão sendo processadas
1. Verifique os logs da Lambda
2. Verifique se há mensagens na Dead Letter Queue (DLQ) se configurada
3. Verifique se o formato da mensagem está correto

## Monitoramento

### Verificar métricas da fila SQS
```bash
aws cloudwatch get-metric-statistics \
  --namespace AWS/SQS \
  --metric-name NumberOfMessagesSent \
  --dimensions Name=QueueName,Value=email-queue \
  --start-time 2025-12-04T00:00:00Z \
  --end-time 2025-12-04T23:59:59Z \
  --period 3600 \
  --statistics Sum \
  --region sa-east-1
```

### Verificar invocações da Lambda
```bash
aws cloudwatch get-metric-statistics \
  --namespace AWS/Lambda \
  --metric-name Invocations \
  --dimensions Name=FunctionName,Value=SqsEmailHandler \
  --start-time 2025-12-04T00:00:00Z \
  --end-time 2025-12-04T23:59:59Z \
  --period 3600 \
  --statistics Sum \
  --region sa-east-1
```
