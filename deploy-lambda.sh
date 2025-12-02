#!/usr/bin/env bash

# Diretório de saída do build
TARGET_DIR="./target"
MANAGE_SCRIPT="./custom-manage.sh"
JAR_FILE="$(find ${TARGET_DIR} -name "*.jar" -not -name "*original*" | head -1)"

# Configurações da Lambda
export FUNCTION_NAME="SqsEmailHandler"
export HANDLER="br.com.fiap.lambda.handler.SqsEmailHandler::handleRequest"
export RUNTIME="java11"
export MEMORY_SIZE="512"
export TIMEOUT="30"
export AWS_REGION="us-east-1"
export LAMBDA_ROLE_ARN="arn:aws:iam::992382492436:role/FeedbackHubLambdaExecutionRole"
export ENVIRONMENT_VARS='{"Variables":{"FROM_EMAIL":"redes.guilherme@gmail.com"}}'

# Caminho para o arquivo ZIP
export ZIP_FILE="fileb://${TARGET_DIR}/function.zip"

# Função de ajuda
usage() {
  echo "Uso: ./deploy-lambda.sh [create|update|invoke|delete]"
  echo "Comandos disponíveis:"
  echo "  create  - Cria uma nova função Lambda"
  echo "  update  - Atualiza uma função Lambda existente"
  echo "  invoke  - Invoca a função Lambda com um evento de teste"
  echo "  delete  - Remove a função Lambda"
  echo ""
  echo "Variáveis de ambiente:"
  echo "  FUNCTION_NAME: Nome da função Lambda (padrão: SqsEmailHandler)"
  echo "  AWS_REGION: Região AWS (padrão: us-east-1)"
  echo "  LAMBDA_ROLE_ARN: ARN da função IAM para a Lambda"
}

# Verifica se o comando é válido
CMD=${1:-help}
if [[ "$CMD" == "help" ]]; then
  usage; exit 0
fi

# Verifica se o arquivo JAR existe
if [[ -z "${JAR_FILE}" ]]; then
  echo "🚨 Nenhum arquivo JAR encontrado em ${TARGET_DIR}/"
  echo "Execute 'mvn clean package' primeiro para gerar o pacote."
  exit 1
fi

# Cria o arquivo ZIP temporário
ZIP_FILE="${TARGET_DIR}/function.zip"
rm -f "${ZIP_FILE}"
zip -j "${ZIP_FILE}" "${JAR_FILE}"

echo "✅ Pacote criado: ${ZIP_FILE} (a partir de ${JAR_FILE})"

# Verifica se o script de gerenciamento existe
if [[ ! -f "${MANAGE_SCRIPT}" ]]; then
  echo "📝 Criando ${MANAGE_SCRIPT}..."
  cat > "${MANAGE_SCRIPT}" << 'EOL'
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
EOL

  chmod +x "${MANAGE_SCRIPT}"
  echo "✅ ${MANAGE_SCRIPT} criado com sucesso!"
fi

# Executa o comando
echo "🚀 Executando Lambda '${CMD}' via ${MANAGE_SCRIPT}"
./custom-manage.sh "${CMD}"

echo "✅ Operação concluída com sucesso!"
