# Changelog - Configuração SQS

## Alterações Realizadas

### 📝 Arquivos Modificados

#### 1. `deploy-lambda.sh`
**Adicionado:**
- Variáveis de configuração da fila SQS:
  - `AWS_ACCOUNT_ID`: ID da conta AWS
  - `SQS_QUEUE_NAME`: Nome da fila (padrão: email-queue)
  - `SQS_QUEUE_ARN`: ARN completo da fila
  - `SQS_BATCH_SIZE`: Tamanho do lote de mensagens (padrão: 10)
- Novos comandos na função `usage()`:
  - `setup-sqs`: Configura o gatilho SQS
  - `remove-sqs`: Remove o gatilho SQS

#### 2. `custom-manage.sh`
**Adicionado:**
- Configuração automática do gatilho SQS no comando `create`
- Novo comando `setup-sqs`: Cria event source mapping manualmente
- Novo comando `remove-sqs`: Remove event source mapping existente

### 📄 Novos Arquivos Criados

#### 1. `SQS-SETUP.md`
Documentação completa sobre:
- Pré-requisitos e permissões IAM necessárias
- Configuração das variáveis
- Comandos disponíveis
- Como testar a integração
- Formato da mensagem SQS
- Troubleshooting
- Monitoramento

#### 2. `iam-policy-sqs.json`
Política IAM completa com permissões para:
- Acesso à fila SQS (ReceiveMessage, DeleteMessage, etc.)
- Envio de emails via SES
- Logs no CloudWatch

#### 3. `create-sqs-queue.sh`
Script auxiliar para:
- Criar a fila SQS com configurações otimizadas
- Configurar Dead Letter Queue (DLQ) opcional
- Exibir resumo da configuração

#### 4. `send-test-message.sh`
Script para enviar mensagens de teste:
- Interface interativa para entrada de dados
- Envia mensagem formatada para a fila SQS
- Exibe dicas para monitoramento

#### 5. `README.md` (atualizado)
Documentação principal atualizada com:
- Visão geral do projeto
- Quick start guide
- Comandos disponíveis
- Diagrama de arquitetura
- Links para documentação detalhada

## 🎯 Como Usar

### Primeira Vez (Setup Completo)

```bash
# 1. Build do projeto
mvn clean package

# 2. Criar a fila SQS
chmod +x create-sqs-queue.sh
./create-sqs-queue.sh

# 3. Adicionar permissões IAM à role da Lambda
# Use o arquivo iam-policy-sqs.json como referência

# 4. Deploy da Lambda com gatilho SQS
chmod +x deploy-lambda.sh
./deploy-lambda.sh create

# 5. Testar
chmod +x send-test-message.sh
./send-test-message.sh
```

### Lambda Já Existe

Se a Lambda já está criada e você só quer adicionar o gatilho SQS:

```bash
./deploy-lambda.sh setup-sqs
```

### Atualizar Código

```bash
mvn clean package
./deploy-lambda.sh update
```

## 🔧 Personalização

### Mudar o Nome da Fila

Edite `deploy-lambda.sh`:
```bash
export SQS_QUEUE_NAME="minha-fila-personalizada"
```

### Ajustar Tamanho do Lote

Edite `deploy-lambda.sh`:
```bash
export SQS_BATCH_SIZE="5"  # Processar 5 mensagens por vez
```

### Mudar Região

Edite `deploy-lambda.sh`:
```bash
export AWS_REGION="us-east-1"
```

## ✅ Checklist de Configuração

- [ ] Fila SQS criada
- [ ] Permissões IAM configuradas na role da Lambda
- [ ] Lambda criada/atualizada
- [ ] Gatilho SQS configurado
- [ ] Email remetente verificado no SES
- [ ] Teste de envio realizado

## 📊 Verificação

### Verificar se o gatilho está ativo

```bash
aws lambda list-event-source-mappings \
  --function-name SqsEmailHandler \
  --region sa-east-1
```

### Verificar logs

```bash
aws logs tail /aws/lambda/SqsEmailHandler --follow --region sa-east-1
```

### Verificar mensagens na fila

```bash
aws sqs get-queue-attributes \
  --queue-url https://sqs.sa-east-1.amazonaws.com/992382492436/email-queue \
  --attribute-names All \
  --region sa-east-1
```

## 🐛 Troubleshooting

### Erro: "Role não tem permissões"
- Verifique se adicionou as permissões do arquivo `iam-policy-sqs.json`
- Aguarde alguns segundos para a propagação das permissões

### Lambda não está sendo invocada
- Verifique se o event source mapping está "Enabled"
- Verifique se há mensagens na fila
- Verifique os logs da Lambda

### Emails não estão sendo enviados
- Verifique se o email remetente está verificado no SES
- Verifique se a conta SES está fora do sandbox (para enviar para qualquer email)
- Verifique os logs da Lambda para erros

## 📚 Referências

- [AWS Lambda Event Source Mappings](https://docs.aws.amazon.com/lambda/latest/dg/invocation-eventsourcemapping.html)
- [Amazon SQS](https://docs.aws.amazon.com/sqs/)
- [Amazon SES](https://docs.aws.amazon.com/ses/)
