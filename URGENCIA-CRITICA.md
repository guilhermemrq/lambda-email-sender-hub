# Urgência CRÍTICA - Documentação

## ⚠️ Regra Importante

**Apenas feedbacks com urgência `CRITICA` são enviados para a fila SQS e processados pela Lambda.**

Feedbacks com outras urgências (BAIXA, MEDIA, ALTA) não devem ser enviados para esta fila.

## 🎯 Objetivo

A fila SQS `email-queue` foi configurada especificamente para processar **feedbacks críticos** que requerem atenção imediata da equipe de gestão.

## 📋 Características de Feedbacks Críticos

### Nota
- Geralmente **muito baixas**: 1-3
- Indicam insatisfação severa ou problemas graves

### Descrição
Deve detalhar claramente o problema crítico, como:
- Solicitações de cancelamento de contrato
- Problemas técnicos que impedem o uso da plataforma
- Insatisfação severa com qualidade do curso/professor
- Problemas financeiros (cobranças indevidas, etc.)
- Falta de comunicação/suporte

### Urgência
- **DEVE SER**: `"urgencia": "CRITICA"`
- Outros valores não serão processados por esta fila

## 📧 Template de Email Crítico

O email gerado para feedbacks críticos possui:

### Visual de Alerta
- 🎨 **Fundo**: Amarelo claro (#fff3cd) - indicando alerta
- 🔴 **Header**: Vermelho escuro (#b71c1c) com título em destaque
- 📦 **Borda**: 3px sólida vermelha ao redor do conteúdo
- ⚠️ **Caixa de Alerta**: Fundo vermelho com texto branco

### Conteúdo
- 🚨 Título: "FEEDBACK CRÍTICO - AÇÃO IMEDIATA NECESSÁRIA"
- 👤 Informações completas do estudante
- 🏛️ Turma e professor (se disponível)
- 📅 Data/hora do feedback
- ⚠️ Nível de urgência destacado
- 📊 Nota em destaque (fonte maior, cor vermelha)
- 📋 Descrição do problema em caixa destacada
- 🔴 Mensagem de ação requerida
- 🔖 IDs de rastreamento (feedbackId, correlationId)

### Rodapé
- Alerta de que é um email automático de feedback crítico
- Identificação do sistema

## 📝 Exemplos de Uso

### 1. Solicitação de Cancelamento
```json
{
  "feedbackId": "uuid-001",
  "emailEstudante": "aluno@example.com",
  "nomeEstudante": "João Silva",
  "nota": 1,
  "descricao": "Aluno solicitou encerramento de contrato e relatou problemas graves de conexão e falta de suporte adequado.",
  "urgencia": "CRITICA",
  "dataHora": "2025-12-05T14:30:00",
  "correlationId": "corr-001",
  "className": "Arquitetura de Software",
  "teacherName": "Prof. João Silva"
}
```

### 2. Problema Técnico Grave
```json
{
  "feedbackId": "uuid-002",
  "emailEstudante": "maria@example.com",
  "nomeEstudante": "Maria Santos",
  "nota": 2,
  "descricao": "Não consegui acessar a plataforma durante toda a semana. Sistema apresenta erros constantes e perdi prazos importantes.",
  "urgencia": "CRITICA",
  "dataHora": "2025-12-05T15:45:00",
  "correlationId": "corr-002",
  "className": "Desenvolvimento Web",
  "teacherName": "Prof. Ana Costa"
}
```

### 3. Insatisfação Severa
```json
{
  "feedbackId": "uuid-003",
  "emailEstudante": "pedro@example.com",
  "nomeEstudante": "Pedro Oliveira",
  "nota": 1,
  "descricao": "Extremamente insatisfeito com a qualidade do curso. Conteúdo desatualizado e professor despreparado. Exijo reembolso.",
  "urgencia": "CRITICA",
  "dataHora": "2025-12-05T16:20:00",
  "correlationId": "corr-003",
  "className": "Banco de Dados",
  "teacherName": "Prof. Carlos Mendes"
}
```

## 🧪 Como Testar

### Usando o Script de Teste
```bash
./send-test-message.sh
```

O script oferece 4 opções de feedbacks críticos pré-configurados:
1. Solicitação de Cancelamento
2. Problema Técnico Grave
3. Insatisfação Severa
4. Feedback Crítico Personalizado

### Manualmente via AWS CLI
```bash
aws sqs send-message \
  --queue-url https://sqs.sa-east-1.amazonaws.com/992382492436/email-queue \
  --message-body '{
    "feedbackId": "test-001",
    "emailEstudante": "aluno@example.com",
    "nomeEstudante": "João Silva",
    "nota": 1,
    "descricao": "Problema crítico que requer atenção imediata.",
    "urgencia": "CRITICA",
    "dataHora": "2025-12-05T14:30:00",
    "correlationId": "corr-001",
    "className": "Arquitetura de Software",
    "teacherName": "Prof. João Silva"
  }' \
  --region sa-east-1
```

## 🔍 Verificação

### Verificar Logs
```bash
aws logs tail /aws/lambda/SqsEmailHandler --follow --region sa-east-1
```

### Verificar Mensagens na Fila
```bash
aws sqs get-queue-attributes \
  --queue-url https://sqs.sa-east-1.amazonaws.com/992382492436/email-queue \
  --attribute-names ApproximateNumberOfMessages \
  --region sa-east-1
```

## ⚙️ Configuração do Código

### EmailPayload.java
```java
public enum Urgencia {
    BAIXA, MEDIA, ALTA, CRITICA
}
```

### EmailFormatter.java
```java
switch (payload.getUrgencia()) {
    case CRITICA:
        return buildCriticalFeedbackBody(payload);
    case ALTA:
        return buildUrgentFeedbackBody(payload);
    case MEDIA:
        return buildStandardFeedbackBody(payload);
    case BAIXA:
    default:
        return buildSimpleFeedbackBody(payload);
}
```

## 📊 Fluxo de Processamento

```
┌─────────────────────┐
│  Sistema Feedback   │
│       (FIAP)        │
└──────────┬──────────┘
           │
           │ Feedback com urgencia="CRITICA"
           ▼
┌─────────────────────┐
│    Fila SQS         │
│   email-queue       │
└──────────┬──────────┘
           │
           │ Event Source Mapping
           ▼
┌─────────────────────┐
│  Lambda Handler     │
│  SqsEmailHandler    │
└──────────┬──────────┘
           │
           │ Processa e formata
           ▼
┌─────────────────────┐
│   Amazon SES        │
│  (Envio de Email)   │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Equipe de Gestão   │
│   (Destinatário)    │
└─────────────────────┘
```

## 🎨 Preview do Email

```
╔═══════════════════════════════════════════════════════════╗
║  🚨🚨 FEEDBACK CRÍTICO - AÇÃO IMEDIATA NECESSÁRIA 🚨🚨   ║
╚═══════════════════════════════════════════════════════════╝

┌───────────────────────────────────────────────────────────┐
│ ⚠️ ESTE FEEDBACK REQUER ATENÇÃO URGENTE E PRIORITÁRIA ⚠️ │
└───────────────────────────────────────────────────────────┘

Estudante: João Silva <aluno@example.com>

╔═══════════════════════════════════════════════════════════╗
║ 🏛️ Turma: Arquitetura de Software                        ║
║ 👨‍🏫 Professor: Prof. João Silva                           ║
╚═══════════════════════════════════════════════════════════╝

Data/Hora: 05/12/2025 14:30:00
⚠️ Nível de Urgência: CRÍTICA
Nota: 1/10

┌───────────────────────────────────────────────────────────┐
│ 📋 Feedback Detalhado:                                    │
│                                                           │
│ Aluno solicitou encerramento de contrato e relatou       │
│ problemas graves de conexão e falta de suporte adequado. │
└───────────────────────────────────────────────────────────┘

╔═══════════════════════════════════════════════════════════╗
║ 🔴 AÇÃO REQUERIDA:                                        ║
║                                                           ║
║ Este feedback foi classificado como CRÍTICO e requer     ║
║ atenção imediata da equipe de gestão. Por favor, entre   ║
║ em contato com o estudante o mais rápido possível e      ║
║ tome as medidas necessárias.                             ║
╚═══════════════════════════════════════════════════════════╝

🔖 ID do Feedback: uuid-001
🔗 Correlation ID: corr-001

───────────────────────────────────────────────────────────
⚠️ Este é um alerta automático de feedback crítico ⚠️
Sistema de Gestão de Feedbacks - FIAP
```

## 📚 Arquivos Relacionados

- `src/main/java/br/com/fiap/lambda/model/EmailPayload.java` - Modelo com enum Urgencia
- `src/main/java/br/com/fiap/lambda/service/EmailFormatter.java` - Template de email crítico
- `test-sqs-event.json` - Exemplos de eventos SQS
- `example-payloads.json` - Exemplos de payloads críticos
- `send-test-message.sh` - Script de teste
- `SQS-SETUP.md` - Documentação da configuração SQS

## ✅ Checklist

- [x] Enum `Urgencia` inclui `CRITICA`
- [x] Método `buildCriticalFeedbackBody()` implementado
- [x] Template HTML com visual de alerta
- [x] Arquivos de teste atualizados
- [x] Script de teste focado em urgência CRITICA
- [x] Documentação atualizada
- [x] Exemplos de payloads críticos criados

---

**Última Atualização**: 2025-12-04  
**Versão**: 3.0 - Suporte exclusivo para urgência CRÍTICA
