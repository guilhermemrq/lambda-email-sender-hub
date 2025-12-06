# 📧 Lambda Email Sender - Sistema de Notificação de Feedbacks Críticos

Função AWS Lambda que processa feedbacks críticos de uma fila SQS e envia e-mails para **todos os administradores ativos** cadastrados no banco de dados.

## 📋 Visão Geral

Este projeto implementa um sistema completo de notificação que:
- ✅ Recebe mensagens de feedbacks críticos via Amazon SQS
- ✅ Busca todos os administradores ativos no banco RDS Aurora Serverless
- ✅ Envia e-mails formatados em HTML para cada administrador via Amazon SES
- ✅ Registra estatísticas de sucesso/falha no CloudWatch

## 🎯 Funcionalidades

- **Broadcast Automático**: E-mails enviados para todos os admins ativos
- **Encoding UTF-8**: Suporte completo a caracteres especiais do português
- **Template HTML Profissional**: E-mails formatados sem emojis
- **Integração com RDS**: Busca dinâmica de destinatários do banco
- **Logs Detalhados**: Rastreamento completo no CloudWatch

## 🚀 Quick Start

### 1. Pré-requisitos

- Java 11+
- Maven 3.8+
- AWS CLI configurado
- Conta AWS com acesso a Lambda, SQS e SES

### 2. Deploy Rápido

```powershell
# Deploy completo (build + deploy)
.\deploy.ps1

# Deploy + teste automático
.\deploy.ps1 -SendTestMessage

# Apenas deploy (sem rebuild)
.\deploy.ps1 -SkipBuild -SendTestMessage
```

## 🔧 Configuração

### Variáveis de Ambiente da Lambda

As seguintes variáveis devem estar configuradas na Lambda:

```bash
FROM_EMAIL=redes.guilherme@gmail.com
DB_CLUSTER_ARN=arn:aws:rds:sa-east-1:992382492436:cluster:database-course
DB_SECRET_ARN=arn:aws:secretsmanager:sa-east-1:992382492436:secret:secret-db-nep8fY
DB_NAME=dbcourse
```

### Permissões IAM Necessárias

A role da Lambda precisa das seguintes permissões (ver `rds-data-policy.json`):

- ✅ `rds-data:ExecuteStatement` - Para consultar o banco
- ✅ `secretsmanager:GetSecretValue` - Para acessar credenciais
- ✅ `ses:SendRawEmail` - Para enviar e-mails
- ✅ `sqs:ReceiveMessage` - Para processar fila
- ✅ `logs:CreateLogGroup/CreateLogStream/PutLogEvents` - Para logs

### Formato da Mensagem SQS

```json
{
  "feedbackId": "test-001",
  "emailEstudante": "aluno@example.com",
  "nomeEstudante": "João Silva",
  "nota": 1,
  "descricao": "Teste de feedback crítico",
  "dataHora": "2025-12-06T10:30:00",
  "correlationId": "test-corr-001",
  "className": "Arquitetura de Software",
  "teacherName": "Prof. Carlos"
}
```

## 📦 Scripts Disponíveis

### `deploy.ps1` - Script Principal de Deploy

```powershell
.\deploy.ps1                    # Build completo e deploy
.\deploy.ps1 -SkipBuild         # Apenas deploy (sem rebuild)
.\deploy.ps1 -SendTestMessage   # Deploy + enviar mensagem de teste
.\deploy.ps1 -Help              # Mostra ajuda
```

### Comandos Manuais

```powershell
# Build
.\mvnw clean package -DskipTests

# Deploy manual
aws lambda update-function-code --function-name SqsEmailHandler --zip-file fileb://target/function.zip --region sa-east-1

# Enviar mensagem de teste
aws sqs send-message --queue-url https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue --message-body file://test-message.json --region sa-east-1

# Ver logs
aws logs tail /aws/lambda/SqsEmailHandler --follow --region sa-east-1
```

## 🏗️ Arquitetura

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   Sistema    │─────▶│  SQS Queue   │─────▶│   Lambda     │
│   Feedback   │      │   Critical   │      │   Handler    │
└──────────────┘      └──────────────┘      └──────┬───────┘
                                                    │
                                                    ▼
                                            ┌──────────────┐
                                            │  RDS Aurora  │
                                            │  (Busca      │
                                            │   Admins)    │
                                            └──────┬───────┘
                                                    │
                                                    ▼
                                            ┌──────────────┐
                                            │ Amazon SES   │
                                            │ (Envia para  │
                                            │  N admins)   │
                                            └──────────────┘
```

## 🛠️ Tecnologias

- **Java 11** - Linguagem de programação
- **Maven 3.8+** - Gerenciamento de dependências
- **AWS Lambda** - Computação serverless
- **Amazon SQS** - Fila de mensagens
- **Amazon SES** - Serviço de e-mail
- **RDS Data API** - Acesso ao banco Aurora Serverless
- **AWS Secrets Manager** - Gerenciamento de credenciais

## 📊 Estrutura do Projeto

```
src/main/java/br/com/fiap/lambda/
├── exception/          # Exceções customizadas
├── gateway/            # Integração com SES
├── handler/            # Handler principal da Lambda
├── model/              # Models (EmailPayload, Usuario, etc)
├── repository/         # Repository para acesso ao RDS
├── service/            # Serviços (EmailBroadcastService, EmailFormatter)
└── util/               # Utilitários (JsonMapper)
```

## 📝 Logs e Monitoramento

### Exemplo de Log de Sucesso

```
Recebido evento SQS com 1 mensagens.
Processando Mensagem ID: 909f2517-412f-429f-920f-410fe52c68fa
Buscando administradores ativos
Encontrados 2 administradores ativos
Enviando e-mail para 2 administradores ativos
E-mail enviado com sucesso para: rodriguesqueirozcaike@gmail.com
E-mail enviado com sucesso para: redes.guilherme@gmail.com
Broadcast concluído para mensagem 909f2517. Total: 2 usuários, Sucesso: 2, Falhas: 0
Mensagem 909f2517 processada com sucesso
```

## 🔐 Segurança

- ✅ Credenciais do banco armazenadas no AWS Secrets Manager
- ✅ Permissões IAM com princípio de menor privilégio
- ✅ Encoding UTF-8 para prevenir problemas de caracteres
- ✅ Escape de HTML para prevenir XSS

## 📚 Documentação Adicional

- **[BROADCAST-FEATURE.md](./BROADCAST-FEATURE.md)** - Documentação completa da feature de broadcast
- **[rds-data-policy.json](./rds-data-policy.json)** - Política IAM para RDS Data API

---

**Desenvolvido para FIAP - Sistema de Gestão de Feedbacks**


