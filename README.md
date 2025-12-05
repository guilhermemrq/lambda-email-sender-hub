# Lambda Email Sender

Função AWS Lambda para processar **feedbacks críticos** a partir de uma fila Amazon SQS e enviar alertas por email usando Amazon SES.

## 📋 Visão Geral

Este projeto implementa um handler Lambda que:
- Recebe mensagens de **feedbacks críticos** de uma fila Amazon SQS
- Processa apenas feedbacks com urgência **CRITICA**
- Envia emails de alerta usando Amazon SES (Simple Email Service)

## ⚠️ Importante

**Apenas feedbacks com `urgencia: "CRITICA"` são processados pela fila SQS.**

Feedbacks críticos incluem:
- 🚫 Solicitações de cancelamento
- 🔧 Problemas técnicos graves
- 😠 Insatisfação severa
- 💰 Problemas financeiros/cobranças
- 📞 Falta de comunicação/suporte

## 🚀 Quick Start

### 1. Pré-requisitos

- Java 11+
- Maven 3.8+
- AWS CLI configurado
- Conta AWS com acesso a Lambda, SQS e SES

### 2. Build do Projeto

```bash
mvn clean package
```

### 3. Criar a Fila SQS

```bash
chmod +x create-sqs-queue.sh
./create-sqs-queue.sh
```

### 4. Deploy da Lambda com SQS

```bash
chmod +x deploy-lambda.sh
./deploy-lambda.sh create
```

Este comando irá:
- Criar a função Lambda
- Configurar automaticamente o gatilho SQS

### 5. Testar o Envio de Email

```bash
chmod +x send-test-message.sh
./send-test-message.sh
```

## 📚 Documentação Detalhada

- **[URGENCIA-CRITICA.md](./URGENCIA-CRITICA.md)** - ⚠️ Documentação sobre urgência CRÍTICA
- **[SQS-SETUP.md](./SQS-SETUP.md)** - Guia completo de configuração do SQS
- **[iam-policy-sqs.json](./iam-policy-sqs.json)** - Política IAM necessária
- **[example-payloads.json](./example-payloads.json)** - Exemplos de payloads críticos

## 🔧 Configuração

### Variáveis de Ambiente

Edite o arquivo `deploy-lambda.sh` para personalizar:

```bash
export FUNCTION_NAME="SqsEmailHandler"
export AWS_REGION="sa-east-1"
export SQS_QUEUE_NAME="feedback-critical-queue"
export SQS_QUEUE_URL="https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue"
export SQS_QUEUE_ARN="arn:aws:sqs:sa-east-1:992382492436:feedback-critical-queue"
export SQS_BATCH_SIZE="10"
```

### Formato da Mensagem SQS (Feedback Crítico)

⚠️ **A urgência DEVE ser "CRITICA"**

```json
{
  "feedbackId": "uuid-001",
  "emailEstudante": "aluno@example.com",
  "nomeEstudante": "João Silva",
  "nota": 1,
  "descricao": "Aluno solicitou encerramento de contrato e relatou problemas graves.",
  "urgencia": "CRITICA",
  "dataHora": "2025-12-05T14:30:00",
  "correlationId": "corr-001",
  "className": "Arquitetura de Software",
  "teacherName": "Prof. João Silva"
}
```

## 📦 Comandos Disponíveis

```bash
# Criar Lambda com gatilho SQS
./deploy-lambda.sh create

# Atualizar código da Lambda
./deploy-lambda.sh update

# Configurar gatilho SQS (se já existe)
./deploy-lambda.sh setup-sqs

# Remover gatilho SQS
./deploy-lambda.sh remove-sqs

# Deletar Lambda
./deploy-lambda.sh delete

# Invocar Lambda com evento de teste
./deploy-lambda.sh invoke
```

## 🔍 Monitoramento

### Ver logs em tempo real

```bash
aws logs tail /aws/lambda/SqsEmailHandler --follow --region sa-east-1
```

### Verificar event source mappings

```bash
aws lambda list-event-source-mappings \
  --function-name SqsEmailHandler \
  --region sa-east-1
```

## 🏗️ Arquitetura

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   Cliente   │─────▶│  SQS Queue  │─────▶│   Lambda    │
│             │      │ email-queue │      │  Handler    │
└─────────────┘      └─────────────┘      └──────┬──────┘
                                                  │
                                                  ▼
                                          ┌─────────────┐
                                          │  Amazon SES │
                                          │   (Email)   │
                                          └─────────────┘
```

## 🛠️ Tecnologias

- **Java 11** - Linguagem de programação
- **Maven** - Gerenciamento de dependências
- **AWS Lambda** - Computação serverless
- **Amazon SQS** - Fila de mensagens
- **Amazon SES** - Serviço de email

---

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/lambda-email-sender-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- AWS Lambda ([guide](https://quarkus.io/guides/aws-lambda)): Write AWS Lambda functions

## Provided Code

### Amazon Lambda Integration example

This example contains a Quarkus Greeting Lambda ready for Amazon.

[Related guide section...](https://quarkus.io/guides/aws-lambda)


