# Referência Rápida - Lambda Email Sender

## 🚀 Comandos Essenciais

### Build e Deploy

```bash
# Build do projeto
mvn clean package

# Criar Lambda + SQS
./deploy-lambda.sh create

# Atualizar Lambda
./deploy-lambda.sh update

# Deletar Lambda
./deploy-lambda.sh delete
```

### Gerenciamento SQS

```bash
# Criar fila SQS
./create-sqs-queue.sh

# Configurar gatilho SQS
./deploy-lambda.sh setup-sqs

# Remover gatilho SQS
./deploy-lambda.sh remove-sqs

# Enviar mensagem de teste
./send-test-message.sh
```

## 📨 Enviar Mensagem Manualmente

```bash
aws sqs send-message \
  --queue-url https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue \
  --message-body '{
    "feedbackId": "test-001",
    "emailEstudante": "aluno@example.com",
    "nomeEstudante": "João Silva",
    "nota": 1,
    "descricao": "Problema crítico que requer atenção imediata."
  }' \
  --region sa-east-1
```

## 🔍 Monitoramento

### Ver Logs da Lambda

```bash
# Logs em tempo real
aws logs tail /aws/lambda/SqsEmailHandler --follow --region sa-east-1

# Últimas 50 linhas
aws logs tail /aws/lambda/SqsEmailHandler --region sa-east-1

# Filtrar por erro
aws logs tail /aws/lambda/SqsEmailHandler --filter-pattern "ERROR" --region sa-east-1
```

### Verificar Event Source Mapping

```bash
# Listar mappings
aws lambda list-event-source-mappings \
  --function-name SqsEmailHandler \
  --region sa-east-1

# Ver detalhes de um mapping específico
aws lambda get-event-source-mapping \
  --uuid <UUID> \
  --region sa-east-1
```

### Verificar Fila SQS

```bash
# Atributos da fila
aws sqs get-queue-attributes \
  --queue-url https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue \
  --attribute-names All \
  --region sa-east-1

# Número de mensagens
aws sqs get-queue-attributes \
  --queue-url https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue \
  --attribute-names ApproximateNumberOfMessages \
  --region sa-east-1
```

## 🛠️ Gerenciamento Lambda

### Informações da Função

```bash
# Ver configuração
aws lambda get-function \
  --function-name SqsEmailHandler \
  --region sa-east-1

# Ver apenas configuração
aws lambda get-function-configuration \
  --function-name SqsEmailHandler \
  --region sa-east-1
```

### Atualizar Configuração

```bash
# Atualizar timeout
aws lambda update-function-configuration \
  --function-name SqsEmailHandler \
  --timeout 60 \
  --region sa-east-1

# Atualizar memória
aws lambda update-function-configuration \
  --function-name SqsEmailHandler \
  --memory-size 1024 \
  --region sa-east-1

# Atualizar variável de ambiente
aws lambda update-function-configuration \
  --function-name SqsEmailHandler \
  --environment 'Variables={FROM_EMAIL=novo@email.com}' \
  --region sa-east-1
```

## 🔐 IAM e Permissões

### Verificar Role da Lambda

```bash
aws iam get-role \
  --role-name FeedbackHubLambdaEmailSender
```

### Listar Políticas Anexadas

```bash
aws iam list-attached-role-policies \
  --role-name FeedbackHubLambdaEmailSender
```

### Adicionar Política

```bash
aws iam put-role-policy \
  --role-name FeedbackHubLambdaEmailSender \
  --policy-name SQSAccessPolicy \
  --policy-document file://iam-policy-sqs.json
```

## 📊 CloudWatch Metrics

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

### Erros da Lambda

```bash
aws cloudwatch get-metric-statistics \
  --namespace AWS/Lambda \
  --metric-name Errors \
  --dimensions Name=FunctionName,Value=SqsEmailHandler \
  --start-time $(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%S) \
  --end-time $(date -u +%Y-%m-%dT%H:%M:%S) \
  --period 300 \
  --statistics Sum \
  --region sa-east-1
```

### Mensagens SQS

```bash
aws cloudwatch get-metric-statistics \
  --namespace AWS/SQS \
  --metric-name NumberOfMessagesSent \
  --dimensions Name=QueueName,Value=feedback-critical-queue \
  --start-time $(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%S) \
  --end-time $(date -u +%Y-%m-%dT%H:%M:%S) \
  --period 300 \
  --statistics Sum \
  --region sa-east-1
```

## 🧪 Testes

### Invocar Lambda Diretamente

```bash
# Com arquivo de teste
aws lambda invoke \
  --function-name SqsEmailHandler \
  --payload file://test-sqs-event.json \
  --region sa-east-1 \
  response.json

# Ver resposta
cat response.json
```

### Purgar Fila SQS

```bash
aws sqs purge-queue \
  --queue-url https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue \
  --region sa-east-1
```

## 📧 Amazon SES

### Verificar Email

```bash
aws ses verify-email-identity \
  --email-address seu@email.com \
  --region sa-east-1
```

### Listar Emails Verificados

```bash
aws ses list-identities \
  --region sa-east-1
```

### Verificar Status do Email

```bash
aws ses get-identity-verification-attributes \
  --identities seu@email.com \
  --region sa-east-1
```

### Sair do Sandbox (Production)

```bash
# Criar caso de suporte no console AWS
# Service: SES Sending Limits Increase
```

## 🔄 Troubleshooting Rápido

### Lambda não está sendo invocada

```bash
# 1. Verificar event source mapping
aws lambda list-event-source-mappings --function-name SqsEmailHandler --region sa-east-1

# 2. Verificar se há mensagens na fila
aws sqs get-queue-attributes --queue-url https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue --attribute-names ApproximateNumberOfMessages --region sa-east-1

# 3. Ver logs
aws logs tail /aws/lambda/SqsEmailHandler --region sa-east-1
```

### Recriar Event Source Mapping

```bash
# 1. Remover
./deploy-lambda.sh remove-sqs

# 2. Aguardar alguns segundos
sleep 5

# 3. Recriar
./deploy-lambda.sh setup-sqs
```

### Verificar Permissões

```bash
# Ver role da Lambda
aws lambda get-function-configuration \
  --function-name SqsEmailHandler \
  --query 'Role' \
  --output text \
  --region sa-east-1

# Ver políticas da role
ROLE_NAME=$(aws lambda get-function-configuration --function-name SqsEmailHandler --query 'Role' --output text --region sa-east-1 | awk -F'/' '{print $NF}')
aws iam list-attached-role-policies --role-name $ROLE_NAME
```

## 📝 Variáveis Importantes

```bash
# Região AWS
export AWS_REGION="sa-east-1"

# Nome da função Lambda
export FUNCTION_NAME="SqsEmailHandler"

# Nome da fila SQS
export SQS_QUEUE_NAME="feedback-critical-queue"

# URL da fila
export QUEUE_URL="https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue"

# ARN da fila
export SQS_QUEUE_ARN="arn:aws:sqs:sa-east-1:992382492436:feedback-critical-queue"
```

## 🎯 Workflow Típico

```bash
# 1. Fazer alterações no código
vim src/main/java/br/com/fiap/lambda/handler/SqsEmailHandler.java

# 2. Build
mvn clean package

# 3. Deploy
./deploy-lambda.sh update

# 4. Testar
./send-test-message.sh

# 5. Monitorar
aws logs tail /aws/lambda/SqsEmailHandler --follow --region sa-east-1
```
