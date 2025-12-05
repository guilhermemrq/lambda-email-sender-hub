#!/usr/bin/env bash

# Script para criar a fila SQS necessária para o Lambda Email Sender

set -e

# Configurações
QUEUE_NAME="feedback-critical-queue"
AWS_REGION="sa-east-1"
VISIBILITY_TIMEOUT="180"  # 3 minutos (deve ser >= timeout da Lambda)
MESSAGE_RETENTION="345600"  # 4 dias em segundos
RECEIVE_WAIT_TIME="20"  # Long polling de 20 segundos

echo "🚀 Criando fila SQS: ${QUEUE_NAME}"

# Verifica se a fila já existe
QUEUE_URL=$(aws sqs get-queue-url \
  --queue-name ${QUEUE_NAME} \
  --region ${AWS_REGION} \
  --output text 2>/dev/null || echo "")

if [[ -n "$QUEUE_URL" ]]; then
  echo "⚠️  A fila ${QUEUE_NAME} já existe!"
  echo "URL: ${QUEUE_URL}"
  
  # Obtém o ARN da fila
  QUEUE_ARN=$(aws sqs get-queue-attributes \
    --queue-url ${QUEUE_URL} \
    --attribute-names QueueArn \
    --region ${AWS_REGION} \
    --query 'Attributes.QueueArn' \
    --output text)
  
  echo "ARN: ${QUEUE_ARN}"
  exit 0
fi

# Cria a fila
QUEUE_URL=$(aws sqs create-queue \
  --queue-name ${QUEUE_NAME} \
  --region ${AWS_REGION} \
  --attributes \
    VisibilityTimeout=${VISIBILITY_TIMEOUT},\
MessageRetentionPeriod=${MESSAGE_RETENTION},\
ReceiveMessageWaitTimeSeconds=${RECEIVE_WAIT_TIME} \
  --query 'QueueUrl' \
  --output text)

echo "✅ Fila criada com sucesso!"
echo "URL: ${QUEUE_URL}"

# Obtém o ARN da fila
QUEUE_ARN=$(aws sqs get-queue-attributes \
  --queue-url ${QUEUE_URL} \
  --attribute-names QueueArn \
  --region ${AWS_REGION} \
  --query 'Attributes.QueueArn' \
  --output text)

echo "ARN: ${QUEUE_ARN}"

# Opcional: Criar Dead Letter Queue (DLQ)
read -p "Deseja criar uma Dead Letter Queue (DLQ)? (s/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Ss]$ ]]; then
  DLQ_NAME="${QUEUE_NAME}-dlq"
  
  echo "🚀 Criando Dead Letter Queue: ${DLQ_NAME}"
  
  DLQ_URL=$(aws sqs create-queue \
    --queue-name ${DLQ_NAME} \
    --region ${AWS_REGION} \
    --attributes \
      MessageRetentionPeriod=1209600 \
    --query 'QueueUrl' \
    --output text)
  
  DLQ_ARN=$(aws sqs get-queue-attributes \
    --queue-url ${DLQ_URL} \
    --attribute-names QueueArn \
    --region ${AWS_REGION} \
    --query 'Attributes.QueueArn' \
    --output text)
  
  echo "✅ DLQ criada com sucesso!"
  echo "DLQ URL: ${DLQ_URL}"
  echo "DLQ ARN: ${DLQ_ARN}"
  
  # Configura a política de redirecionamento para a DLQ
  REDRIVE_POLICY="{\"deadLetterTargetArn\":\"${DLQ_ARN}\",\"maxReceiveCount\":\"3\"}"
  
  aws sqs set-queue-attributes \
    --queue-url ${QUEUE_URL} \
    --attributes RedrivePolicy="${REDRIVE_POLICY}" \
    --region ${AWS_REGION}
  
  echo "✅ Política de redirecionamento configurada (maxReceiveCount: 3)"
fi

echo ""
echo "📋 Resumo da configuração:"
echo "  Nome da fila: ${QUEUE_NAME}"
echo "  Região: ${AWS_REGION}"
echo "  URL: ${QUEUE_URL}"
echo "  ARN: ${QUEUE_ARN}"
echo "  Visibility Timeout: ${VISIBILITY_TIMEOUT}s"
echo "  Message Retention: ${MESSAGE_RETENTION}s (4 dias)"
echo "  Long Polling: ${RECEIVE_WAIT_TIME}s"
echo ""
echo "✅ Configuração concluída!"
