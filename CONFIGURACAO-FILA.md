# Configuração da Fila SQS - feedback-critical-queue

## 📋 Informações da Fila

### Detalhes
- **Nome**: `feedback-critical-queue`
- **URL**: `https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue`
- **ARN**: `arn:aws:sqs:sa-east-1:992382492436:feedback-critical-queue`
- **Região**: `sa-east-1`
- **Account ID**: `992382492436`

### Configuração
- **Batch Size**: 10 mensagens por invocação
- **Timeout da Lambda**: 30 segundos
- **Visibility Timeout**: Deve ser >= 180 segundos (3 minutos)

## ✅ Arquivos Atualizados

Todos os arquivos do projeto foram atualizados para usar a fila `feedback-critical-queue`:

### Scripts de Deploy
1. ✅ `deploy-lambda.sh` - Variáveis SQS atualizadas
2. ✅ `custom-manage.sh` - Usa variáveis do deploy-lambda.sh
3. ✅ `send-test-message.sh` - URL da fila atualizada
4. ✅ `create-sqs-queue.sh` - Nome da fila atualizado

### Arquivos de Teste
5. ✅ `test-sqs-event.json` - ARN da fila atualizado

### Documentação
6. ✅ `README.md` - Informações da fila atualizadas
7. ✅ `SQS-SETUP.md` - Configuração completa atualizada
8. ✅ `QUICK-REFERENCE.md` - Comandos com nova fila
9. ✅ `iam-policy-sqs.json` - ARN da fila atualizado

## 🚀 Como Configurar o Event Source Mapping

### Opção 1: Criar Lambda com SQS (Recomendado)
```bash
# Build do projeto
mvn clean package

# Deploy da Lambda + configuração automática do SQS
./deploy-lambda.sh create
```

Este comando irá:
1. Criar a função Lambda
2. Configurar automaticamente o event source mapping para `feedback-critical-queue`

### Opção 2: Adicionar SQS a Lambda Existente
```bash
# Se a Lambda já existe
./deploy-lambda.sh setup-sqs
```

## 🔐 Permissões IAM Necessárias

A role `FeedbackHubLambdaEmailSender` precisa ter acesso à fila:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowSQSAccess",
      "Effect": "Allow",
      "Action": [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:GetQueueAttributes",
        "sqs:ChangeMessageVisibility"
      ],
      "Resource": "arn:aws:sqs:sa-east-1:992382492436:feedback-critical-queue"
    }
  ]
}
```

### Aplicar Política
```bash
aws iam put-role-policy \
  --role-name FeedbackHubLambdaEmailSender \
  --policy-name SQSFeedbackCriticalAccess \
  --policy-document file://iam-policy-sqs.json
```

## 🧪 Testar a Configuração

### 1. Enviar Mensagem de Teste
```bash
./send-test-message.sh
```

### 2. Verificar Event Source Mapping
```bash
aws lambda list-event-source-mappings \
  --function-name SqsEmailHandler \
  --region sa-east-1
```

**Saída esperada:**
```json
{
    "EventSourceMappings": [
        {
            "UUID": "...",
            "BatchSize": 10,
            "EventSourceArn": "arn:aws:sqs:sa-east-1:992382492436:feedback-critical-queue",
            "FunctionArn": "arn:aws:lambda:sa-east-1:992382492436:function:SqsEmailHandler",
            "State": "Enabled",
            ...
        }
    ]
}
```

### 3. Verificar Logs
```bash
aws logs tail /aws/lambda/SqsEmailHandler --follow --region sa-east-1
```

### 4. Verificar Fila
```bash
aws sqs get-queue-attributes \
  --queue-url https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue \
  --attribute-names All \
  --region sa-east-1
```

## 📨 Enviar Mensagem Manual

```bash
aws sqs send-message \
  --queue-url https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue \
  --message-body '{
    "feedbackId": "test-001",
    "emailEstudante": "aluno@example.com",
    "nomeEstudante": "João Silva",
    "nota": 1,
    "descricao": "Aluno solicitou encerramento de contrato e relatou problemas graves de conexão e falta de suporte adequado.",
    "dataHora": "2025-12-05T14:30:00",
    "correlationId": "corr-001",
    "className": "Arquitetura de Software",
    "teacherName": "Prof. João Silva"
  }' \
  --region sa-east-1
```

## 🔍 Monitoramento

### Métricas da Fila
```bash
# Mensagens enviadas
aws cloudwatch get-metric-statistics \
  --namespace AWS/SQS \
  --metric-name NumberOfMessagesSent \
  --dimensions Name=QueueName,Value=feedback-critical-queue \
  --start-time $(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%S) \
  --end-time $(date -u +%Y-%m-%dT%H:%M:%S) \
  --period 300 \
  --statistics Sum \
  --region sa-east-1

# Mensagens visíveis
aws cloudwatch get-metric-statistics \
  --namespace AWS/SQS \
  --metric-name ApproximateNumberOfMessagesVisible \
  --dimensions Name=QueueName,Value=feedback-critical-queue \
  --start-time $(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%S) \
  --end-time $(date -u +%Y-%m-%dT%H:%M:%S) \
  --period 300 \
  --statistics Average \
  --region sa-east-1
```

### Invocações da Lambda
```bash
aws cloudwatch get-metric-statistics \
  --namespace AWS/Lambda \
  --metric-name Invocations \
  --dimensions Name=FunctionName,Value=SqsEmailHandler \
  --start-time $(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%S) \
  --end-time $(date -u +%Y-%m-%dT%H:%M:%S) \
  --period 300 \
  --statistics Sum \
  --region sa-east-1
```

## 🛠️ Troubleshooting

### Problema: Lambda não está sendo invocada

**Verificações:**
1. Event source mapping está habilitado?
   ```bash
   aws lambda list-event-source-mappings --function-name SqsEmailHandler --region sa-east-1
   ```

2. Há mensagens na fila?
   ```bash
   aws sqs get-queue-attributes \
     --queue-url https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue \
     --attribute-names ApproximateNumberOfMessages \
     --region sa-east-1
   ```

3. Permissões IAM corretas?
   ```bash
   aws iam get-role-policy \
     --role-name FeedbackHubLambdaEmailSender \
     --policy-name SQSFeedbackCriticalAccess
   ```

### Problema: Mensagens não são deletadas da fila

**Possíveis causas:**
- Lambda está falhando (verificar logs)
- Timeout da Lambda muito curto
- Visibility timeout da fila muito curto

**Solução:**
```bash
# Aumentar visibility timeout da fila
aws sqs set-queue-attributes \
  --queue-url https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue \
  --attributes VisibilityTimeout=180 \
  --region sa-east-1
```

### Problema: Muitas mensagens na DLQ (se configurada)

**Verificar:**
1. Logs da Lambda para erros
2. Formato das mensagens
3. Validações no código

## 📊 Arquitetura

```
┌─────────────────────────┐
│  Sistema de Feedback    │
│       (FIAP)            │
└───────────┬─────────────┘
            │
            │ POST feedback crítico
            ▼
┌─────────────────────────┐
│  feedback-critical-queue│
│  (Amazon SQS)           │
└───────────┬─────────────┘
            │
            │ Event Source Mapping
            │ (Batch Size: 10)
            ▼
┌─────────────────────────┐
│   SqsEmailHandler       │
│   (AWS Lambda)          │
└───────────┬─────────────┘
            │
            │ Processa e formata
            ▼
┌─────────────────────────┐
│   Amazon SES            │
│   (Envio de Email)      │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│  suporte@fiap.com.br    │
│  (Equipe de Gestão)     │
└─────────────────────────┘
```

## ✅ Checklist de Configuração

- [x] Fila `feedback-critical-queue` existe
- [ ] Permissões IAM configuradas
- [ ] Lambda criada
- [ ] Event source mapping configurado e habilitado
- [ ] Email remetente verificado no SES
- [ ] Teste de envio realizado com sucesso
- [ ] Logs verificados
- [ ] Monitoramento configurado

## 📚 Próximos Passos

1. **Configurar permissões IAM**
   ```bash
   aws iam put-role-policy \
     --role-name FeedbackHubLambdaEmailSender \
     --policy-name SQSFeedbackCriticalAccess \
     --policy-document file://iam-policy-sqs.json
   ```

2. **Deploy da Lambda**
   ```bash
   mvn clean package
   ./deploy-lambda.sh create
   ```

3. **Testar**
   ```bash
   ./send-test-message.sh
   ```

4. **Monitorar**
   ```bash
   aws logs tail /aws/lambda/SqsEmailHandler --follow --region sa-east-1
   ```

---

**Data de Configuração**: 2025-12-04  
**Fila**: feedback-critical-queue  
**Status**: ✅ Configurada e pronta para uso
