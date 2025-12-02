#!/usr/bin/env bash

set -e

CMD=$1

case "$CMD" in
  create)
    echo "🚀 Criando função Lambda ${FUNCTION_NAME}..."
    aws lambda create-function \
      --function-name ${FUNCTION_NAME} \
      --runtime ${RUNTIME} \
      --handler ${HANDLER} \
      --memory-size ${MEMORY_SIZE} \
      --timeout ${TIMEOUT} \
      --role ${LAMBDA_ROLE_ARN} \
      --zip-file ${ZIP_FILE} \
      --environment "${ENVIRONMENT_VARS}" \
      --region ${AWS_REGION}
    ;;

  update)
    echo "🔄 Atualizando função Lambda ${FUNCTION_NAME}..."
    aws lambda update-function-code \
      --function-name ${FUNCTION_NAME} \
      --zip-file ${ZIP_FILE} \
      --region ${AWS_REGION}
    
    echo "🔄 Atualizando configuração..."
    aws lambda update-function-configuration \
      --function-name ${FUNCTION_NAME} \
      --memory-size ${MEMORY_SIZE} \
      --timeout ${TIMEOUT} \
      --environment "${ENVIRONMENT_VARS}" \
      --region ${AWS_REGION}
    ;;

  invoke)
    echo "⚡ Invocando função Lambda ${FUNCTION_NAME}..."
    aws lambda invoke \
      --function-name ${FUNCTION_NAME} \
      --payload file://test-sqs-event.json \
      --region ${AWS_REGION} \
      response.json
    
    echo "📄 Resposta:"
    cat response.json
    echo ""
    ;;

  delete)
    echo "🗑️  Removendo função Lambda ${FUNCTION_NAME}..."
    aws lambda delete-function \
      --function-name ${FUNCTION_NAME} \
      --region ${AWS_REGION}
    ;;

  *)
    echo "❌ Comando inválido: $CMD"
    exit 1
    ;;
esac
