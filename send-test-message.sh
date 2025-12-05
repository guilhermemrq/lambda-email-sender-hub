#!/usr/bin/env bash

# Script para enviar mensagens de teste para a fila SQS

set -e

# Configurações da Fila de Feedbacks Críticos
QUEUE_NAME="feedback-critical-queue"
AWS_REGION="sa-east-1"
AWS_ACCOUNT_ID="992382492436"

# URL da fila
QUEUE_URL="https://sqs.sa-east-1.amazonaws.com/992382492436/feedback-critical-queue"

echo "📨 Enviando mensagem de teste para a fila SQS"
echo "Fila: ${QUEUE_NAME}"
echo "URL: ${QUEUE_URL}"
echo ""

# Menu de opções
echo "⚠️  IMPORTANTE: Apenas feedbacks com urgência CRÍTICA são processados pela fila SQS"
echo ""
echo "Escolha o tipo de feedback CRÍTICO:"
echo "1) Solicitação de Cancelamento"
echo "2) Problema Técnico Grave"
echo "3) Insatisfação Severa"
echo "4) Feedback Crítico Personalizado"
echo ""
read -p "Opção: " OPTION

case $OPTION in
  1)
    MESSAGE_BODY=$(cat <<'EOF'
{
  "feedbackId": "test-critical-001",
  "emailEstudante": "aluno@example.com",
  "nomeEstudante": "João Silva",
  "nota": 1,
  "descricao": "Aluno solicitou encerramento de contrato e relatou problemas graves de conexão e falta de suporte adequado.",
  "urgencia": "CRITICA",
  "dataHora": "2025-12-05T14:30:00",
  "correlationId": "corr-test-001",
  "className": "Arquitetura de Software",
  "teacherName": "Prof. João Silva"
}
EOF
)
    ;;
  2)
    MESSAGE_BODY=$(cat <<'EOF'
{
  "feedbackId": "test-critical-002",
  "emailEstudante": "maria@example.com",
  "nomeEstudante": "Maria Santos",
  "nota": 2,
  "descricao": "Não consegui acessar a plataforma durante toda a semana. Sistema apresenta erros constantes e perdi prazos importantes.",
  "urgencia": "CRITICA",
  "dataHora": "2025-12-05T15:45:00",
  "correlationId": "corr-test-002",
  "className": "Desenvolvimento Web",
  "teacherName": "Prof. Ana Costa"
}
EOF
)
    ;;
  3)
    MESSAGE_BODY=$(cat <<'EOF'
{
  "feedbackId": "test-critical-003",
  "emailEstudante": "pedro@example.com",
  "nomeEstudante": "Pedro Oliveira",
  "nota": 1,
  "descricao": "Extremamente insatisfeito com a qualidade do curso. Conteúdo desatualizado e professor despreparado. Exijo reembolso.",
  "urgencia": "CRITICA",
  "dataHora": "2025-12-05T16:20:00",
  "correlationId": "corr-test-003",
  "className": "Banco de Dados",
  "teacherName": "Prof. Carlos Mendes"
}
EOF
)
    ;;
  4)
    echo ""
    echo "⚠️  Lembre-se: A urgência será automaticamente definida como CRÍTICA"
    echo ""
    read -p "Email do estudante: " EMAIL_ESTUDANTE
    read -p "Nome do estudante: " NOME_ESTUDANTE
    read -p "Nota (0-10): " NOTA
    read -p "Descrição do problema crítico: " DESCRICAO
    read -p "Nome da turma: " CLASS_NAME
    read -p "Nome do professor: " TEACHER_NAME
    
    FEEDBACK_ID="test-custom-$(date +%s)"
    CORRELATION_ID="corr-custom-$(date +%s)"
    DATA_HORA=$(date -u +"%Y-%m-%dT%H:%M:%S")
    
    MESSAGE_BODY=$(cat <<EOF
{
  "feedbackId": "${FEEDBACK_ID}",
  "emailEstudante": "${EMAIL_ESTUDANTE}",
  "nomeEstudante": "${NOME_ESTUDANTE}",
  "nota": ${NOTA},
  "descricao": "${DESCRICAO}",
  "urgencia": "CRITICA",
  "dataHora": "${DATA_HORA}",
  "correlationId": "${CORRELATION_ID}",
  "className": "${CLASS_NAME}",
  "teacherName": "${TEACHER_NAME}"
}
EOF
)
    ;;
  *)
    echo "❌ Opção inválida!"
    exit 1
    ;;
esac

echo ""
echo "📋 Payload da mensagem:"
echo "${MESSAGE_BODY}"
echo ""

# Envia a mensagem
MESSAGE_ID=$(aws sqs send-message \
  --queue-url ${QUEUE_URL} \
  --message-body "${MESSAGE_BODY}" \
  --region ${AWS_REGION} \
  --query 'MessageId' \
  --output text)

echo "✅ Mensagem enviada com sucesso!"
echo "Message ID: ${MESSAGE_ID}"
echo ""
echo "💡 Dica: Para verificar os logs da Lambda, execute:"
echo "   aws logs tail /aws/lambda/SqsEmailHandler --follow --region ${AWS_REGION}"
