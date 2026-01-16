# OzPay

Plataforma SaaS de orquestração inteligente de pagamentos para múltiplos gateways

## Visão Geral

OzPay é um sistema multi-tenant que abstrai a complexidade de integração com diferentes provedores de pagamento. A plataforma oferece uma API unificada que permite processar transações através de múltiplos gateways (Stripe, Cielo, PayPal, etc.) com roteamento inteligente, resiliência avançada e observabilidade completa.

O projeto será comercializado como SaaS (domínio: OzPay.app ou OzPay.io em planejamento) mas mantém o código-fonte open-source para garantir transparência e permitir que a comunidade contribua com melhorias.

## Motivação

### Problemas que o OzPay Resolve

**Múltiplas APIs de Pagamento**
- Cada gateway possui sua própria API, formato de dados e comportamento
- Integrar com vários provedores requer manter código duplicado e complexo
- OzPay oferece uma API única e consistente para todos os gateways

**Falhas Transitórias**
- Gateways podem falhar temporariamente ou ter problemas de latência
- Uma transação rejeitada em um gateway pode ser aprovada em outro
- OzPay implementa retry inteligente e failover automático entre gateways

**Credenciais Seguras**
- Cada tenant precisa de suas próprias credenciais para os gateways
- Armazenar e gerenciar credenciais de forma segura é crítico
- OzPay utiliza CredentialVault com isolamento por tenant

**Multi-Tenancy Complexo**
- SaaS precisa isolar completamente os dados e credenciais de cada cliente
- Contexto de tenant deve ser propagado por toda a aplicação
- OzPay implementa multi-tenancy nativo com ThreadLocal e isolamento de dados

## Arquitetura

O OzPay segue Clean Architecture com separação clara entre Domain, Application e Infrastructure. O sistema é composto por 7 camadas principais:

### Camada de API (Entrada SaaS)

**PaymentController**
- REST API para processar pagamentos
- Endpoint unificado que abstrai múltiplos gateways
- Validação de entrada e formatação de resposta

**ApiKeyFilter**
- Autenticação via header `X-Nexus-Key`
- Identificação e validação do tenant
- Estabelecimento do contexto de segurança

### Camada de Orquestração

**MetaPaymentGateway**
- Orquestrador central que coordena todo o fluxo de pagamento
- Gerencia idempotência para evitar duplicação de transações
- Publica eventos de domínio para integração assíncrona
- Coordena a persistência do estado da transação

### Motor de Decisão (The Brain)

**PaymentRouter**
- Seleciona o melhor gateway baseado em estratégia configurável
- Permite diferentes estratégias: round-robin, least-latency, cost-optimized

**SmartRoutingStrategy**
- Estratégia padrão que combina múltiplos fatores
- Scorers para latência, custo, taxa de sucesso e disponibilidade
- Decisão baseada em peso ponderado de cada fator

**RoutingDecision**
- Resultado auditável da decisão de roteamento
- Contém justificativa e scores de cada gateway avaliado
- Permite análise posterior para otimização

### Motor de Execução (The Muscle)

**PaymentExecutor**
- Executa a transação no gateway selecionado
- Gerencia timeout e cancelamento
- Registra resultado e tempo de execução

**GatewayRegistry**
- Registry Pattern para todos os gateways disponíveis
- Permite adicionar/remover gateways dinamicamente
- Mantém metadados de cada gateway (latência média, taxa de sucesso)

**RetryPolicy**
- Política de retry com exponential backoff
- Configurável por tipo de erro
- Limita número de tentativas para evitar loops

### Contexto e Estado

**PaymentContext**
- Contém estado completo da transação
- Inclui `tenantId` para isolamento multi-tenant
- Fornece acesso a credenciais via `getCredential(gatewayName)`
- Imutável para evitar efeitos colaterais

**ExecutionHistory**
- Histórico auditável de todas as tentativas
- Registra decisões de roteamento
- Permite debugging e análise de falhas

### Adaptadores de Gateway

**PaymentGateway Interface**
- Contrato comum para todos os provedores
- Implementações específicas: StripeGateway, CieloGateway, PayPalGateway
- Isolamento de credenciais por tenant via `context.getCredential()`
- Tradução de modelos de domínio para APIs específicas

### Serviços de Infraestrutura

**IdempotencyService**
- Lock distribuído com Redis
- Previne processamento duplicado de transações
- Usa idempotency key fornecida pelo cliente

**CredentialVault**
- Gerenciamento seguro de credenciais
- Isolamento por tenant
- Suporte a rotação de credenciais

**TransactionRepository**
- Persistência de transações
- Queries para histórico e auditoria
- Suporte a filtros por tenant, gateway, status, etc.

**DomainEventPublisher**
- Publicação de eventos de domínio
- Permite integração assíncrona
- Eventos: PaymentCreated, PaymentCompleted, PaymentFailed

## Características Principais

### Multi-Tenancy Nativo

- **API Key por Tenant**: Autenticação via header `X-Nexus-Key`
- **ThreadLocal Context**: Propagação automática do tenant por toda aplicação
- **CredentialVault**: Isolamento completo de credenciais por tenant
- **Particionamento de Dados**: Queries automáticas filtradas por tenant

### Roteamento Inteligente

- **Taxa de Sucesso**: Prioriza gateways com histórico de aprovações
- **Latência**: Considera tempo de resposta médio de cada gateway
- **Custo**: Otimiza baseado em taxas de transação
- **Disponibilidade**: Evita gateways com circuit breaker aberto

### Resiliência Hardcore

- **Idempotência**: Lock distribuído com Redis previne duplicação
- **Circuit Breaker**: Resilience4j para isolar falhas
- **Retry com Exponential Backoff**: Tentativas progressivas com delay crescente
- **Timeout Configurável**: Limites de tempo por gateway
- **Failover Automático**: Troca de gateway em caso de falha

### Observabilidade

- **Prometheus Metrics**: Métricas de latência, taxa de sucesso, erros
- **Grafana Dashboards**: Visualização em tempo real
- **Histórico Auditável**: Todas as decisões e tentativas são registradas
- **Distributed Tracing**: Rastreamento de transações ponta-a-ponta

## Stack Tecnológica

- **Runtime**: Java 21 + Spring Boot 3
- **Arquitetura**: Clean Architecture (Domain/Application/Infrastructure)
- **Persistência**: PostgreSQL 16 + JPA
- **Cache e Lock**: Redis
- **Resiliência**: Resilience4j (Circuit Breaker, Retry, Timeout)
- **Observabilidade**: Prometheus + Grafana + Spring Actuator
- **Containerização**: Docker + Docker Compose
- **Validação**: Bean Validation (Jakarta)
- **Build**: Maven

## Roadmap de Desenvolvimento

### Fase 0: Foundation
- Spring Boot 3 + Java 21
- Clean Architecture: Domain/Application/Infrastructure
- Docker Compose: PostgreSQL 16

### Fase 1: Domain Model
- Entidade: Payment & Tenant
- Value Objects: Money & Currency
- Ports: PaymentGateway

### Fase 2: Application
- DTOs: Records Java
- UseCase: ProcessPayment
- Testes unitários sem Spring

### Fase 3: Infra Basic
- JPA Adapters & Entity
- PaymentController REST
- FakeGateway (Thread.sleep)

### Fase 4: SaaS Security
- ApiKeyFilter OncePerRequest
- ThreadLocal Context
- Multi-tenancy no DB

### Fase 5: Gateway Engine
- Mocks: Stripe & Cielo
- Registry Pattern Map
- Strategy: SmartRouter

### Fase 6: Resiliência Hardcore
- Redis Lock Distribuído
- Circuit Breaker Resilience4j
- Retry Exponential Backoff

### Fase 7: Observabilidade
- Prometheus + Grafana
- Spring Actuator Metrics
- Dashboard de Latência

### Fase 8: Lançamento
- Swagger OpenAPI
- README.md completo
- Deploy Script

## Configuração

### Pré-requisitos

- Java 21
- Docker e Docker Compose
- Maven 3.8+

### Instalação

```bash
# Clonar o repositório
git clone https://github.com/vitinh0z/oz-pay.git
cd oz-pay

# Iniciar dependências (PostgreSQL, Redis)
docker-compose up -d

# Compilar o projeto
./mvnw clean install

# Executar a aplicação
./mvnw spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`

## Uso Básico

### Processar um Pagamento

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "X-Nexus-Key: your-tenant-api-key" \
  -d '{
    "idempotencyKey": "unique-transaction-id-123",
    "amount": 10000,
    "currency": "BRL",
    "customerEmail": "customer@example.com",
    "description": "Assinatura Premium - Plano Mensal"
  }'
```

### Resposta de Sucesso

```json
{
  "transactionId": "txn_abc123xyz",
  "status": "COMPLETED",
  "gateway": "stripe",
  "amount": 10000,
  "currency": "BRL",
  "createdAt": "2026-01-14T10:30:00Z",
  "executionTimeMs": 245
}
```

## Status do Projeto

O OzPay está em desenvolvimento ativo. A arquitetura e roadmap estão definidos, e o projeto está sendo construído incrementalmente seguindo as 8 fases planejadas.

Atualmente, a base do projeto está estabelecida com:
- Estrutura de Clean Architecture
- Entidades de domínio (User, Tenant)
- Camada de persistência com JPA

As próximas fases incluirão a implementação do motor de pagamentos, roteamento inteligente e resiliência.

## Licença

Este projeto está licenciado sob a MIT License - veja o arquivo [LICENSE](LICENSE) para detalhes.

### Estratégia Open-Source + SaaS

O OzPay será comercializado como SaaS (OzPay.app ou OzPay.io), mas o código-fonte permanecerá open-source. Esta estratégia oferece:

- **Transparência**: Clientes podem auditar o código que processa seus pagamentos
- **Confiança**: Sem vendor lock-in, possibilidade de self-hosting
- **Comunidade**: Contribuições da comunidade melhoram o produto para todos
- **Inovação**: Feedback rápido e colaboração aberta

## Contribuindo

Contribuições são bem-vindas! Por favor:

1. Faça fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

### Diretrizes

- Mantenha a Clean Architecture
- Escreva testes unitários para novas funcionalidades
- Siga as convenções de código Java
- Documente APIs públicas

## Contato

Desenvolvido por [@vitinh0z](https://github.com/vitinh0z)

Para questões e sugestões, abra uma issue no GitHub.
